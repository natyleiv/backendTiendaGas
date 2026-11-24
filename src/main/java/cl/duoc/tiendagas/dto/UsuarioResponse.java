package cl.duoc.tiendagas.dto;

import cl.duoc.tiendagas.model.Rol;
import cl.duoc.tiendagas.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Lo que la API devuelve de un usuario: todo MENOS la contraseña.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String run;
    private String nombre;
    private String apellidos;
    private String correo;
    private LocalDate fechaNacimiento;
    private String region;
    private String direccion;
    private Rol rol;
    private int intentosFallidos;
    private boolean bloqueado;

    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getRun(), u.getNombre(), u.getApellidos(),
                u.getCorreo(), u.getFechaNacimiento(), u.getRegion(), u.getDireccion(),
                u.getRol(), u.getIntentosFallidos(), u.isBloqueado());
    }
}
