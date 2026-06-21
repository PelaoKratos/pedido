package microservice.pedido.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import microservice.pedido.model.CarritoCompra;
import microservice.pedido.model.DetalleCarrito;
import microservice.pedido.service.CarritoService;

@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

	private final CarritoService carritoService;

	public CarritoController(CarritoService carritoService) {
		this.carritoService = carritoService;
	}

	@GetMapping
	public List<CarritoCompra> listar() {
		return carritoService.listar();
	}

	@GetMapping("/{id}")
	public CarritoCompra obtenerPorId(@PathVariable Long id) {
		return carritoService.obtenerPorId(id);
	}

	@GetMapping("/cliente/{idCliente}")
	public List<CarritoCompra> buscarPorCliente(@PathVariable Long idCliente) {
		return carritoService.buscarPorCliente(idCliente);
	}

	@GetMapping("/estado/{estado}")
	public List<CarritoCompra> buscarPorEstado(@PathVariable String estado) {
		return carritoService.buscarPorEstado(estado);
	}

	@PostMapping
	public CarritoCompra crear(@Valid @RequestBody CarritoCompra carrito) {
		return carritoService.crear(carrito);
	}

	@PostMapping("/{id}/productos")
	public CarritoCompra agregarProducto(@PathVariable Long id, @Valid @RequestBody DetalleCarrito detalle) {
		return carritoService.agregarProducto(id, detalle);
	}

	@PatchMapping("/{id}/productos/{idProducto}/cantidad")
	public CarritoCompra actualizarCantidad(@PathVariable Long id, @PathVariable Long idProducto,
			@RequestBody Map<String, Integer> request) {
		return carritoService.actualizarCantidad(id, idProducto, request.get("cantidad"));
	}

	@PatchMapping("/{id}/descuento")
	public CarritoCompra aplicarDescuento(@PathVariable Long id, @RequestBody Map<String, BigDecimal> request) {
		return carritoService.aplicarDescuento(id, request.get("descuento"));
	}

	@PatchMapping("/{id}/confirmacion")
	public CarritoCompra confirmarCompra(@PathVariable Long id) {
		return carritoService.confirmarCompra(id);
	}

	@DeleteMapping("/{id}/productos/{idProducto}")
	public CarritoCompra quitarProducto(@PathVariable Long id, @PathVariable Long idProducto) {
		return carritoService.quitarProducto(id, idProducto);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		carritoService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
