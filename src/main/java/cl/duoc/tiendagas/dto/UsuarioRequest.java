package cl.duoc.tiendagas.dto;

import cl.duoc.tiendagas.model.Rol;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// Datos que llegan desde los formularios de registro, administración y perfil.
// Las reglas del RUN (módulo 11), el dominio del correo y la edad mínima
// se revisan en UsuarioService.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioRequest {
    @NotBlank(message = "El RUN es obligatorio")
    private String run;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(max = 80)
    private String apellidos;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo con formato inválido")
    @Size(max = 100, message = "El correo puede tener máximo 100 caracteres")
    private String correo;

    // Obligatoria al crear. Al editar, si viene vacía se mantiene la actual.
    @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres")
    private String password;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    @NotBlank(message = "La región es obligatoria")
    private String region;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 150)
    private String direccion;

    // Solo lo usa el administrador. En registro y perfil se ignora.
    private Rol rol;
}
