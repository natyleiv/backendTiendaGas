package cl.duoc.tiendagas.service;

import cl.duoc.tiendagas.dto.AuthResponse;
import cl.duoc.tiendagas.dto.LoginRequest;
import cl.duoc.tiendagas.dto.UsuarioResponse;
import cl.duoc.tiendagas.model.Usuario;
import cl.duoc.tiendagas.repository.UsuarioRepository;
import cl.duoc.tiendagas.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final int MAX_INTENTOS = 3;

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest datos) {
        String correo = UsuarioService.normalizarCorreo(datos.getCorreo());

        Usuario usuario = repository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "Correo o contraseña incorrectos"));

        if (usuario.isBloqueado()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La cuenta está bloqueada por 3 intentos fallidos. Contacta a un administrador.");
        }

        // Se compara la contraseña escrita con el hash guardado (BCrypt).
        if (!passwordEncoder.matches(datos.getPassword(), usuario.getPassword())) {
            int intentos = usuario.getIntentosFallidos() + 1;
            usuario.setIntentosFallidos(intentos);
            usuario.setBloqueado(intentos >= MAX_INTENTOS);
            repository.save(usuario);

            if (usuario.isBloqueado()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "La cuenta quedó bloqueada por 3 intentos fallidos. Contacta a un administrador.");
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Correo o contraseña incorrectos. Intentos restantes: " + (MAX_INTENTOS - intentos));
        }

        usuario.setIntentosFallidos(0);
        repository.save(usuario);
        return crearRespuesta(usuario);
    }

    // Renovación: con un refresh token válido se entrega un token de acceso nuevo
    // (y un refresh nuevo), sin pedir la contraseña otra vez.
    public AuthResponse renovar(String refreshToken) {
        String correo = jwtService.validarRefreshToken(refreshToken);

        Usuario usuario = repository.findByCorreo(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "La sesión ya no es válida. Inicia sesión nuevamente."));

        if (usuario.isBloqueado()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta está bloqueada.");
        }
        return crearRespuesta(usuario);
    }

    private AuthResponse crearRespuesta(Usuario usuario) {
        return new AuthResponse(
                jwtService.generarToken(usuario),
                jwtService.generarRefreshToken(usuario),
                "Bearer",
                jwtService.segundosDeVida(),
                UsuarioResponse.desde(usuario)
        );
    }
}
