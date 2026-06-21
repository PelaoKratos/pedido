package microservice.pedido.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import microservice.pedido.exception.ResourceNotFoundException;
import microservice.pedido.model.DetallePedido;
import microservice.pedido.model.HistorialEstadoPedido;
import microservice.pedido.model.Pedido;
import microservice.pedido.model.Cupon;
import microservice.pedido.model.UsoCupon;
import microservice.pedido.repository.CuponRepository;
import microservice.pedido.repository.HistorialEstadoPedidoRepository;
import microservice.pedido.repository.PedidoRepository;
import microservice.pedido.repository.UsoCuponRepository;

@Service
public class PedidoService {

	private static final String ESTADO_CREADO = "CREADO";
	private static final String ESTADO_CONFIRMADO = "CONFIRMADO";
	private static final String ESTADO_CANCELADO = "CANCELADO";
	private static final String ESTADO_PAGADO = "PAGADO";
	private static final String ESTADO_EN_PREPARACION = "EN_PREPARACION";

	private final PedidoRepository pedidoRepository;
	private final HistorialEstadoPedidoRepository historialRepository;
	private final CuponRepository cuponRepository;
	private final UsoCuponRepository usoCuponRepository;

	public PedidoService(PedidoRepository pedidoRepository, HistorialEstadoPedidoRepository historialRepository,
			CuponRepository cuponRepository, UsoCuponRepository usoCuponRepository) {
		this.pedidoRepository = pedidoRepository;
		this.historialRepository = historialRepository;
		this.cuponRepository = cuponRepository;
		this.usoCuponRepository = usoCuponRepository;
	}

	public List<Pedido> listar() {
		return pedidoRepository.findAll();
	}

	public Pedido obtenerPorId(Long id) {
		return buscarPedido(id);
	}

	public List<Pedido> buscarPorCliente(Long idCliente) {
		return pedidoRepository.findByIdCliente(idCliente);
	}

	public List<Pedido> buscarPorEstado(String estado) {
		return pedidoRepository.findByEstado(normalizarEstado(estado));
	}

	@Transactional
	public Pedido crear(Pedido pedido) {
		if (pedido.getFechaPedido() == null) {
			pedido.setFechaPedido(LocalDateTime.now());
		}
		if (pedido.getEstado() == null || pedido.getEstado().isBlank()) {
			pedido.setEstado(ESTADO_CREADO);
		} else {
			pedido.setEstado(normalizarEstado(pedido.getEstado()));
		}
		prepararDetalles(pedido);
		calcularTotales(pedido);
		registrarHistorial(pedido, null, pedido.getEstado(), "Pedido creado");
		return pedidoRepository.save(pedido);
	}

	@Transactional
	public Pedido actualizar(Long id, Pedido datosPedido) {
		Pedido pedido = buscarPedido(id);
		pedido.setIdCliente(datosPedido.getIdCliente());
		pedido.setIdSucursal(datosPedido.getIdSucursal());
		pedido.setIdDireccion(datosPedido.getIdDireccion());
		pedido.setIdPago(datosPedido.getIdPago());
		pedido.setFechaPedido(datosPedido.getFechaPedido());
		String estadoAnterior = pedido.getEstado();
		pedido.setEstado(normalizarEstado(datosPedido.getEstado()));
		pedido.getDetalles().clear();
		pedido.getDetalles().addAll(datosPedido.getDetalles());
		prepararDetalles(pedido);
		calcularTotales(pedido);
		if (!estadoAnterior.equals(pedido.getEstado())) {
			registrarHistorial(pedido, estadoAnterior, pedido.getEstado(), "Pedido actualizado");
		}
		return pedidoRepository.save(pedido);
	}

	@Transactional
	public Pedido cambiarEstado(Long id, String estado, String observacion) {
		Pedido pedido = buscarPedido(id);
		String estadoAnterior = pedido.getEstado();
		String estadoNuevo = normalizarEstado(estado);
		if (ESTADO_CANCELADO.equals(estadoAnterior)) {
			throw new IllegalArgumentException("No se puede cambiar el estado de un pedido cancelado");
		}
		pedido.setEstado(estadoNuevo);
		registrarHistorial(pedido, estadoAnterior, estadoNuevo, observacion);
		return pedidoRepository.save(pedido);
	}

	public Pedido confirmarPedido(Long id) {
		return cambiarEstado(id, ESTADO_CONFIRMADO, "Pedido confirmado");
	}

	public Pedido cancelarPedido(Long id) {
		Pedido pedido = buscarPedido(id);
		if (ESTADO_PAGADO.equals(pedido.getEstado())) {
			throw new IllegalArgumentException("No se puede cancelar un pedido pagado");
		}
		String estadoAnterior = pedido.getEstado();
		pedido.setEstado(ESTADO_CANCELADO);
		registrarHistorial(pedido, estadoAnterior, ESTADO_CANCELADO, "Pedido cancelado");
		return pedidoRepository.save(pedido);
	}

	public void eliminar(Long id) {
		Pedido pedido = buscarPedido(id);
		pedidoRepository.delete(pedido);
	}

	public List<HistorialEstadoPedido> consultarHistorial(Long idPedido) {
		buscarPedido(idPedido);
		return historialRepository.findByPedidoIdPedido(idPedido);
	}

	@Transactional
	public Pedido aplicarCupon(Long idPedido, Long idCupon) {
		Pedido pedido = buscarPedido(idPedido);
		Cupon cupon = cuponRepository.findById(idCupon)
				.orElseThrow(() -> new ResourceNotFoundException("No existe el cupon con id " + idCupon));
		prepararDetalles(pedido);
		calcularTotales(pedido);
		validarCuponAplicable(pedido, cupon);
		BigDecimal descuentoAplicado = calcularDescuentoCupon(pedido, cupon);
		UsoCupon usoCupon = new UsoCupon();
		usoCupon.setPedido(pedido);
		usoCupon.setCupon(cupon);
		usoCupon.setFechaUso(LocalDateTime.now());
		usoCupon.setDescuentoAplicado(descuentoAplicado);
		usoCupon.setEstado("APLICADO");
		pedido.getUsosCupon().add(usoCupon);
		cupon.setUsosActuales(cupon.getUsosActuales() + 1);
		pedido.setDescuento(pedido.getDescuento().add(descuentoAplicado));
		calcularTotales(pedido);
		return pedidoRepository.save(pedido);
	}

	@Transactional
	public Pedido anularUsoCupon(Long idPedido, Long idUsoCupon) {
		Pedido pedido = buscarPedido(idPedido);
		UsoCupon usoCupon = usoCuponRepository.findById(idUsoCupon)
				.orElseThrow(() -> new ResourceNotFoundException("No existe el uso de cupon con id " + idUsoCupon));
		if (!pedido.getIdPedido().equals(usoCupon.getPedido().getIdPedido())) {
			throw new IllegalArgumentException("El uso de cupon no pertenece al pedido");
		}
		if (!"APLICADO".equals(usoCupon.getEstado())) {
			throw new IllegalArgumentException("El uso de cupon ya esta anulado");
		}
		usoCupon.setEstado("ANULADO");
		usoCupon.getCupon().setUsosActuales(usoCupon.getCupon().getUsosActuales() - 1);
		pedido.setDescuento(pedido.getDescuento().subtract(usoCupon.getDescuentoAplicado()));
		prepararDetalles(pedido);
		calcularTotales(pedido);
		return pedidoRepository.save(pedido);
	}

	private Pedido buscarPedido(Long id) {
		return pedidoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No existe el pedido con id " + id));
	}

	private void prepararDetalles(Pedido pedido) {
		if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
			throw new IllegalArgumentException("El pedido debe tener al menos un detalle");
		}
		for (DetallePedido detalle : pedido.getDetalles()) {
			detalle.setPedido(pedido);
			detalle.setSubtotal(detalle.calcularSubtotal());
			if (detalle.getSubtotal().compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException("El subtotal del detalle no puede ser negativo");
			}
		}
	}

	private void calcularTotales(Pedido pedido) {
		BigDecimal subtotal = pedido.getDetalles().stream()
				.map(DetallePedido::getSubtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal descuento = pedido.getDescuento() == null ? BigDecimal.ZERO : pedido.getDescuento();
		BigDecimal total = subtotal.subtract(descuento);
		if (total.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("El total del pedido no puede ser negativo");
		}
		pedido.setSubtotal(subtotal);
		pedido.setDescuento(descuento);
		pedido.setTotal(total);
	}

	private void validarCuponAplicable(Pedido pedido, Cupon cupon) {
		if (!"ACTIVO".equals(cupon.getEstado())) {
			throw new IllegalArgumentException("El cupon no esta activo");
		}
		if (LocalDateTime.now().toLocalDate().isBefore(cupon.getFechaInicio())
				|| LocalDateTime.now().toLocalDate().isAfter(cupon.getFechaVencimiento())) {
			throw new IllegalArgumentException("El cupon no esta vigente");
		}
		if (cupon.getUsosActuales() >= cupon.getLimiteUso()) {
			throw new IllegalArgumentException("El cupon alcanzo su limite de uso");
		}
		if (pedido.getSubtotal().compareTo(cupon.getMontoMinimo()) < 0) {
			throw new IllegalArgumentException("El pedido no cumple el monto minimo del cupon");
		}
	}

	private BigDecimal calcularDescuentoCupon(Pedido pedido, Cupon cupon) {
		if ("PORCENTAJE".equals(cupon.getTipoDescuento())) {
			return pedido.getSubtotal()
					.multiply(cupon.getValorDescuento())
					.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		}
		return cupon.getValorDescuento();
	}

	private void registrarHistorial(Pedido pedido, String estadoAnterior, String estadoNuevo, String observacion) {
		HistorialEstadoPedido historial = new HistorialEstadoPedido();
		historial.setPedido(pedido);
		historial.setEstadoAnterior(estadoAnterior);
		historial.setEstadoNuevo(estadoNuevo);
		historial.setFechaCambio(LocalDateTime.now());
		historial.setObservacion(observacion);
		pedido.getHistorialEstados().add(historial);
	}

	private String normalizarEstado(String estado) {
		if (estado == null || estado.isBlank()) {
			throw new IllegalArgumentException("El estado no puede estar vacio");
		}
		String estadoNormalizado = estado.trim().toUpperCase();
		if (!List.of(ESTADO_CREADO, ESTADO_CONFIRMADO, ESTADO_CANCELADO, ESTADO_PAGADO, ESTADO_EN_PREPARACION)
				.contains(estadoNormalizado)) {
			throw new IllegalArgumentException("Estado de pedido no valido: " + estado);
		}
		return estadoNormalizado;
	}
}
