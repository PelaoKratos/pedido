package microservice.pedido.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import microservice.pedido.exception.ResourceNotFoundException;
import microservice.pedido.model.CarritoCompra;
import microservice.pedido.model.DetalleCarrito;
import microservice.pedido.repository.CarritoCompraRepository;

@Service
public class CarritoService {

	private static final String ESTADO_ACTIVO = "ACTIVO";
	private static final String ESTADO_CONFIRMADO = "CONFIRMADO";

	private final CarritoCompraRepository carritoRepository;

	public CarritoService(CarritoCompraRepository carritoRepository) {
		this.carritoRepository = carritoRepository;
	}

	public List<CarritoCompra> listar() {
		return carritoRepository.findAll();
	}

	public CarritoCompra obtenerPorId(Long id) {
		return buscarCarrito(id);
	}

	public List<CarritoCompra> buscarPorCliente(Long idCliente) {
		return carritoRepository.findByIdCliente(idCliente);
	}

	public List<CarritoCompra> buscarPorEstado(String estado) {
		return carritoRepository.findByEstado(normalizarEstado(estado));
	}

	@Transactional
	public CarritoCompra crear(CarritoCompra carrito) {
		if (carrito.getFechaCreacion() == null) {
			carrito.setFechaCreacion(LocalDateTime.now());
		}
		if (carrito.getEstado() == null || carrito.getEstado().isBlank()) {
			carrito.setEstado(ESTADO_ACTIVO);
		} else {
			carrito.setEstado(normalizarEstado(carrito.getEstado()));
		}
		prepararDetalles(carrito);
		carrito.calcularSubtotal();
		return carritoRepository.save(carrito);
	}

	@Transactional
	public CarritoCompra agregarProducto(Long idCarrito, DetalleCarrito detalle) {
		CarritoCompra carrito = buscarCarritoActivo(idCarrito);
		validarDetalle(detalle);
		carrito.agregarProducto(detalle);
		return carritoRepository.save(carrito);
	}

	@Transactional
	public CarritoCompra quitarProducto(Long idCarrito, Long idProducto) {
		CarritoCompra carrito = buscarCarritoActivo(idCarrito);
		DetalleCarrito detalle = buscarDetalle(carrito, idProducto);
		carrito.getDetalles().remove(detalle);
		carrito.calcularSubtotal();
		return carritoRepository.save(carrito);
	}

	@Transactional
	public CarritoCompra actualizarCantidad(Long idCarrito, Long idProducto, Integer cantidad) {
		CarritoCompra carrito = buscarCarritoActivo(idCarrito);
		if (cantidad == null || cantidad <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
		}
		DetalleCarrito detalle = buscarDetalle(carrito, idProducto);
		detalle.actualizarCantidad(cantidad);
		carrito.calcularSubtotal();
		return carritoRepository.save(carrito);
	}

	@Transactional
	public CarritoCompra aplicarDescuento(Long idCarrito, BigDecimal descuento) {
		CarritoCompra carrito = buscarCarritoActivo(idCarrito);
		if (descuento != null && descuento.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("El descuento no puede ser negativo");
		}
		carrito.aplicarDescuento(descuento);
		return carritoRepository.save(carrito);
	}

	@Transactional
	public CarritoCompra confirmarCompra(Long idCarrito) {
		CarritoCompra carrito = buscarCarritoActivo(idCarrito);
		if (carrito.getDetalles().isEmpty()) {
			throw new IllegalArgumentException("El carrito debe tener al menos un producto");
		}
		carrito.confirmarCompra();
		return carritoRepository.save(carrito);
	}

	public void eliminar(Long id) {
		CarritoCompra carrito = buscarCarrito(id);
		carritoRepository.delete(carrito);
	}

	private CarritoCompra buscarCarrito(Long id) {
		return carritoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No existe el carrito con id " + id));
	}

	private CarritoCompra buscarCarritoActivo(Long id) {
		CarritoCompra carrito = buscarCarrito(id);
		if (!ESTADO_ACTIVO.equals(carrito.getEstado())) {
			throw new IllegalArgumentException("Solo se puede modificar un carrito activo");
		}
		return carrito;
	}

	private void prepararDetalles(CarritoCompra carrito) {
		if (carrito.getDetalles() == null) {
			carrito.setDetalles(new ArrayList<>());
		}
		for (DetalleCarrito detalle : carrito.getDetalles()) {
			validarDetalle(detalle);
			detalle.setCarrito(carrito);
			detalle.calcularSubtotal();
		}
	}

	private void validarDetalle(DetalleCarrito detalle) {
		if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
		}
		if (detalle.getPrecioUnitario() == null || detalle.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("El precio unitario no puede ser negativo");
		}
	}

	private DetalleCarrito buscarDetalle(CarritoCompra carrito, Long idProducto) {
		return carrito.getDetalles().stream()
				.filter(detalle -> idProducto.equals(detalle.getIdProducto()))
				.findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("No existe el producto en el carrito"));
	}

	private String normalizarEstado(String estado) {
		if (estado == null || estado.isBlank()) {
			throw new IllegalArgumentException("El estado no puede estar vacio");
		}
		String estadoNormalizado = estado.trim().toUpperCase();
		if (!List.of(ESTADO_ACTIVO, ESTADO_CONFIRMADO).contains(estadoNormalizado)) {
			throw new IllegalArgumentException("Estado de carrito no valido: " + estado);
		}
		return estadoNormalizado;
	}
}
