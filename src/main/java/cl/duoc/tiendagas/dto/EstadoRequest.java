package cl.duoc.tiendagas.dto;

import cl.duoc.tiendagas.model.EstadoBoleta;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoRequest {
    @NotNull(message = "El estado es obligatorio")
    private EstadoBoleta estado;
}
