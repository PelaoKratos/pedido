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
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idPedido;

	@NotNull(message = "El id de cliente es obligatorio")
	private Long idCliente;

	@NotNull(message = "El id de sucursal es obligatorio")
	private Long idSucursal;

	@NotNull(message = "El id de direccion es obligatorio")
	private Long idDireccion;

	private Long idPago;

	@Column(nullable = false)
	private LocalDateTime fechaPedido;

	@PositiveOrZero(message = "El subtotal no puede ser negativo")
	private BigDecimal subtotal = BigDecimal.ZERO;

	@PositiveOrZero(message = "El descuento no puede ser negativo")
	private BigDecimal descuento = BigDecimal.ZERO;

	@PositiveOrZero(message = "El total no puede ser negativo")
	private BigDecimal total = BigDecimal.ZERO;

	@Column(nullable = false)
	private String estado;

	@Valid
	@JsonManagedReference
	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<DetallePedido> detalles = new ArrayList<>();

	@JsonManagedReference
	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<HistorialEstadoPedido> historialEstados = new ArrayList<>();
}
