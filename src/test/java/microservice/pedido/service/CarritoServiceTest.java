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
import microservice.pedido.model.CarritoCompra;
import microservice.pedido.model.DetalleCarrito;
import microservice.pedido.repository.CarritoCompraRepository;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

	@Mock
	private CarritoCompraRepository carritoRepository;

	private CarritoService carritoService;

	@BeforeEach
	void setUp() {
		carritoService = new CarritoService(carritoRepository);
	}

	@Test
	void listarObtenerYBuscarDeleganEnRepositorio() {
		CarritoCompra carrito = crearCarrito(1L);
		List<CarritoCompra> carritos = List.of(carrito);
		when(carritoRepository.findAll()).thenReturn(carritos);
		when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
		when(carritoRepository.findByIdCliente(10L)).thenReturn(carritos);
		when(carritoRepository.findByEstado("ACTIVO")).thenReturn(carritos);

		assertThat(carritoService.listar()).isEqualTo(carritos);
		assertThat(carritoService.obtenerPorId(1L)).isEqualTo(carrito);
		assertThat(carritoService.buscarPorCliente(10L)).isEqualTo(carritos);
		assertThat(carritoService.buscarPorEstado(" activo ")).isEqualTo(carritos);
	}

	@Test
	void obtenerPorIdLanzaErrorSiNoExiste() {
		when(carritoRepository.findById(99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> carritoService.obtenerPorId(99L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("No existe el carrito con id 99");
	}

	@Test
	void crearCompletaFechaEstadoDetallesYSubtotal() {
		CarritoCompra carrito = crearCarrito(null);
		carrito.setFechaCreacion(null);
		carrito.setEstado(null);
		when(carritoRepository.save(carrito)).thenReturn(carrito);

		CarritoCompra resultado = carritoService.crear(carrito);

		assertThat(resultado.getFechaCreacion()).isNotNull();
		assertThat(resultado.getEstado()).isEqualTo("ACTIVO");
		assertThat(resultado.getSubtotal()).isEqualByComparingTo("12000");
		assertThat(resultado.getDetalles()).allSatisfy(detalle -> assertThat(detalle.getCarrito()).isEqualTo(resultado));
	}

	@Test
	void crearNormalizaEstadoYPermiteDetalleNulo() {
		CarritoCompra carrito = crearCarrito(null);
		carrito.setEstado(" confirmado ");
		carrito.setDetalles(null);
		when(carritoRepository.save(carrito)).thenReturn(carrito);

		CarritoCompra resultado = carritoService.crear(carrito);

		assertThat(resultado.getEstado()).isEqualTo("CONFIRMADO");
		assertThat(resultado.getDetalles()).isEmpty();
		assertThat(resultado.getSubtotal()).isEqualByComparingTo("0");
	}

	@Test
	void crearAsignaEstadoActivoCuandoEstadoVieneEnBlanco() {
		CarritoCompra carrito = crearCarrito(null);
		carrito.setEstado(" ");
		when(carritoRepository.save(carrito)).thenReturn(carrito);

		assertThat(carritoService.crear(carrito).getEstado()).isEqualTo("ACTIVO");
	}

	@Test
	void crearRechazaCantidadYPrecioInvalidos() {
		CarritoCompra carritoCantidad = crearCarrito(null);
		carritoCantidad.getDetalles().get(0).setCantidad(0);
		assertThatThrownBy(() -> carritoService.crear(carritoCantidad))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La cantidad debe ser mayor a cero");

		CarritoCompra carritoPrecio = crearCarrito(null);
		carritoPrecio.getDetalles().get(0).setPrecioUnitario(new BigDecimal("-1"));
		assertThatThrownBy(() -> carritoService.crear(carritoPrecio))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El precio unitario no puede ser negativo");

		CarritoCompra carritoCantidadNula = crearCarrito(null);
		carritoCantidadNula.getDetalles().get(0).setCantidad(null);
		assertThatThrownBy(() -> carritoService.crear(carritoCantidadNula))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La cantidad debe ser mayor a cero");

		CarritoCompra carritoPrecioNulo = crearCarrito(null);
		carritoPrecioNulo.getDetalles().get(0).setPrecioUnitario(null);
		assertThatThrownBy(() -> carritoService.crear(carritoPrecioNulo))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El precio unitario no puede ser negativo");
	}

	@Test
	void agregarProductoActualizaSubtotal() {
		CarritoCompra carrito = crearCarrito(1L);
		DetalleCarrito detalle = crearDetalle(300L, 1, "2500");
		when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
		when(carritoRepository.save(carrito)).thenReturn(carrito);

		CarritoCompra resultado = carritoService.agregarProducto(1L, detalle);

		assertThat(resultado.getDetalles()).hasSize(3);
		assertThat(resultado.getSubtotal()).isEqualByComparingTo("14500");
		assertThat(detalle.getCarrito()).isEqualTo(carrito);
	}

	@Test
	void quitarProductoActualizaSubtotal() {
		CarritoCompra carrito = crearCarrito(1L);
		when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
		when(carritoRepository.save(carrito)).thenReturn(carrito);

		CarritoCompra resultado = carritoService.quitarProducto(1L, 100L);

		assertThat(resultado.getDetalles()).extracting(DetalleCarrito::getIdProducto).doesNotContain(100L);
		assertThat(resultado.getSubtotal()).isEqualByComparingTo("2000");
	}

	@Test
	void actualizarCantidadModificaDetalleYSubtotal() {
		CarritoCompra carrito = crearCarrito(1L);
		when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
		when(carritoRepository.save(carrito)).thenReturn(carrito);

		CarritoCompra resultado = carritoService.actualizarCantidad(1L, 200L, 3);

		assertThat(resultado.getDetalles().get(1).getCantidad()).isEqualTo(3);
		assertThat(resultado.getSubtotal()).isEqualByComparingTo("16000");
	}

	@Test
	void actualizarCantidadRechazaCantidadInvalida() {
		CarritoCompra carrito = crearCarrito(1L);
		when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

		assertThatThrownBy(() -> carritoService.actualizarCantidad(1L, 100L, null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La cantidad debe ser mayor a cero");
		assertThatThrownBy(() -> carritoService.actualizarCantidad(1L, 100L, 0))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La cantidad debe ser mayor a cero");
	}

	@Test
	void aplicarDescuentoActualizaSubtotalYNoPermiteNegativo() {
		CarritoCompra carrito = crearCarrito(1L);
		carrito.calcularSubtotal();
		when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
		when(carritoRepository.save(carrito)).thenReturn(carrito);

		CarritoCompra resultado = carritoService.aplicarDescuento(1L, new BigDecimal("2000"));

		assertThat(resultado.getSubtotal()).isEqualByComparingTo("10000");
		assertThat(carritoService.aplicarDescuento(1L, null).getSubtotal()).isEqualByComparingTo("12000");
		assertThat(carritoService.aplicarDescuento(1L, new BigDecimal("999999")).getSubtotal()).isEqualByComparingTo("0");
		assertThatThrownBy(() -> carritoService.aplicarDescuento(1L, new BigDecimal("-1")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El descuento no puede ser negativo");
	}

	@Test
	void confirmarCompraActualizaEstado() {
		CarritoCompra carrito = crearCarrito(1L);
		when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));
		when(carritoRepository.save(carrito)).thenReturn(carrito);

		assertThat(carritoService.confirmarCompra(1L).getEstado()).isEqualTo("CONFIRMADO");
	}

	@Test
	void confirmarCompraRechazaCarritoVacio() {
		CarritoCompra carrito = crearCarrito(1L);
		carrito.setDetalles(new ArrayList<>());
		when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

		assertThatThrownBy(() -> carritoService.confirmarCompra(1L))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El carrito debe tener al menos un producto");
	}

	@Test
	void modificarCarritoNoActivoYProductoInexistenteLanzaError() {
		CarritoCompra confirmado = crearCarrito(1L);
		confirmado.setEstado("CONFIRMADO");
		when(carritoRepository.findById(1L)).thenReturn(Optional.of(confirmado));

		assertThatThrownBy(() -> carritoService.agregarProducto(1L, crearDetalle(300L, 1, "1000")))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Solo se puede modificar un carrito activo");

		CarritoCompra activo = crearCarrito(2L);
		when(carritoRepository.findById(2L)).thenReturn(Optional.of(activo));
		assertThatThrownBy(() -> carritoService.quitarProducto(2L, 999L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("No existe el producto en el carrito");
	}

	@Test
	void normalizarEstadoRechazaVacioONoValido() {
		assertThatThrownBy(() -> carritoService.buscarPorEstado(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El estado no puede estar vacio");
		assertThatThrownBy(() -> carritoService.buscarPorEstado(" "))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El estado no puede estar vacio");
		assertThatThrownBy(() -> carritoService.buscarPorEstado("CERRADO"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Estado de carrito no valido: CERRADO");
	}

	@Test
	void eliminarBorraCarritoExistente() {
		CarritoCompra carrito = crearCarrito(1L);
		when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

		carritoService.eliminar(1L);

		verify(carritoRepository).delete(carrito);
	}

	private CarritoCompra crearCarrito(Long id) {
		CarritoCompra carrito = new CarritoCompra();
		carrito.setIdCarrito(id);
		carrito.setIdCliente(10L);
		carrito.setFechaCreacion(LocalDateTime.of(2026, 6, 21, 12, 0));
		carrito.setEstado("ACTIVO");
		carrito.setSubtotal(BigDecimal.ZERO);
		carrito.setDetalles(new ArrayList<>(List.of(
				crearDetalle(100L, 2, "5000"),
				crearDetalle(200L, 1, "2000"))));
		return carrito;
	}

	private DetalleCarrito crearDetalle(Long idProducto, int cantidad, String precioUnitario) {
		DetalleCarrito detalle = new DetalleCarrito();
		detalle.setIdProducto(idProducto);
		detalle.setCantidad(cantidad);
		detalle.setPrecioUnitario(new BigDecimal(precioUnitario));
		return detalle;
	}
}
