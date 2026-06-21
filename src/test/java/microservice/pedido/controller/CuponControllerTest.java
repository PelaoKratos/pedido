package microservice.pedido.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import microservice.pedido.model.Cupon;
import microservice.pedido.service.CuponService;

@ExtendWith(MockitoExtension.class)
class CuponControllerTest {

	@Mock
	private CuponService cuponService;

	private CuponController cuponController;

	@BeforeEach
	void setUp() {
		cuponController = new CuponController(cuponService);
	}

	@Test
	void endpointsDeleganEnServicio() {
		Cupon cupon = new Cupon();
		List<Cupon> cupones = List.of(cupon);
		when(cuponService.listar()).thenReturn(cupones);
		when(cuponService.obtenerPorId(1L)).thenReturn(cupon);
		when(cuponService.obtenerPorCodigo("DESC10")).thenReturn(cupon);
		when(cuponService.buscarPorEstado("ACTIVO")).thenReturn(cupones);
		when(cuponService.crear(cupon)).thenReturn(cupon);
		when(cuponService.actualizar(1L, cupon)).thenReturn(cupon);
		when(cuponService.cambiarEstado(1L, "INACTIVO")).thenReturn(cupon);

		assertThat(cuponController.listar()).isEqualTo(cupones);
		assertThat(cuponController.obtenerPorId(1L)).isEqualTo(cupon);
		assertThat(cuponController.obtenerPorCodigo("DESC10")).isEqualTo(cupon);
		assertThat(cuponController.buscarPorEstado("ACTIVO")).isEqualTo(cupones);
		assertThat(cuponController.crear(cupon)).isEqualTo(cupon);
		assertThat(cuponController.actualizar(1L, cupon)).isEqualTo(cupon);
		assertThat(cuponController.cambiarEstado(1L, Map.of("estado", "INACTIVO"))).isEqualTo(cupon);
	}
}
