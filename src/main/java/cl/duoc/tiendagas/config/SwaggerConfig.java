package cl.duoc.tiendagas.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API Distribuidora de Gas El Volcán",
                version = "v1",
                description = "API REST de la tienda online (DSY1104). Productos, categorías, usuarios, " +
                        "boletas, reportes y autenticación con JWT. Para probar endpoints protegidos: " +
                        "haz login en /api/v1/auth/login, copia el token y pégalo en el botón Authorize."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Pega el token obtenido en /api/v1/auth/login (sin la palabra Bearer)"
)
public class SwaggerConfig {
}
