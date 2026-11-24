package cl.duoc.tiendagas.controller;

import cl.duoc.tiendagas.dto.ConsultaRequest;
import cl.duoc.tiendagas.model.Consulta;
import cl.duoc.tiendagas.service.ConsultaService;
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
@RequestMapping("/api/v1/consultas")
@Tag(name = "Contacto", description = "Mensajes del formulario de contacto")
public class ConsultaController {

    @Autowired
    private ConsultaService consultaService;

    @PostMapping
    @Operation(summary = "Envía un mensaje de contacto (público)", description = "201 o 400")
    public ResponseEntity<Consulta> crear(@Valid @RequestBody ConsultaRequest datos) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultaService.crear(datos));
    }

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Lista los mensajes recibidos (ADMIN)")
    public ResponseEntity<List<Consulta>> listar() {
        return ResponseEntity.ok(consultaService.listar());
    }
}
