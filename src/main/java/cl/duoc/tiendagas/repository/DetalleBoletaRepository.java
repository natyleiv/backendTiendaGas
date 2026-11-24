package cl.duoc.tiendagas.repository;

import cl.duoc.tiendagas.model.DetalleBoleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleBoletaRepository extends JpaRepository<DetalleBoleta, Long> {

    boolean existsByProductoId(Long productoId);
}
