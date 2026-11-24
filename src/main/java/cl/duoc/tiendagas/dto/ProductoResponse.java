package cl.duoc.tiendagas.dto;

import cl.duoc.tiendagas.model.Producto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Mismos campos que la interface Producto del frontend.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoResponse {
    private Long id;
    private String codigo;
    private String nombre;
    private Long categoriaId;
    private String categoriaNombre;
    private Integer precio;
    private Integer precioOferta;
    private String imagen;
    private Integer stock;
    private Integer stockCritico;
    private String descripcion;

    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(p.getId(), p.getCodigo(), p.getNombre(),
                p.getCategoria().getId(), p.getCategoria().getNombre(),
                p.getPrecio(), p.getPrecioOferta(), p.getImagen(),
                p.getStock(), p.getStockCritico(), p.getDescripcion());
    }
}
