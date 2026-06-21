package microservice.pedido.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DetalleCarrito {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idDetalleCarrito;

	@Column(name = "id_carrito", insertable = false, updatable = false)
	private Long idCarrito;

	@NotNull(message = "El id de producto es obligatorio")
	private Long idProducto;

	@NotNull(message = "La cantidad es obligatoria")
	@Positive(message = "La cantidad debe ser mayor a cero")
	private Integer cantidad;

	@NotNull(message = "El precio unitario es obligatorio")
	@PositiveOrZero(message = "El precio unitario no puede ser negativo")
	private BigDecimal precioUnitario;

	@PositiveOrZero(message = "El subtotal no puede ser negativo")
	private BigDecimal subtotal = BigDecimal.ZERO;

	@JsonBackReference(value = "carrito-detalles")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_carrito", nullable = false)
	private CarritoCompra carrito;

	public void actualizarCantidad(Integer cantidad) {
		this.cantidad = cantidad;
		calcularSubtotal();
	}

	public BigDecimal calcularSubtotal() {
		subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
		return subtotal;
	}

	public void setCarrito(CarritoCompra carrito) {
		this.carrito = carrito;
		this.idCarrito = carrito != null ? carrito.getIdCarrito() : null;
	}
}
