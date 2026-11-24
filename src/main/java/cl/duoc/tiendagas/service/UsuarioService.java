package cl.duoc.tiendagas.service;

import cl.duoc.tiendagas.dto.UsuarioRequest;
import cl.duoc.tiendagas.dto.UsuarioResponse;
import cl.duoc.tiendagas.model.Rol;
import cl.duoc.tiendagas.model.Usuario;
import cl.duoc.tiendagas.repository.BoletaRepository;
import cl.duoc.tiendagas.repository.UsuarioRepository;
import cl.duoc.tiendagas.security.UsuarioActual;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BoletaRepository boletaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    public Usuario buscarEntidad(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.desde(buscarEntidad(id));
    }

    // Registro público: siempre crea un CLIENTE y solo acepta correos @duoc.cl (regla EP1).
    public UsuarioResponse registrar(UsuarioRequest datos) {
        validar(datos, Validaciones.DOMINIOS_REGISTRO, null);
        exigirPassword(datos);
        Usuario usuario = new Usuario();
        copiarDatos(datos, usuario);
        usuario.setRol(Rol.CLIENTE);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    // El administrador crea usuarios con cualquier rol.
    public UsuarioResponse crear(UsuarioRequest datos) {
        validar(datos, Validaciones.DOMINIOS_GENERALES, null);
        exigirPassword(datos);
        Usuario usuario = new Usuario();
        copiarDatos(datos, usuario);
        usuario.setRol(datos.getRol() != null ? datos.getRol() : Rol.CLIENTE);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    // El administrador edita. Nadie puede cambiar su propio rol (regla de la EP2).
    public UsuarioResponse actualizar(Long id, UsuarioRequest datos, UsuarioActual quien) {
        Usuario usuario = buscarEntidad(id);
        validar(datos, Validaciones.DOMINIOS_GENERALES, id);
        copiarDatos(datos, usuario);
        if (datos.getRol() != null && !id.equals(quien.getId())) {
            usuario.setRol(datos.getRol());
        }
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    // Cada usuario edita su perfil, pero nunca su rol.
    public UsuarioResponse actualizarPerfil(UsuarioActual quien, UsuarioRequest datos) {
        Usuario usuario = buscarEntidad(quien.getId());
        validar(datos, Validaciones.DOMINIOS_GENERALES, usuario.getId());
        copiarDatos(datos, usuario);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    public void eliminar(Long id, UsuarioActual quien) {
        Usuario usuario = buscarEntidad(id);
        if (id.equals(quien.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes eliminar tu propia cuenta");
        }
        if (boletaRepository.existsByUsuarioId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar: el usuario tiene compras registradas. Puedes bloquear su cuenta.");
        }
        usuarioRepository.delete(usuario);
    }

    public UsuarioResponse desbloquear(Long id) {
        Usuario usuario = buscarEntidad(id);
        usuario.setBloqueado(false);
        usuario.setIntentosFallidos(0);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    public UsuarioResponse bloquear(Long id, UsuarioActual quien) {
        if (id.equals(quien.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No puedes bloquear tu propia cuenta");
        }
        Usuario usuario = buscarEntidad(id);
        usuario.setBloqueado(true);
        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    // ---- reglas compartidas ----

    private void validar(UsuarioRequest datos, List<String> dominios, Long idActual) {
        if (!Validaciones.runValido(datos.getRun())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RUN inválido (ej: 12345678-5)");
        }
        String run = Validaciones.normalizarRun(datos.getRun());
        boolean runRepetido = idActual == null
                ? usuarioRepository.existsByRun(run)
                : usuarioRepository.existsByRunAndIdNot(run, idActual);
        if (runRepetido) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese RUN ya está registrado");
        }

        String correo = normalizarCorreo(datos.getCorreo());
        if (!Validaciones.dominioPermitido(correo, dominios)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Dominio de correo no permitido. Usa: " + String.join(", ", dominios));
        }
        boolean correoRepetido = idActual == null
                ? usuarioRepository.existsByCorreo(correo)
                : usuarioRepository.existsByCorreoAndIdNot(correo, idActual);
        if (correoRepetido) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }

        if (datos.getFechaNacimiento().isAfter(LocalDate.now())
                || Validaciones.edad(datos.getFechaNacimiento()) < Validaciones.EDAD_MINIMA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debes tener al menos " + Validaciones.EDAD_MINIMA + " años");
        }
    }

    private void exigirPassword(UsuarioRequest datos) {
        if (datos.getPassword() == null || datos.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria");
        }
    }

    private void copiarDatos(UsuarioRequest datos, Usuario usuario) {
        usuario.setRun(Validaciones.normalizarRun(datos.getRun()));
        usuario.setNombre(datos.getNombre().trim());
        usuario.setApellidos(datos.getApellidos().trim());
        usuario.setCorreo(normalizarCorreo(datos.getCorreo()));
        usuario.setFechaNacimiento(datos.getFechaNacimiento());
        usuario.setRegion(datos.getRegion().trim());
        usuario.setDireccion(datos.getDireccion().trim());
        // La contraseña solo cambia si viene una nueva. Siempre se guarda con hash.
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
    }

    public static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase(Locale.ROOT);
    }
}
