package microservice.pedido.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import microservice.pedido.exception.ResourceNotFoundException;
import microservice.pedido.model.Cupon;
import microservice.pedido.repository.CuponRepository;

@Service
public class CuponService {

	static final String ESTADO_ACTIVO = "ACTIVO";
	static final String ESTADO_INACTIVO = "INACTIVO";
	static final String TIPO_MONTO = "MONTO";
	static final String TIPO_PORCENTAJE = "PORCENTAJE";

	private final CuponRepository cuponRepository;

	public CuponService(CuponRepository cuponRepository) {
		this.cuponRepository = cuponRepository;
	}

	public List<Cupon> listar() {
		return cuponRepository.findAll();
	}

	public Cupon obtenerPorId(Long id) {
		return buscarCupon(id);
	}

	public Cupon obtenerPorCodigo(String codigo) {
		return cuponRepository.findByCodigo(codigo)
				.orElseThrow(() -> new ResourceNotFoundException("No existe el cupon con codigo " + codigo));
	}

	public List<Cupon> buscarPorEstado(String estado) {
		return cuponRepository.findByEstado(normalizarEstado(estado));
	}

	public Cupon crear(Cupon cupon) {
		prepararCupon(cupon);
		return cuponRepository.save(cupon);
	}

	public Cupon actualizar(Long id, Cupon datosCupon) {
		Cupon cupon = buscarCupon(id);
		cupon.setCodigo(datosCupon.getCodigo());
		cupon.setTipoDescuento(normalizarTipo(datosCupon.getTipoDescuento()));
		cupon.setValorDescuento(datosCupon.getValorDescuento());
		cupon.setMontoMinimo(datosCupon.getMontoMinimo());
		cupon.setFechaInicio(datosCupon.getFechaInicio());
		cupon.setFechaVencimiento(datosCupon.getFechaVencimiento());
		cupon.setLimiteUso(datosCupon.getLimiteUso());
		cupon.setUsosActuales(datosCupon.getUsosActuales());
		cupon.setEstado(normalizarEstado(datosCupon.getEstado()));
		validarFechas(cupon);
		return cuponRepository.save(cupon);
	}

	public Cupon cambiarEstado(Long id, String estado) {
		Cupon cupon = buscarCupon(id);
		cupon.setEstado(normalizarEstado(estado));
		return cuponRepository.save(cupon);
	}

	public boolean validarCupon(Cupon cupon) {
		LocalDate hoy = LocalDate.now();
		return ESTADO_ACTIVO.equals(cupon.getEstado())
				&& !hoy.isBefore(cupon.getFechaInicio())
				&& !hoy.isAfter(cupon.getFechaVencimiento())
				&& cupon.getUsosActuales() < cupon.getLimiteUso();
	}

	private Cupon buscarCupon(Long id) {
		return cuponRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No existe el cupon con id " + id));
	}

	private void prepararCupon(Cupon cupon) {
		cupon.setTipoDescuento(normalizarTipo(cupon.getTipoDescuento()));
		if (cupon.getEstado() == null || cupon.getEstado().isBlank()) {
			cupon.setEstado(ESTADO_ACTIVO);
		} else {
			cupon.setEstado(normalizarEstado(cupon.getEstado()));
		}
		if (cupon.getUsosActuales() == null) {
			cupon.setUsosActuales(0);
		}
		validarFechas(cupon);
	}

	private void validarFechas(Cupon cupon) {
		if (cupon.getFechaInicio().isAfter(cupon.getFechaVencimiento())) {
			throw new IllegalArgumentException("La fecha de inicio no puede ser posterior al vencimiento");
		}
	}

	private String normalizarEstado(String estado) {
		if (estado == null || estado.isBlank()) {
			throw new IllegalArgumentException("El estado no puede estar vacio");
		}
		String estadoNormalizado = estado.trim().toUpperCase();
		if (!List.of(ESTADO_ACTIVO, ESTADO_INACTIVO).contains(estadoNormalizado)) {
			throw new IllegalArgumentException("Estado de cupon no valido: " + estado);
		}
		return estadoNormalizado;
	}

	private String normalizarTipo(String tipo) {
		if (tipo == null || tipo.isBlank()) {
			throw new IllegalArgumentException("El tipo de descuento no puede estar vacio");
		}
		String tipoNormalizado = tipo.trim().toUpperCase();
		if (!List.of(TIPO_MONTO, TIPO_PORCENTAJE).contains(tipoNormalizado)) {
			throw new IllegalArgumentException("Tipo de descuento no valido: " + tipo);
		}
		return tipoNormalizado;
	}
}
