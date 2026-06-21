package microservice.pedido.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import microservice.pedido.model.HistorialEstadoPedido;
import microservice.pedido.model.Pedido;
import microservice.pedido.service.PedidoService;

@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {

	@Mock
	private PedidoService pedidoService;

	private PedidoController pedidoController;

	@BeforeEach
	void setUp() {
		pedidoController = new PedidoController(pedidoService);
	}

	@Test
	void endpointsDeleganEnServicio() {
		Pedido pedido = new Pedido();
		List<Pedido> pedidos = List.of(pedido);
		List<HistorialEstadoPedido> historial = List.of(new HistorialEstadoPedido());
		when(pedidoService.listar()).thenReturn(pedidos);
		when(pedidoService.obtenerPorId(1L)).thenReturn(pedido);
		when(pedidoService.buscarPorCliente(10L)).thenReturn(pedidos);
		when(pedidoService.buscarPorEstado("CREADO")).thenReturn(pedidos);
		when(pedidoService.consultarHistorial(1L)).thenReturn(historial);
		when(pedidoService.crear(pedido)).thenReturn(pedido);
		when(pedidoService.actualizar(1L, pedido)).thenReturn(pedido);
		when(pedidoService.cambiarEstado(1L, "PAGADO", "Pago confirmado")).thenReturn(pedido);
		when(pedidoService.confirmarPedido(1L)).thenReturn(pedido);
		when(pedidoService.cancelarPedido(1L)).thenReturn(pedido);

		assertThat(pedidoController.listar()).isEqualTo(pedidos);
		assertThat(pedidoController.obtenerPorId(1L)).isEqualTo(pedido);
		assertThat(pedidoController.buscarPorCliente(10L)).isEqualTo(pedidos);
		assertThat(pedidoController.buscarPorEstado("CREADO")).isEqualTo(pedidos);
		assertThat(pedidoController.consultarHistorial(1L)).isEqualTo(historial);
		assertThat(pedidoController.crear(pedido)).isEqualTo(pedido);
		assertThat(pedidoController.actualizar(1L, pedido)).isEqualTo(pedido);
		assertThat(pedidoController.cambiarEstado(1L, Map.of("estado", "PAGADO", "observacion", "Pago confirmado")))
				.isEqualTo(pedido);
		assertThat(pedidoController.confirmarPedido(1L)).isEqualTo(pedido);
		assertThat(pedidoController.cancelarPedido(1L)).isEqualTo(pedido);
	}

	@Test
	void eliminarRetornaSinContenido() {
		ResponseEntity<Void> respuesta = pedidoController.eliminar(1L);

		verify(pedidoService).eliminar(1L);
		assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}
}
