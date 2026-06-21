package microservice.pedido.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import microservice.pedido.model.Cupon;
import microservice.pedido.service.CuponService;

@RestController
@RequestMapping("/api/cupones")
public class CuponController {

	private final CuponService cuponService;

	public CuponController(CuponService cuponService) {
		this.cuponService = cuponService;
	}

	@GetMapping
	public List<Cupon> listar() {
		return cuponService.listar();
	}

	@GetMapping("/{id}")
	public Cupon obtenerPorId(@PathVariable Long id) {
		return cuponService.obtenerPorId(id);
	}

	@GetMapping("/codigo/{codigo}")
	public Cupon obtenerPorCodigo(@PathVariable String codigo) {
		return cuponService.obtenerPorCodigo(codigo);
	}

	@GetMapping("/estado/{estado}")
	public List<Cupon> buscarPorEstado(@PathVariable String estado) {
		return cuponService.buscarPorEstado(estado);
	}

	@PostMapping
	public Cupon crear(@Valid @RequestBody Cupon cupon) {
		return cuponService.crear(cupon);
	}

	@PutMapping("/{id}")
	public Cupon actualizar(@PathVariable Long id, @Valid @RequestBody Cupon cupon) {
		return cuponService.actualizar(id, cupon);
	}

	@PatchMapping("/{id}/estado")
	public Cupon cambiarEstado(@PathVariable Long id, @RequestBody Map<String, String> request) {
		return cuponService.cambiarEstado(id, request.get("estado"));
	}
}
