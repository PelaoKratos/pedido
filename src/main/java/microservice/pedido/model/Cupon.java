package microservice.pedido.model;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Cupon {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long idCupon;

	@NotBlank(message = "El codigo del cupon es obligatorio")
	@Column(nullable = false, unique = true)
	private String codigo;

	@NotBlank(message = "El tipo de descuento es obligatorio")
	@Column(nullable = false)
	private String tipoDescuento;

	@NotNull(message = "El valor de descuento es obligatorio")
	@PositiveOrZero(message = "El valor de descuento no puede ser negativo")
	private BigDecimal valorDescuento;

	@PositiveOrZero(message = "El monto minimo no puede ser negativo")
	private BigDecimal montoMinimo = BigDecimal.ZERO;

	@NotNull(message = "La fecha de inicio es obligatoria")
	private LocalDate fechaInicio;

	@NotNull(message = "La fecha de vencimiento es obligatoria")
	private LocalDate fechaVencimiento;

	@PositiveOrZero(message = "El limite de uso no puede ser negativo")
	private Integer limiteUso = 0;

	@PositiveOrZero(message = "Los usos actuales no pueden ser negativos")
	private Integer usosActuales = 0;

	@Column(nullable = false)
	private String estado;

	@JsonManagedReference(value = "cupon-usos-cupon")
	@OneToMany(mappedBy = "cupon", cascade = CascadeType.ALL)
	private List<UsoCupon> usosCupon = new ArrayList<>();
}
