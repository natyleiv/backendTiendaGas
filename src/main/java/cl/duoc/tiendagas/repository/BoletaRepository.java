package cl.duoc.tiendagas.repository;

import cl.duoc.tiendagas.model.Boleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BoletaRepository extends JpaRepository<Boleta, Long> {

    // Historial de un cliente: solo SUS boletas, de la más nueva a la más antigua.
    List<Boleta> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    List<Boleta> findAllByOrderByFechaDesc();

    // Para los reportes: boletas desde una fecha.
    List<Boleta> findByFechaGreaterThanEqual(LocalDateTime desde);

    boolean existsByUsuarioId(Long usuarioId);
}
