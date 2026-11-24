package cl.duoc.tiendagas.repository;

import cl.duoc.tiendagas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByRun(String run);

    // Para editar: ¿existe OTRO usuario (id distinto) con ese correo / RUN?
    boolean existsByCorreoAndIdNot(String correo, Long id);

    boolean existsByRunAndIdNot(String run, Long id);
}
