package microservice.pedido.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import microservice.pedido.model.UsoCupon;

public interface UsoCuponRepository extends JpaRepository<UsoCupon, Long> {

	List<UsoCupon> findByPedidoIdPedido(Long idPedido);

	List<UsoCupon> findByCuponIdCupon(Long idCupon);
}
