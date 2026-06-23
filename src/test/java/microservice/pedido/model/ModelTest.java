package microservice.pedido.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class ModelTest {

	@Test
	void carritoCompraGestionaProductosDescuentosYEstado() {
		CarritoCompra carrito = new CarritoCompra();
		carrito.setIdCarrito(1L);
		carrito.setDetalles(new ArrayList<>());
		DetalleCarrito perfume = detalleCarrito(10L, 2, "5000");
		DetalleCarrito crema = detalleCarrito(20L, 1, "2000");

		carrito.agregarProducto(perfume);
		carrito.agregarProducto(crema);
		assertThat(carrito.getSubtotal()).isEqualByComparingTo("12000");
		assertThat(perfume.getCarrito()).isSameAs(carrito);
		assertThat(perfume.getIdCarrito()).isEqualTo(1L);

		carrito.quitarProducto(20L);
		assertThat(carrito.getDetalles()).extracting(DetalleCarrito::getIdProducto).containsExactly(10L);
		assertThat(carrito.getSubtotal()).isEqualByComparingTo("10000");

		carrito.aplicarDescuento(new BigDecimal("1500"));
		assertThat(carrito.getSubtotal()).isEqualByComparingTo("8500");
		carrito.aplicarDescuento(null);
		assertThat(carrito.getSubtotal()).isEqualByComparingTo("10000");
		carrito.aplicarDescuento(new BigDecimal("99999"));
		assertThat(carrito.getSubtotal()).isEqualByComparingTo("0");

		carrito.confirmarCompra();
		assertThat(carrito.getEstado()).isEqualTo("CONFIRMADO");
	}

	@Test
	void detalleCarritoActualizaCantidadSubtotalYRelacion() {
		DetalleCarrito detalle = detalleCarrito(30L, 1, "3000");
		CarritoCompra carrito = new CarritoCompra();
		carrito.setIdCarrito(9L);

		detalle.setCarrito(carrito);
		detalle.actualizarCantidad(4);

		assertThat(detalle.getSubtotal()).isEqualByComparingTo("12000");
		assertThat(detalle.getIdCarrito()).isEqualTo(9L);
		detalle.setCarrito(null);
		assertThat(detalle.getIdCarrito()).isNull();
	}

	@Test
	void cuponInicializaModificaValidaYCambiaEstado() {
		Cupon cupon = cuponVigente();
		cupon.setEstado(null);
		cupon.setUsosActuales(null);

		cupon.crearCupon();
		assertThat(cupon.getUsosActuales()).isZero();
		assertThat(cupon.getEstado()).isEqualTo("ACTIVO");

		cupon.modificarCupon("NUEVO10", "MONTO", new BigDecimal("1000"));
		assertThat(cupon.getCodigo()).isEqualTo("NUEVO10");
		assertThat(cupon.getTipoDescuento()).isEqualTo("MONTO");
		assertThat(cupon.getValorDescuento()).isEqualByComparingTo("1000");
		assertThat(cupon.validarCupon()).isTrue();

		cupon.desactivarCupon();
		assertThat(cupon.getEstado()).isEqualTo("INACTIVO");
		assertThat(cupon.validarCupon()).isFalse();

		cupon.activarCupon();
		assertThat(cupon.getEstado()).isEqualTo("ACTIVO");
		cupon.setFechaInicio(LocalDate.now().plusDays(1));
		assertThat(cupon.validarCupon()).isFalse();
		cupon.setFechaInicio(LocalDate.now().minusDays(1));
		cupon.setFechaVencimiento(LocalDate.now().minusDays(1));
		assertThat(cupon.validarCupon()).isFalse();
		cupon.setFechaVencimiento(LocalDate.now().plusDays(1));
		cupon.setUsosActuales(cupon.getLimiteUso());
		assertThat(cupon.validarCupon()).isFalse();
	}

	@Test
	void cuponCrearConEstadoEnBlancoOInformado() {
		Cupon conEstadoEnBlanco = cuponVigente();
		conEstadoEnBlanco.setEstado(" ");
		conEstadoEnBlanco.crearCupon();
		assertThat(conEstadoEnBlanco.getEstado()).isEqualTo("ACTIVO");

		Cupon conEstadoInformado = cuponVigente();
		conEstadoInformado.setEstado("INACTIVO");
		conEstadoInformado.setUsosActuales(3);
		conEstadoInformado.crearCupon();
		assertThat(conEstadoInformado.getEstado()).isEqualTo("INACTIVO");
		assertThat(conEstadoInformado.getUsosActuales()).isEqualTo(3);
	}

	@Test
	void pedidoCalculaTotalYCambiaEstado() {
		Pedido pedido = pedidoBase();
		pedido.setFechaPedido(null);
		pedido.setEstado(null);
		pedido.setDescuento(new BigDecimal("500"));

		pedido.crearPedido();

		assertThat(pedido.getFechaPedido()).isNotNull();
		assertThat(pedido.getEstado()).isEqualTo("CREADO");
		assertThat(pedido.getSubtotal()).isEqualByComparingTo("11500");
		assertThat(pedido.getTotal()).isEqualByComparingTo("11000");

		pedido.confirmarPedido();
		assertThat(pedido.getEstado()).isEqualTo("CONFIRMADO");
		pedido.cancelarPedido();
		assertThat(pedido.getEstado()).isEqualTo("CANCELADO");
		pedido.actualizarEstado("PAGADO");
		assertThat(pedido.getEstado()).isEqualTo("PAGADO");
	}

	@Test
	void pedidoCrearRespetaFechaYEstadoInformadosYDescuentoNulo() {
		LocalDateTime fecha = LocalDateTime.of(2026, 6, 21, 12, 0);
		Pedido pedido = pedidoBase();
		pedido.setFechaPedido(fecha);
		pedido.setEstado("CONFIRMADO");
		pedido.setDescuento(null);

		pedido.crearPedido();

		assertThat(pedido.getFechaPedido()).isEqualTo(fecha);
		assertThat(pedido.getEstado()).isEqualTo("CONFIRMADO");
		assertThat(pedido.getTotal()).isEqualByComparingTo("11500");

		pedido.setEstado(" ");
		pedido.crearPedido();
		assertThat(pedido.getEstado()).isEqualTo("CREADO");
	}

	@Test
	void detallePedidoCalculaActualizaQuitaYRelacionaPedido() {
		Pedido pedido = new Pedido();
		pedido.setIdPedido(77L);
		DetallePedido detalle = detallePedido(40L, 2, "3000", null);

		detalle.setPedido(pedido);
		assertThat(detalle.getIdPedido()).isEqualTo(77L);
		assertThat(detalle.calcularSubtotal()).isEqualByComparingTo("6000");

		detalle.setDescuento(new BigDecimal("1000"));
		detalle.actualizarCantidad(3);
		assertThat(detalle.getSubtotal()).isEqualByComparingTo("8000");

		detalle.quitarProducto();
		assertThat(detalle.getCantidad()).isZero();
		assertThat(detalle.getSubtotal()).isEqualByComparingTo("0");

		detalle.setPedido(null);
		assertThat(detalle.getIdPedido()).isNull();
	}

	@Test
	void historialRegistraConsultaYRelacionaPedido() {
		Pedido pedido = new Pedido();
		pedido.setIdPedido(55L);
		HistorialEstadoPedido historial = new HistorialEstadoPedido();

		historial.setPedido(pedido);
		historial.registrarCambio("CREADO", "CONFIRMADO", "Cliente confirma compra");

		assertThat(historial.getIdPedido()).isEqualTo(55L);
		assertThat(historial.getFechaCambio()).isNotNull();
		assertThat(historial.consultarHistorial()).isEqualTo("CREADO -> CONFIRMADO: Cliente confirma compra");

		historial.setPedido(null);
		assertThat(historial.getIdPedido()).isNull();
	}

	@Test
	void usoCuponRegistraValidaAnulaYRelacionaEntidades() {
		Pedido pedido = new Pedido();
		pedido.setIdPedido(11L);
		Cupon cupon = new Cupon();
		cupon.setIdCupon(22L);
		UsoCupon uso = new UsoCupon();

		uso.setPedido(pedido);
		uso.setCupon(cupon);
		uso.registrarUso();

		assertThat(uso.getIdPedido()).isEqualTo(11L);
		assertThat(uso.getIdCupon()).isEqualTo(22L);
		assertThat(uso.getFechaUso()).isNotNull();
		assertThat(uso.validarUso()).isTrue();

		uso.anularUso();
		assertThat(uso.getEstado()).isEqualTo("ANULADO");
		assertThat(uso.validarUso()).isFalse();

		uso.setPedido(null);
		uso.setCupon(null);
		assertThat(uso.getIdPedido()).isNull();
		assertThat(uso.getIdCupon()).isNull();
	}

	@Test
	void constructoresAccesoresEqualsHashCodeYToStringDeModelos() {
		LocalDate fechaInicio = LocalDate.of(2026, 6, 1);
		LocalDate fechaVencimiento = LocalDate.of(2026, 6, 30);
		LocalDateTime fecha = LocalDateTime.of(2026, 6, 22, 10, 0);

		CarritoCompra carrito = new CarritoCompra(1L, 2L, fecha, "ABIERTO",
				new BigDecimal("15000"), new ArrayList<>());
		CarritoCompra carritoCopia = new CarritoCompra(1L, 2L, fecha, "ABIERTO",
				new BigDecimal("15000"), new ArrayList<>());

		Cupon cupon = new Cupon(3L, "DESC10", "PORCENTAJE", new BigDecimal("10"),
				new BigDecimal("5000"), fechaInicio, fechaVencimiento, 100, 5, "ACTIVO", new ArrayList<>());
		Cupon cuponCopia = new Cupon(3L, "DESC10", "PORCENTAJE", new BigDecimal("10"),
				new BigDecimal("5000"), fechaInicio, fechaVencimiento, 100, 5, "ACTIVO", new ArrayList<>());

		DetalleCarrito detalleCarrito = new DetalleCarrito(4L, 1L, 5L, 2,
				new BigDecimal("3000"), new BigDecimal("6000"), null);
		DetalleCarrito detalleCarritoCopia = new DetalleCarrito(4L, 1L, 5L, 2,
				new BigDecimal("3000"), new BigDecimal("6000"), null);

		DetallePedido detallePedido = new DetallePedido(6L, 7L, 8L, 3,
				new BigDecimal("4000"), new BigDecimal("1000"), new BigDecimal("11000"), null);
		DetallePedido detallePedidoCopia = new DetallePedido(6L, 7L, 8L, 3,
				new BigDecimal("4000"), new BigDecimal("1000"), new BigDecimal("11000"), null);

		HistorialEstadoPedido historial = new HistorialEstadoPedido(9L, 10L, "CREADO",
				"CONFIRMADO", fecha, "Confirmacion cliente", null);
		HistorialEstadoPedido historialCopia = new HistorialEstadoPedido(9L, 10L, "CREADO",
				"CONFIRMADO", fecha, "Confirmacion cliente", null);

		Pedido pedido = new Pedido(11L, 12L, 13L, 14L, 15L, fecha,
				new BigDecimal("15000"), new BigDecimal("1000"), new BigDecimal("14000"), "CREADO",
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		Pedido pedidoCopia = new Pedido(11L, 12L, 13L, 14L, 15L, fecha,
				new BigDecimal("15000"), new BigDecimal("1000"), new BigDecimal("14000"), "CREADO",
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

		UsoCupon usoCupon = new UsoCupon(16L, 11L, 3L, fecha,
				new BigDecimal("1000"), "APLICADO", null, null);
		UsoCupon usoCuponCopia = new UsoCupon(16L, 11L, 3L, fecha,
				new BigDecimal("1000"), "APLICADO", null, null);

		assertThat(carrito).isEqualTo(carritoCopia).hasSameHashCodeAs(carritoCopia);
		assertThat(carrito.getIdCliente()).isEqualTo(2L);
		assertThat(carrito.toString()).contains("ABIERTO");

		assertThat(cupon).isEqualTo(cuponCopia).hasSameHashCodeAs(cuponCopia);
		assertThat(cupon.getCodigo()).isEqualTo("DESC10");
		assertThat(cupon.toString()).contains("DESC10");

		assertThat(detalleCarrito).isEqualTo(detalleCarritoCopia).hasSameHashCodeAs(detalleCarritoCopia);
		assertThat(detalleCarrito.getIdProducto()).isEqualTo(5L);
		assertThat(detalleCarrito.toString()).contains("6000");

		assertThat(detallePedido).isEqualTo(detallePedidoCopia).hasSameHashCodeAs(detallePedidoCopia);
		assertThat(detallePedido.getIdPedido()).isEqualTo(7L);
		assertThat(detallePedido.toString()).contains("11000");

		assertThat(historial).isEqualTo(historialCopia).hasSameHashCodeAs(historialCopia);
		assertThat(historial.getEstadoNuevo()).isEqualTo("CONFIRMADO");
		assertThat(historial.toString()).contains("Confirmacion cliente");

		assertThat(pedido).isEqualTo(pedidoCopia).hasSameHashCodeAs(pedidoCopia);
		assertThat(pedido.getIdPago()).isEqualTo(15L);
		assertThat(pedido.toString()).contains("CREADO");

		assertThat(usoCupon).isEqualTo(usoCuponCopia).hasSameHashCodeAs(usoCuponCopia);
		assertThat(usoCupon.getDescuentoAplicado()).isEqualByComparingTo("1000");
		assertThat(usoCupon.toString()).contains("APLICADO");
	}

	private DetalleCarrito detalleCarrito(Long idProducto, int cantidad, String precioUnitario) {
		DetalleCarrito detalle = new DetalleCarrito();
		detalle.setIdProducto(idProducto);
		detalle.setCantidad(cantidad);
		detalle.setPrecioUnitario(new BigDecimal(precioUnitario));
		return detalle;
	}

	private Cupon cuponVigente() {
		Cupon cupon = new Cupon();
		cupon.setCodigo("CUPON10");
		cupon.setTipoDescuento("PORCENTAJE");
		cupon.setValorDescuento(new BigDecimal("10"));
		cupon.setMontoMinimo(BigDecimal.ZERO);
		cupon.setFechaInicio(LocalDate.now().minusDays(1));
		cupon.setFechaVencimiento(LocalDate.now().plusDays(1));
		cupon.setLimiteUso(5);
		cupon.setUsosActuales(0);
		cupon.setEstado("ACTIVO");
		cupon.setUsosCupon(new ArrayList<>());
		return cupon;
	}

	private Pedido pedidoBase() {
		Pedido pedido = new Pedido();
		pedido.setIdCliente(1L);
		pedido.setIdSucursal(2L);
		pedido.setIdDireccion(3L);
		pedido.setDetalles(new ArrayList<>());
		pedido.getDetalles().add(detallePedido(100L, 2, "5000", "0"));
		pedido.getDetalles().add(detallePedido(200L, 1, "2000", "500"));
		return pedido;
	}

	private DetallePedido detallePedido(Long idProducto, int cantidad, String precioUnitario, String descuento) {
		DetallePedido detalle = new DetallePedido();
		detalle.setIdProducto(idProducto);
		detalle.setCantidad(cantidad);
		detalle.setPrecioUnitario(new BigDecimal(precioUnitario));
		detalle.setDescuento(descuento == null ? null : new BigDecimal(descuento));
		return detalle;
	}
}
