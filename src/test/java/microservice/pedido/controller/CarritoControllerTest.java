package microservice.pedido.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import microservice.pedido.model.CarritoCompra;
import microservice.pedido.model.DetalleCarrito;
import microservice.pedido.service.CarritoService;

@ExtendWith(MockitoExtension.class)
class CarritoControllerTest {

	@Mock
	private CarritoService carritoService;

	private CarritoController carritoController;

	@BeforeEach
	void setUp() {
		carritoController = new CarritoController(carritoService);
	}

	@Test
	void endpointsDeleganEnServicio() {
		CarritoCompra carrito = new CarritoCompra();
		DetalleCarrito detalle = new DetalleCarrito();
		List<CarritoCompra> carritos = List.of(carrito);
		when(carritoService.listar()).thenReturn(carritos);
		when(carritoService.obtenerPorId(1L)).thenReturn(carrito);
		when(carritoService.buscarPorCliente(10L)).thenReturn(carritos);
		when(carritoService.buscarPorEstado("ACTIVO")).thenReturn(carritos);
		when(carritoService.crear(carrito)).thenReturn(carrito);
		when(carritoService.agregarProducto(1L, detalle)).thenReturn(carrito);
		when(carritoService.actualizarCantidad(1L, 100L, 3)).thenReturn(carrito);
		when(carritoService.aplicarDescuento(1L, new BigDecimal("1000"))).thenReturn(carrito);
		when(carritoService.confirmarCompra(1L)).thenReturn(carrito);
		when(carritoService.quitarProducto(1L, 100L)).thenReturn(carrito);

		assertThat(carritoController.listar()).isEqualTo(carritos);
		assertThat(carritoController.obtenerPorId(1L)).isEqualTo(carrito);
		assertThat(carritoController.buscarPorCliente(10L)).isEqualTo(carritos);
		assertThat(carritoController.buscarPorEstado("ACTIVO")).isEqualTo(carritos);
		assertThat(carritoController.crear(carrito)).isEqualTo(carrito);
		assertThat(carritoController.agregarProducto(1L, detalle)).isEqualTo(carrito);
		assertThat(carritoController.actualizarCantidad(1L, 100L, Map.of("cantidad", 3))).isEqualTo(carrito);
		assertThat(carritoController.aplicarDescuento(1L, Map.of("descuento", new BigDecimal("1000"))))
				.isEqualTo(carrito);
		assertThat(carritoController.confirmarCompra(1L)).isEqualTo(carrito);
		assertThat(carritoController.quitarProducto(1L, 100L)).isEqualTo(carrito);
	}

	@Test
	void eliminarRetornaSinContenido() {
		ResponseEntity<Void> respuesta = carritoController.eliminar(1L);

		verify(carritoService).eliminar(1L);
		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}
}
