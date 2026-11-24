package cl.duoc.tiendagas.controller;

import cl.duoc.tiendagas.dto.ReporteResponse;
import cl.duoc.tiendagas.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reportes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reportes", description = "Indicadores del dashboard (ADMINISTRADOR)")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping("/resumen")
    @Operation(summary = "Resumen de ventas. periodo = semana | mes | anio | todo")
    public ResponseEntity<ReporteResponse> resumen(@RequestParam(defaultValue = "mes") String periodo) {
        return ResponseEntity.ok(reporteService.resumen(periodo));
    }
}
