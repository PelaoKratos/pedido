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
public class DetallePedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idDetallePedido;

	@NotNull(message = "El id de producto es obligatorio")
	private Long idProducto;

	@NotNull(message = "La cantidad es obligatoria")
	@Positive(message = "La cantidad debe ser mayor a cero")
	private Integer cantidad;

	@NotNull(message = "El precio unitario es obligatorio")
	@PositiveOrZero(message = "El precio unitario no puede ser negativo")
	private BigDecimal precioUnitario;

	@PositiveOrZero(message = "El descuento no puede ser negativo")
	private BigDecimal descuento = BigDecimal.ZERO;

	@PositiveOrZero(message = "El subtotal no puede ser negativo")
	private BigDecimal subtotal = BigDecimal.ZERO;

	@JsonBackReference
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pedido", nullable = false)
	private Pedido pedido;

	public BigDecimal calcularSubtotal() {
		BigDecimal descuentoAplicado = descuento == null ? BigDecimal.ZERO : descuento;
		return precioUnitario.multiply(BigDecimal.valueOf(cantidad)).subtract(descuentoAplicado);
	}

	@Column(nullable = false)
	public BigDecimal getSubtotal() {
		return subtotal;
	}
}
