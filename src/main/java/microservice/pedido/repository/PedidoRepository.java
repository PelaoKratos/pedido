package microservice.pedido.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import microservice.pedido.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

	List<Pedido> findByIdCliente(Long idCliente);

	List<Pedido> findByEstado(String estado);
}
