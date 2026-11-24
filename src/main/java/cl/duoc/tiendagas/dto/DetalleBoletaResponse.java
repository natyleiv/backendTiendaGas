package cl.duoc.tiendagas.dto;

import cl.duoc.tiendagas.model.DetalleBoleta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetalleBoletaResponse {
    private Long productoId;
    private String nombre;
    private Integer precioUnitario;
    private Integer cantidad;
    private Integer subtotal;

    public static DetalleBoletaResponse desde(DetalleBoleta d) {
        return new DetalleBoletaResponse(d.getProducto().getId(), d.getNombreProducto(),
                d.getPrecioUnitario(), d.getCantidad(), d.getSubtotal());
    }
}
