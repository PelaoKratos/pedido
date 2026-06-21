package microservice.pedido.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import microservice.pedido.model.Cupon;

public interface CuponRepository extends JpaRepository<Cupon, Long> {

	Optional<Cupon> findByCodigo(String codigo);

	List<Cupon> findByEstado(String estado);
}
