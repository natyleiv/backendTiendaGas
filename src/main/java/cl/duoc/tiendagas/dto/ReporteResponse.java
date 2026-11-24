package cl.duoc.tiendagas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

// Indicadores del dashboard del administrador.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReporteResponse {
    private String periodo;                 // semana, mes, anio o todo
    private int totalBoletas;               // cantidad de operaciones
    private int montoTotal;                 // total vendido (sin contar canceladas)
    private int unidadesVendidas;           // productos vendidos
    private List<ProductoVendidoResponse> masVendidos; // top 5
    private Map<String, Integer> boletasPorEstado;
}
