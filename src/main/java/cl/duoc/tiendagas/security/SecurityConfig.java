package cl.duoc.tiendagas.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = "ADMINISTRADOR";
    private static final String VENDEDOR = "VENDEDOR";

    // Contraseñas guardadas con hash BCrypt.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey(@Value("${app.jwt.secret}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException(
                    "Falta la clave JWT (mínimo 32 caracteres). Copia " +
                    "src/main/resources/secretos.example.properties como secretos.properties");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey key) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    // Valida el token de cada petición: firma, vigencia (exp), emisor (iss)
    // y que sea un token de ACCESO (un refresh token no sirve para entrar).
    @Bean
    public JwtDecoder jwtDecoder(SecretKey key, @Value("${app.jwt.issuer}") String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(issuer),
                new JwtClaimValidator<String>("tipo", JwtService.TIPO_ACCESS::equals)
        ));
        return decoder;
    }

    // Lee el claim "rol" del token y lo transforma en ROLE_ADMINISTRADOR, ROLE_VENDEDOR...
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationConverter converter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {
                })
                // Sin sesión en el servidor: cada petición trae su token (STATELESS).
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        // Autenticación (público)
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh").permitAll()

                        // Catálogo: cualquiera puede verlo; solo el ADMIN lo modifica
                        .requestMatchers(HttpMethod.GET, "/api/v1/productos/**", "/api/v1/categorias/**").permitAll()
                        .requestMatchers("/api/v1/productos/**", "/api/v1/categorias/**").hasRole(ADMIN)

                        // Perfil propio: cualquier usuario con sesión
                        .requestMatchers("/api/v1/usuarios/me").authenticated()
                        // Resto de usuarios: solo ADMIN
                        .requestMatchers("/api/v1/usuarios/**").hasRole(ADMIN)

                        // Boletas (pedidos)
                        .requestMatchers(HttpMethod.GET, "/api/v1/boletas").hasAnyRole(ADMIN, VENDEDOR)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/boletas/*/estado").hasRole(ADMIN)
                        .requestMatchers("/api/v1/boletas/**").authenticated()

                        // Reportes del dashboard
                        .requestMatchers("/api/v1/reportes/**").hasRole(ADMIN)

                        // Contacto: cualquiera envía; solo ADMIN lee
                        .requestMatchers(HttpMethod.POST, "/api/v1/consultas").permitAll()
                        .requestMatchers("/api/v1/consultas/**").hasRole(ADMIN)

                        // Todo lo demás requiere token
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(converter)));

        return http.build();
    }

    // CORS: permite que React (localhost:5173) llame a esta API desde el navegador.
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.origenes}") List<String> origenes) {

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(origenes);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
