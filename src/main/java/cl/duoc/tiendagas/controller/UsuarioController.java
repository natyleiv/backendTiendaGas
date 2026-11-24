package cl.duoc.tiendagas.controller;

import cl.duoc.tiendagas.dto.UsuarioRequest;
import cl.duoc.tiendagas.dto.UsuarioResponse;
import cl.duoc.tiendagas.security.UsuarioActual;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Administración de usuarios (ADMINISTRADOR) y perfil propio (/me)")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // ---- Perfil propio: cualquier usuario con sesión ----

    @GetMapping("/me")
    @Operation(summary = "Ver mi perfil")
    public ResponseEntity<UsuarioResponse> miPerfil(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(usuarioService.buscarPorId(UsuarioActual.desde(jwt).getId()));
    }

    @PutMapping("/me")
    @Operation(summary = "Editar mi perfil (el rol no se puede cambiar)")
    public ResponseEntity<UsuarioResponse> editarMiPerfil(@AuthenticationPrincipal Jwt jwt,
                                                          @Valid @RequestBody UsuarioRequest datos) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(UsuarioActual.desde(jwt), datos));
    }

    // ---- Administración: solo ADMINISTRADOR ----

    @GetMapping
    @Operation(summary = "Lista los usuarios (ADMIN)")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca un usuario (ADMIN)", description = "200 o 404")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Crea un usuario con cualquier rol (ADMIN)", description = "201, 400, 409")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(datos));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un usuario (ADMIN). Nadie cambia su propio rol",
            description = "200, 400, 404, 409. Si password viene vacío se mantiene la actual.")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody UsuarioRequest datos,
                                                      @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(usuarioService.actualizar(id, datos, UsuarioActual.desde(jwt)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Elimina un usuario (ADMIN)", description = "204, 404, 409 si es uno mismo o tiene compras")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        usuarioService.eliminar(id, UsuarioActual.desde(jwt));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desbloquear")
    @Operation(summary = "Desbloquea una cuenta bloqueada por intentos fallidos (ADMIN)")
    public ResponseEntity<UsuarioResponse> desbloquear(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.desbloquear(id));
    }

    @PatchMapping("/{id}/bloquear")
    @Operation(summary = "Bloquea (desactiva) una cuenta (ADMIN)")
    public ResponseEntity<UsuarioResponse> bloquear(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(usuarioService.bloquear(id, UsuarioActual.desde(jwt)));
    }
}
