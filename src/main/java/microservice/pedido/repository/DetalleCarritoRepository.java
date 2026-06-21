package microservice.pedido.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import microservice.pedido.model.DetalleCarrito;

public interface DetalleCarritoRepository extends JpaRepository<DetalleCarrito, Long> {

	List<DetalleCarrito> findByCarritoIdCarrito(Long idCarrito);
}
