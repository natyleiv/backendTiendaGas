package cl.duoc.tiendagas.security;

import cl.duoc.tiendagas.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;

// Crea y valida los tokens JWT.
// Hay dos tipos de token (claim "tipo"):
//  - "access":  dura poco (30 min). Se manda en cada petición protegida.
//  - "refresh": dura más (7 días). Solo sirve para pedir un token de acceso nuevo.
@Service
public class JwtService {

    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder refreshDecoder;
    private final long expirationMinutes;
    private final long refreshExpirationDays;
    private final String issuer;

    public JwtService(
            JwtEncoder jwtEncoder,
            SecretKey jwtSecretKey,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes,
            @Value("${app.jwt.refresh-expiration-days}") long refreshExpirationDays,
            @Value("${app.jwt.issuer}") String issuer) {

        this.jwtEncoder = jwtEncoder;
        this.expirationMinutes = expirationMinutes;
        this.refreshExpirationDays = refreshExpirationDays;
        this.issuer = issuer;

        // Decodificador SOLO para tokens de renovación: revisa firma, vigencia,
        // emisor y que el claim "tipo" sea "refresh".
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                new JwtClaimValidator<String>("tipo", TIPO_REFRESH::equals)
        ));
        this.refreshDecoder = decoder;
    }

    public String generarToken(Usuario usuario) {
        return crear(usuario, TIPO_ACCESS, Duration.ofMinutes(expirationMinutes));
    }

    public String generarRefreshToken(Usuario usuario) {
        return crear(usuario, TIPO_REFRESH, Duration.ofDays(refreshExpirationDays));
    }

    public long segundosDeVida() {
        return expirationMinutes * 60;
    }

    // Valida un refresh token y devuelve el correo (subject) del usuario.
    public String validarRefreshToken(String refreshToken) {
        try {
            return refreshDecoder.decode(refreshToken).getSubject();
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "La sesión expiró. Inicia sesión nuevamente.");
        }
    }

    private String crear(Usuario usuario, String tipo, Duration duracion) {
        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(ahora)
                .expiresAt(ahora.plus(duracion))
                .subject(usuario.getCorreo())
                .claim("id", usuario.getId())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name())
                .claim("tipo", tipo)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
