package cl.duoc.tiendagas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo con formato inválido")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
