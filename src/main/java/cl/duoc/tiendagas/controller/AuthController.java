package cl.duoc.tiendagas.controller;

import cl.duoc.tiendagas.dto.*;
import cl.duoc.tiendagas.security.UsuarioActual;
import cl.duoc.tiendagas.service.AuthService;
import cl.duoc.tiendagas.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Login, registro y renovación del token JWT")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y entrega el token JWT",
            description = "200: token + refreshToken. 401: credenciales incorrectas. 403: cuenta bloqueada (3 intentos).")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest datos) {
        return ResponseEntity.ok(authService.login(datos));
    }

    @PostMapping("/register")
    @Operation(summary = "Registro público de clientes (solo correos @duoc.cl)",
            description = "201: creado. 400: datos inválidos. 409: RUN o correo repetido.")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody UsuarioRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(datos));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renueva el token de acceso usando el refreshToken",
            description = "200: tokens nuevos. 401: refreshToken inválido o vencido.")
    public ResponseEntity<AuthResponse> renovar(@Valid @RequestBody RefreshRequest datos) {
        return ResponseEntity.ok(authService.renovar(datos.getRefreshToken()));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Datos del usuario dueño del token")
    public ResponseEntity<UsuarioResponse> yo(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(usuarioService.buscarPorId(UsuarioActual.desde(jwt).getId()));
    }
}
