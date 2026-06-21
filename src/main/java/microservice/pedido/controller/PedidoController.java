package microservice.pedido.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import microservice.pedido.model.HistorialEstadoPedido;
import microservice.pedido.model.Pedido;
import microservice.pedido.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

	private final PedidoService pedidoService;

	public PedidoController(PedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	@GetMapping
	public List<Pedido> listar() {
		return pedidoService.listar();
	}

	@GetMapping("/{id}")
	public Pedido obtenerPorId(@PathVariable Long id) {
		return pedidoService.obtenerPorId(id);
	}

	@GetMapping("/cliente/{idCliente}")
	public List<Pedido> buscarPorCliente(@PathVariable Long idCliente) {
		return pedidoService.buscarPorCliente(idCliente);
	}

	@GetMapping("/estado/{estado}")
	public List<Pedido> buscarPorEstado(@PathVariable String estado) {
		return pedidoService.buscarPorEstado(estado);
	}

	@GetMapping("/{id}/historial")
	public List<HistorialEstadoPedido> consultarHistorial(@PathVariable Long id) {
		return pedidoService.consultarHistorial(id);
	}

	@PostMapping
	public Pedido crear(@Valid @RequestBody Pedido pedido) {
		return pedidoService.crear(pedido);
	}

	@PutMapping("/{id}")
	public Pedido actualizar(@PathVariable Long id, @Valid @RequestBody Pedido pedido) {
		return pedidoService.actualizar(id, pedido);
	}

	@PatchMapping("/{id}/estado")
	public Pedido cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> request) {
		return pedidoService.cambiarEstado(id, request.get("estado"), request.get("observacion"));
	}

	@PatchMapping("/{id}/confirmacion")
	public Pedido confirmarPedido(@PathVariable Long id) {
		return pedidoService.confirmarPedido(id);
	}

	@PatchMapping("/{id}/cancelacion")
	public Pedido cancelarPedido(@PathVariable Long id) {
		return pedidoService.cancelarPedido(id);
	}

	@PatchMapping("/{id}/cupon/{idCupon}")
	public Pedido aplicarCupon(@PathVariable Long id, @PathVariable Long idCupon) {
		return pedidoService.aplicarCupon(id, idCupon);
	}

	@PatchMapping("/{id}/cupon/{idUsoCupon}/anulacion")
	public Pedido anularUsoCupon(@PathVariable Long id, @PathVariable Long idUsoCupon) {
		return pedidoService.anularUsoCupon(id, idUsoCupon);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Long id) {
		pedidoService.eliminar(id);
		return ResponseEntity.noContent().build();
	}
}
