package microservice.pedido.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class HistorialEstadoPedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idHistorial;

	@Column(name = "id_pedido", insertable = false, updatable = false)
	private Long idPedido;

	private String estadoAnterior;

	@Column(nullable = false)
	private String estadoNuevo;

	@Column(nullable = false)
	private LocalDateTime fechaCambio;

	private String observacion;

	@JsonBackReference
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_pedido", nullable = false)
	private Pedido pedido;

	public void registrarCambio(String estadoAnterior, String estadoNuevo, String observacion) {
		this.estadoAnterior = estadoAnterior;
		this.estadoNuevo = estadoNuevo;
		this.observacion = observacion;
		this.fechaCambio = LocalDateTime.now();
	}

	public String consultarHistorial() {
		return estadoAnterior + " -> " + estadoNuevo + ": " + observacion;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
		this.idPedido = pedido != null ? pedido.getIdPedido() : null;
	}
}
