package microservice.pedido.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import microservice.pedido.exception.ResourceNotFoundException;

@Component
public class MicroserviceValidationClient {

	private final RestTemplate restTemplate;
	private final String clienteUrl;
	private final String sucursalUrl;
	private final String pagoUrl;
	private final String productoUrl;

	public MicroserviceValidationClient(
			RestTemplate restTemplate,
			@Value("${microservices.cliente.url:http://localhost:8082/api/v1/clientes}") String clienteUrl,
			@Value("${microservices.sucursal.url:http://localhost:8083/api/v1/sucursales}") String sucursalUrl,
			@Value("${microservices.pago.url:http://localhost:8087/api/v1/pagos}") String pagoUrl,
			@Value("${microservices.producto.url:http://localhost:8084/api/v1/productos}") String productoUrl) {
		this.restTemplate = restTemplate;
		this.clienteUrl = clienteUrl;
		this.sucursalUrl = sucursalUrl;
		this.pagoUrl = pagoUrl;
		this.productoUrl = productoUrl;
	}

	public void validarCliente(Long idCliente) {
		validarExiste(clienteUrl, idCliente, "Cliente");
	}

	public void validarSucursal(Long idSucursal) {
		validarExiste(sucursalUrl, idSucursal, "Sucursal");
	}

	public void validarPago(Long idPago) {
		if (idPago != null) {
			validarExiste(pagoUrl, idPago, "Pago");
		}
	}

	public void validarProducto(Long idProducto) {
		validarExiste(productoUrl, idProducto, "Producto");
	}

	private void validarExiste(String baseUrl, Long id, String recurso) {
		try {
			restTemplate.getForEntity(baseUrl + "/" + id, String.class);
		} catch (HttpClientErrorException.NotFound exception) {
			throw new ResourceNotFoundException(recurso + " no encontrado con id: " + id);
		} catch (RestClientException exception) {
			throw new IllegalStateException("No se pudo validar " + recurso.toLowerCase() + " con id: " + id, exception);
		}
	}
}
