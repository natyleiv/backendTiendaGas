package cl.duoc.tiendagas.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductoRequest {
    @NotBlank(message = "El código es obligatorio")
    @Size(max = 20)
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80)
    private String nombre;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Integer precio;

    // Opcional. Si viene, debe ser menor que el precio (se revisa en el servicio).
    @Positive(message = "El precio de oferta debe ser mayor a 0")
    private Integer precioOferta;

    @NotBlank(message = "La imagen es obligatoria")
    private String imagen;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "El stock crítico es obligatorio")
    @PositiveOrZero(message = "El stock crítico no puede ser negativo")
    private Integer stockCritico;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500)
    private String descripcion;
}
