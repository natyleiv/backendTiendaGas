package cl.duoc.tiendagas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Respuesta del login y de la renovación del token.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;          // token de acceso (dura poco: 30 min)
    private String refreshToken;   // token para renovar (dura 7 días)
    private String tipo;           // siempre "Bearer"
    private long expiraEn;         // segundos de vida del token de acceso
    private UsuarioResponse usuario;
}
