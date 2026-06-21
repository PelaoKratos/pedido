package microservice.pedido.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import microservice.pedido.exception.ResourceNotFoundException;
import microservice.pedido.model.Cupon;
import microservice.pedido.repository.CuponRepository;

@ExtendWith(MockitoExtension.class)
class CuponServiceTest {

	@Mock
	private CuponRepository cuponRepository;

	private CuponService cuponService;

	@BeforeEach
	void setUp() {
		cuponService = new CuponService(cuponRepository);
	}

	@Test
	void listarObtenerYBuscarDeleganEnRepositorio() {
		Cupon cupon = crearCupon(1L);
		when(cuponRepository.findAll()).thenReturn(List.of(cupon));
		when(cuponRepository.findById(1L)).thenReturn(Optional.of(cupon));
		when(cuponRepository.findByCodigo("DESC10")).thenReturn(Optional.of(cupon));
		when(cuponRepository.findByEstado("ACTIVO")).thenReturn(List.of(cupon));

		assertThat(cuponService.listar()).containsExactly(cupon);
		assertThat(cuponService.obtenerPorId(1L)).isEqualTo(cupon);
		assertThat(cuponService.obtenerPorCodigo("DESC10")).isEqualTo(cupon);
		assertThat(cuponService.buscarPorEstado(" activo ")).containsExactly(cupon);
	}

	@Test
	void obtenerLanzaErrorCuandoNoExiste() {
		when(cuponRepository.findById(99L)).thenReturn(Optional.empty());
		when(cuponRepository.findByCodigo("NOPE")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> cuponService.obtenerPorId(99L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("No existe el cupon con id 99");
		assertThatThrownBy(() -> cuponService.obtenerPorCodigo("NOPE"))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("No existe el cupon con codigo NOPE");
	}

	@Test
	void crearPreparaCupon() {
		Cupon cupon = crearCupon(null);
		cupon.setEstado(null);
		cupon.setTipoDescuento(" porcentaje ");
		cupon.setUsosActuales(null);
		when(cuponRepository.save(cupon)).thenReturn(cupon);

		Cupon resultado = cuponService.crear(cupon);

		assertThat(resultado.getEstado()).isEqualTo("ACTIVO");
		assertThat(resultado.getTipoDescuento()).isEqualTo("PORCENTAJE");
		assertThat(resultado.getUsosActuales()).isZero();
	}

	@Test
	void crearAsignaEstadoActivoCuandoEstadoVieneEnBlanco() {
		Cupon cupon = crearCupon(null);
		cupon.setEstado(" ");
		when(cuponRepository.save(cupon)).thenReturn(cupon);

		assertThat(cuponService.crear(cupon).getEstado()).isEqualTo("ACTIVO");
	}

	@Test
	void actualizarYCambiarEstadoModificanCupon() {
		Cupon existente = crearCupon(1L);
		Cupon datos = crearCupon(2L);
		datos.setCodigo("NUEVO");
		datos.setTipoDescuento("monto");
		datos.setEstado("inactivo");
		when(cuponRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(cuponRepository.save(existente)).thenReturn(existente);

		assertThat(cuponService.actualizar(1L, datos).getCodigo()).isEqualTo("NUEVO");
		assertThat(cuponService.cambiarEstado(1L, "ACTIVO").getEstado()).isEqualTo("ACTIVO");
	}

	@Test
	void validarCuponRetornaSegunReglas() {
		Cupon cupon = crearCupon(1L);
		assertThat(cuponService.validarCupon(cupon)).isTrue();

		cupon.setEstado("INACTIVO");
		assertThat(cuponService.validarCupon(cupon)).isFalse();

		cupon = crearCupon(1L);
		cupon.setFechaInicio(LocalDate.now().plusDays(1));
		assertThat(cuponService.validarCupon(cupon)).isFalse();

		cupon = crearCupon(1L);
		cupon.setFechaVencimiento(LocalDate.now().minusDays(1));
		assertThat(cuponService.validarCupon(cupon)).isFalse();

		cupon = crearCupon(1L);
		cupon.setUsosActuales(10);
		cupon.setLimiteUso(10);
		assertThat(cuponService.validarCupon(cupon)).isFalse();
	}

	@Test
	void validacionesLanzanErrores() {
		Cupon cupon = crearCupon(1L);
		cupon.setFechaInicio(LocalDate.now().plusDays(2));
		cupon.setFechaVencimiento(LocalDate.now().plusDays(1));
		assertThatThrownBy(() -> cuponService.crear(cupon))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("La fecha de inicio no puede ser posterior al vencimiento");

		assertThatThrownBy(() -> cuponService.buscarPorEstado(" "))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El estado no puede estar vacio");
		assertThatThrownBy(() -> cuponService.buscarPorEstado(null))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El estado no puede estar vacio");
		assertThatThrownBy(() -> cuponService.buscarPorEstado("BORRADO"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Estado de cupon no valido: BORRADO");

		Cupon tipoVacio = crearCupon(1L);
		tipoVacio.setTipoDescuento(" ");
		assertThatThrownBy(() -> cuponService.crear(tipoVacio))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El tipo de descuento no puede estar vacio");

		Cupon tipoNulo = crearCupon(1L);
		tipoNulo.setTipoDescuento(null);
		assertThatThrownBy(() -> cuponService.crear(tipoNulo))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("El tipo de descuento no puede estar vacio");

		Cupon tipoInvalido = crearCupon(1L);
		tipoInvalido.setTipoDescuento("REGALO");
		assertThatThrownBy(() -> cuponService.crear(tipoInvalido))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Tipo de descuento no valido: REGALO");
	}

	private Cupon crearCupon(Long id) {
		Cupon cupon = new Cupon();
		cupon.setIdCupon(id);
		cupon.setCodigo("DESC10");
		cupon.setTipoDescuento("PORCENTAJE");
		cupon.setValorDescuento(new BigDecimal("10"));
		cupon.setMontoMinimo(BigDecimal.ZERO);
		cupon.setFechaInicio(LocalDate.now().minusDays(1));
		cupon.setFechaVencimiento(LocalDate.now().plusDays(1));
		cupon.setLimiteUso(10);
		cupon.setUsosActuales(0);
		cupon.setEstado("ACTIVO");
		return cupon;
	}
}
