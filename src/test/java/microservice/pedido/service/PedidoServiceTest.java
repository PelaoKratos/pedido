package microservice.pedido.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import microservice.pedido.exception.ResourceNotFoundException;
import microservice.pedido.model.DetallePedido;
import microservice.pedido.model.HistorialEstadoPedido;
import microservice.pedido.model.Pedido;
import microservice.pedido.repository.HistorialEstadoPedidoRepository;
import microservice.pedido.repository.PedidoRepository;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

	@Mock
	private PedidoRepository pedidoRepository;

	@Mock
	private HistorialEstadoPedidoRepository historialRepository;

	private PedidoService pedidoService;

	@BeforeEach
	void setUp() {
		pedidoService = new PedidoService(pedidoRepository, historialRepository);
	}

	@Test
	void listarRetornaPedidos() {
		List<Pedido> pedidos = List.of(crearPedido(1L));
		when(pedidoRepository.findAll()).thenReturn(pedidos);

		assertThat(pedidoService.listar()).isEqualTo(pedidos);
	}

	@Test
	void obtenerPorIdRetornaPedidoExistente() {
		Pedido pedido = crearPedido(1L);
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

		assertThat(pedidoService.obtenerPorId(1L)).isEqualTo(pedido);
	}

	@Test
	void obtenerPorIdLanzaErrorCuandoNoExiste() {
		when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> pedidoService.obtenerPorId(99L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("No existe el pedido con id 99");
	}

	@Test
	void buscarPorClienteYEstadoDeleganEnRepositorio() {
		List<Pedido> pedidos = List.of(crearPedido(1L));
		when(pedidoRepository.findByIdCliente(10L)).thenReturn(pedidos);
		when(pedidoRepository.findByEstado("CREADO")).thenReturn(pedidos);

		assertThat(pedidoService.buscarPorCliente(10L)).isEqualTo(pedidos);
		assertThat(pedidoService.buscarPorEstado(" creado ")).isEqualTo(pedidos);
	}

	@Test
	void crearCalculaTotalesYRegistraHistorial() {
		Pedido pedido = crearPedido(null);
		pedido.setFechaPedido(null);
		pedido.setEstado(null);
		pedido.setDescuento(new BigDecimal("500"));
		when(pedidoRepository.save(pedido)).thenReturn(pedido);

		Pedido resultado = pedidoService.crear(pedido);

		assertThat(resultado.getFechaPedido()).isNotNull();
		assertThat(resultado.getEstado()).isEqualTo("CREADO");
		assertThat(resultado.getSubtotal()).isEqualByComparingTo("11500");
		assertThat(resultado.getTotal()).isEqualByComparingTo("11000");
		assertThat(resultado.getDetalles()).allSatisfy(detalle -> assertThat(detalle.getPedido()).isEqualTo(resultado));
		assertThat(resultado.getHistorialEstados()).hasSize(1);
		assertThat(resultado.getHistorialEstados().get(0).getEstadoNuevo()).isEqualTo("CREADO");
	}

	@Test
	void crearNormalizaEstadoInformado() {
		Pedido pedido = crearPedido(null);
		pedido.setEstado(" confirmado ");
		when(pedidoRepository.save(pedido)).thenReturn(pedido);

		assertThat(pedidoService.crear(pedido).getEstado()).isEqualTo("CONFIRMADO");
	}

	@Test
	void crearAsignaEstadoCreadoCuandoEstadoVieneEnBlanco() {
		Pedido pedido = crearPedido(null);
		pedido.setEstado(" ");
		when(pedidoRepository.save(pedido)).thenReturn(pedido);

		assertThat(pedidoService.crear(pedido).getEstado()).isEqualTo("CREADO");
	}

	@Test
	void crearUsaDescuentoCeroCuandoDescuentoVieneNulo() {
		Pedido pedido = crearPedido(null);
		pedido.setDescuento(null);
		when(pedidoRepository.save(pedido)).thenReturn(pedido);

		Pedido resultado = pedidoService.crear(pedido);

		assertThat(resultado.getDescuento()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(resultado.getTotal()).isEqualByComparingTo("11500");
	}

	@Test
	void crearRechazaPedidoSinDetalles() {
		Pedido pedido = crearPedido(null);
		pedido.setDetalles(new ArrayList<>());

		assertThatThrownBy(() -> pedidoService.crear(pedido))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El pedido debe tener al menos un detalle");
	}

	@Test
	void crearRechazaPedidoConDetallesNulos() {
		Pedido pedido = crearPedido(null);
		pedido.setDetalles(null);

		assertThatThrownBy(() -> pedidoService.crear(pedido))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El pedido debe tener al menos un detalle");
	}

	@Test
	void crearRechazaSubtotalDetalleNegativo() {
		Pedido pedido = crearPedido(null);
		pedido.getDetalles().get(0).setDescuento(new BigDecimal("999999"));

		assertThatThrownBy(() -> pedidoService.crear(pedido))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El subtotal del detalle no puede ser negativo");
	}

	@Test
	void crearRechazaTotalNegativo() {
		Pedido pedido = crearPedido(null);
		pedido.setDescuento(new BigDecimal("999999"));

		assertThatThrownBy(() -> pedidoService.crear(pedido))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El total del pedido no puede ser negativo");
	}

	@Test
	void actualizarCambiaDatosDetallesTotalesYRegistraHistorialSiCambiaEstado() {
		Pedido existente = crearPedido(1L);
		Pedido datos = crearPedido(null);
		datos.setIdCliente(88L);
		datos.setIdSucursal(77L);
		datos.setIdDireccion(66L);
		datos.setIdPago(55L);
		datos.setFechaPedido(LocalDateTime.of(2026, 7, 1, 10, 0));
		datos.setEstado("pagado");
		datos.setDescuento(BigDecimal.ZERO);
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(pedidoRepository.save(existente)).thenReturn(existente);

		Pedido resultado = pedidoService.actualizar(1L, datos);

		assertThat(resultado.getIdCliente()).isEqualTo(88L);
		assertThat(resultado.getIdSucursal()).isEqualTo(77L);
		assertThat(resultado.getIdDireccion()).isEqualTo(66L);
		assertThat(resultado.getIdPago()).isEqualTo(55L);
		assertThat(resultado.getEstado()).isEqualTo("PAGADO");
		assertThat(resultado.getHistorialEstados()).hasSize(1);
	}

	@Test
	void actualizarNoRegistraHistorialSiEstadoNoCambia() {
		Pedido existente = crearPedido(1L);
		Pedido datos = crearPedido(null);
		datos.setEstado("CREADO");
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(pedidoRepository.save(existente)).thenReturn(existente);

		Pedido resultado = pedidoService.actualizar(1L, datos);

		assertThat(resultado.getHistorialEstados()).isEmpty();
	}

	@Test
	void cambiarEstadoActualizaEstadoEHistorial() {
		Pedido pedido = crearPedido(1L);
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
		when(pedidoRepository.save(pedido)).thenReturn(pedido);

		Pedido resultado = pedidoService.cambiarEstado(1L, "en_preparacion", "Preparando productos");

		assertThat(resultado.getEstado()).isEqualTo("EN_PREPARACION");
		assertThat(resultado.getHistorialEstados()).hasSize(1);
		assertThat(resultado.getHistorialEstados().get(0).getObservacion()).isEqualTo("Preparando productos");
	}

	@Test
	void cambiarEstadoRechazaPedidoCancelado() {
		Pedido pedido = crearPedido(1L);
		pedido.setEstado("CANCELADO");
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

		assertThatThrownBy(() -> pedidoService.cambiarEstado(1L, "CONFIRMADO", null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("No se puede cambiar el estado de un pedido cancelado");
	}

	@Test
	void confirmarPedidoUsaEstadoConfirmado() {
		Pedido pedido = crearPedido(1L);
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
		when(pedidoRepository.save(pedido)).thenReturn(pedido);

		assertThat(pedidoService.confirmarPedido(1L).getEstado()).isEqualTo("CONFIRMADO");
	}

	@Test
	void cancelarPedidoRechazaPedidoPagado() {
		Pedido pedido = crearPedido(1L);
		pedido.setEstado("PAGADO");
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

		assertThatThrownBy(() -> pedidoService.cancelarPedido(1L))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("No se puede cancelar un pedido pagado");
	}

	@Test
	void cancelarPedidoActualizaEstado() {
		Pedido pedido = crearPedido(1L);
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
		when(pedidoRepository.save(pedido)).thenReturn(pedido);

		assertThat(pedidoService.cancelarPedido(1L).getEstado()).isEqualTo("CANCELADO");
	}

	@Test
	void eliminarBorraPedidoExistente() {
		Pedido pedido = crearPedido(1L);
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

		pedidoService.eliminar(1L);

		verify(pedidoRepository).delete(pedido);
	}

	@Test
	void consultarHistorialValidaPedidoYRetornaHistorial() {
		Pedido pedido = crearPedido(1L);
		List<HistorialEstadoPedido> historial = List.of(new HistorialEstadoPedido());
		when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
		when(historialRepository.findByPedidoIdPedido(1L)).thenReturn(historial);

		assertThat(pedidoService.consultarHistorial(1L)).isEqualTo(historial);
	}

	@Test
	void normalizarEstadoRechazaVacioNullONoValido() {
		assertThatThrownBy(() -> pedidoService.buscarPorEstado(" "))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El estado no puede estar vacio");
		assertThatThrownBy(() -> pedidoService.buscarPorEstado(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El estado no puede estar vacio");
		assertThatThrownBy(() -> pedidoService.buscarPorEstado("PERDIDO"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Estado de pedido no valido: PERDIDO");
	}

	private Pedido crearPedido(Long id) {
		Pedido pedido = new Pedido();
		pedido.setIdPedido(id);
		pedido.setIdCliente(10L);
		pedido.setIdSucursal(20L);
		pedido.setIdDireccion(30L);
		pedido.setIdPago(null);
		pedido.setFechaPedido(LocalDateTime.of(2026, 6, 21, 12, 0));
		pedido.setDescuento(BigDecimal.ZERO);
		pedido.setEstado("CREADO");
		pedido.setDetalles(new ArrayList<>(List.of(
				crearDetalle(100L, 2, "5000", "0"),
				crearDetalle(200L, 1, "2000", "500"))));
		pedido.setHistorialEstados(new ArrayList<>());
		return pedido;
	}

	private DetallePedido crearDetalle(Long idProducto, int cantidad, String precio, String descuento) {
		DetallePedido detalle = new DetallePedido();
		detalle.setIdProducto(idProducto);
		detalle.setCantidad(cantidad);
		detalle.setPrecioUnitario(new BigDecimal(precio));
		detalle.setDescuento(new BigDecimal(descuento));
		return detalle;
	}
}
