package cl.duoc.tiendagas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoVendidoResponse {
    private Long productoId;
    private String nombre;
    private int unidades;
    private int monto;
}
