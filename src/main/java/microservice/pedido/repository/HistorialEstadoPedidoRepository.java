package microservice.pedido.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import microservice.pedido.model.HistorialEstadoPedido;

public interface HistorialEstadoPedidoRepository extends JpaRepository<HistorialEstadoPedido, Long> {

	List<HistorialEstadoPedido> findByPedidoIdPedido(Long idPedido);
}
