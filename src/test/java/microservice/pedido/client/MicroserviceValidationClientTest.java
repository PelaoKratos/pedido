package microservice.pedido.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import microservice.pedido.exception.ResourceNotFoundException;

@MockitoSettings(strictness = Strictness.LENIENT)
class MicroserviceValidationClientTest {

	@Mock
	private RestTemplate restTemplate;

	private MicroserviceValidationClient client;

	@BeforeEach
	void setUp() {
		restTemplate = org.mockito.Mockito.mock(RestTemplate.class);
		client = new MicroserviceValidationClient(
				restTemplate,
				"http://cliente/api",
				"http://sucursal/api",
				"http://pago/api",
				"http://producto/api");
	}

	@Test
	void validaReferenciasExistentes() {
		when(restTemplate.getForEntity("http://cliente/api/1", String.class)).thenReturn(ResponseEntity.ok("{}"));
		when(restTemplate.getForEntity("http://sucursal/api/2", String.class)).thenReturn(ResponseEntity.ok("{}"));
		when(restTemplate.getForEntity("http://pago/api/3", String.class)).thenReturn(ResponseEntity.ok("{}"));
		when(restTemplate.getForEntity("http://producto/api/4", String.class)).thenReturn(ResponseEntity.ok("{}"));

		assertThatCode(() -> client.validarCliente(1L)).doesNotThrowAnyException();
		assertThatCode(() -> client.validarSucursal(2L)).doesNotThrowAnyException();
		assertThatCode(() -> client.validarPago(3L)).doesNotThrowAnyException();
		assertThatCode(() -> client.validarProducto(4L)).doesNotThrowAnyException();
	}

	@Test
	void noValidaPagoCuandoIdEsNulo() {
		client.validarPago(null);

		verify(restTemplate, never()).getForEntity("http://pago/api/null", String.class);
	}

	@Test
	void lanzaResourceNotFoundCuandoServicioRetorna404() {
		when(restTemplate.getForEntity("http://cliente/api/99", String.class))
				.thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "No encontrado",
						HttpHeaders.EMPTY, null, null));

		assertThatThrownBy(() -> client.validarCliente(99L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Cliente no encontrado con id: 99");
	}

	@Test
	void lanzaIllegalStateCuandoNoPuedeConectar() {
		when(restTemplate.getForEntity("http://producto/api/7", String.class))
				.thenThrow(new RestClientException("conexion rechazada"));

		assertThatThrownBy(() -> client.validarProducto(7L))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("No se pudo validar producto con id: 7")
				.hasCauseInstanceOf(RestClientException.class);
	}

	@Test
	void restTemplateConfigExponeBean() {
		assertThat(new microservice.pedido.config.RestTemplateConfig().restTemplate()).isInstanceOf(RestTemplate.class);
	}
}
