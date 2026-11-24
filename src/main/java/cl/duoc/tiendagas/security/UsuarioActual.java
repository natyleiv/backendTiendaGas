package cl.duoc.tiendagas.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.oauth2.jwt.Jwt;

// Datos del usuario que hizo la petición, sacados de su token.
@Data
@AllArgsConstructor
public class UsuarioActual {
    private Long id;
    private String correo;
    private String rol;

    public static UsuarioActual desde(Jwt jwt) {
        Number id = jwt.getClaim("id");
        return new UsuarioActual(id.longValue(), jwt.getSubject(), jwt.getClaimAsString("rol"));
    }

    public boolean esAdmin() {
        return "ADMINISTRADOR".equals(rol);
    }

    public boolean esVendedor() {
        return "VENDEDOR".equals(rol);
    }
}
