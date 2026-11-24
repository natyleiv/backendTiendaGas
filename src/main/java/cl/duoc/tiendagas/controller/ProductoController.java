package cl.duoc.tiendagas.controller;

import cl.duoc.tiendagas.dto.ProductoRequest;
import cl.duoc.tiendagas.dto.ProductoResponse;
import cl.duoc.tiendagas.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "GET público (catálogo). Crear, editar y eliminar: solo ADMINISTRADOR")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    @Operation(summary = "Lista los productos (público). Opcional: ?categoriaId=1")
    public ResponseEntity<List<ProductoResponse>> listar(@RequestParam(required = false) Long categoriaId) {
        return ResponseEntity.ok(productoService.listar(categoriaId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un producto (público)", description = "200 o 404")
    public ResponseEntity<ProductoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.buscarPorId(id));
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Crea un producto (ADMIN)", description = "201, 400, 404 categoría, 409 código repetido")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crear(datos));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Actualiza un producto (ADMIN)", description = "200, 400, 404, 409")
    public ResponseEntity<ProductoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest datos) {
        return ResponseEntity.ok(productoService.actualizar(id, datos));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Elimina un producto (ADMIN)", description = "204, 404, 409 si ya tiene compras")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
