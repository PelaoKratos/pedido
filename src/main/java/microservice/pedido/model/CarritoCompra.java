package microservice.pedido.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CarritoCompra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idCarrito;

	@NotNull(message = "El id de cliente es obligatorio")
	private Long idCliente;

	@Column(nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(nullable = false)
	private String estado;

	@PositiveOrZero(message = "El subtotal no puede ser negativo")
	private BigDecimal subtotal = BigDecimal.ZERO;

	@Valid
	@JsonManagedReference(value = "carrito-detalles")
	@OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DetalleCarrito> detalles = new ArrayList<>();

	public void agregarProducto(DetalleCarrito detalle) {
		detalle.setCarrito(this);
		detalles.add(detalle);
		calcularSubtotal();
	}

	public void quitarProducto(Long idProducto) {
		detalles.removeIf(detalle -> idProducto.equals(detalle.getIdProducto()));
		calcularSubtotal();
	}

	public void aplicarDescuento(BigDecimal descuento) {
		BigDecimal descuentoAplicado = descuento == null ? BigDecimal.ZERO : descuento;
		subtotal = calcularSubtotal().subtract(descuentoAplicado);
		if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
			subtotal = BigDecimal.ZERO;
		}
	}

	public void confirmarCompra() {
		estado = "CONFIRMADO";
	}

	public BigDecimal calcularSubtotal() {
		subtotal = detalles.stream()
				.map(DetalleCarrito::calcularSubtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return subtotal;
	}
}
