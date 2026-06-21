package microservice.pedido.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UsoCupon {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idUsoCupon;

	@Column(name = "id_pedido", insertable = false, updatable = false)
	private Long idPedido;

	@Column(name = "id_cupon", insertable = false, updatable = false)
	private Long idCupon;

	@Column(nullable = false)
	private LocalDateTime fechaUso;

	@PositiveOrZero(message = "El descuento aplicado no puede ser negativo")
	private BigDecimal descuentoAplicado = BigDecimal.ZERO;

	@Column(nullable = false)
	private String estado;

	@JsonBackReference(value = "pedido-usos-cupon")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pedido", nullable = false)
	private Pedido pedido;

	@JsonBackReference(value = "cupon-usos-cupon")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_cupon", nullable = false)
	private Cupon cupon;

	public void registrarUso() {
		fechaUso = LocalDateTime.now();
		estado = "APLICADO";
	}

	public boolean validarUso() {
		return "APLICADO".equals(estado);
	}

	public void anularUso() {
		estado = "ANULADO";
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
		this.idPedido = pedido != null ? pedido.getIdPedido() : null;
	}

	public void setCupon(Cupon cupon) {
		this.cupon = cupon;
		this.idCupon = cupon != null ? cupon.getIdCupon() : null;
	}
}
