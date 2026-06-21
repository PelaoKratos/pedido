package microservice.pedido.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import microservice.pedido.model.CarritoCompra;

public interface CarritoCompraRepository extends JpaRepository<CarritoCompra, Long> {

	List<CarritoCompra> findByIdCliente(Long idCliente);

	List<CarritoCompra> findByEstado(String estado);
}
