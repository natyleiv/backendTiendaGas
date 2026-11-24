package cl.duoc.tiendagas.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Lo que envía el carrito al confirmar la compra.
// El usuario NO viene aquí: se saca del token (así nadie compra a nombre de otro).
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompraRequest {
    @NotEmpty(message = "El carrito está vacío")
    @Valid
    private List<ItemCompraRequest> items;

    private String cupon; // opcional
}
