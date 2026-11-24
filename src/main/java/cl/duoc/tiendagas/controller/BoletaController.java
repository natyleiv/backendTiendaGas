package cl.duoc.tiendagas.controller;

import cl.duoc.tiendagas.dto.BoletaResponse;
import cl.duoc.tiendagas.dto.CompraRequest;
import cl.duoc.tiendagas.dto.EstadoRequest;
import cl.duoc.tiendagas.security.UsuarioActual;
import cl.duoc.tiendagas.service.BoletaService;
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
@RequestMapping("/api/v1/boletas")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Boletas (pedidos)", description = "Compras, historial del cliente y gestión de órdenes")
public class BoletaController {

    @Autowired
    private BoletaService boletaService;

    @PostMapping
    @Operation(summary = "Confirma una compra del usuario del token",
            description = "201: boleta creada y stock descontado. 400: cupón o datos inválidos. 404: producto. 409: sin stock.")
    public ResponseEntity<BoletaResponse> comprar(@Valid @RequestBody CompraRequest datos,
                                                  @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(boletaService.crear(datos, UsuarioActual.desde(jwt)));
    }

    @GetMapping("/mis-boletas")
    @Operation(summary = "Historial: solo las boletas del usuario del token")
    public ResponseEntity<List<BoletaResponse>> misBoletas(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(boletaService.misBoletas(UsuarioActual.desde(jwt)));
    }

    @GetMapping
    @Operation(summary = "Lista todas las órdenes (ADMIN y VENDEDOR)")
    public ResponseEntity<List<BoletaResponse>> listar() {
        return ResponseEntity.ok(boletaService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una boleta",
            description = "El cliente solo ve las suyas (403 si es de otro). ADMIN y VENDEDOR ven todas.")
    public ResponseEntity<BoletaResponse> buscarPorId(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(boletaService.buscarPorId(id, UsuarioActual.desde(jwt)));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambia el estado del pedido (ADMIN)",
            description = "PENDIENTE → EN_REPARTO → ENTREGADA, o CANCELADA (devuelve el stock). 409 si ya es final.")
    public ResponseEntity<BoletaResponse> cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoRequest datos) {
        return ResponseEntity.ok(boletaService.cambiarEstado(id, datos.getEstado()));
    }
}
