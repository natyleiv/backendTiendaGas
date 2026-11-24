package cl.duoc.tiendagas.dto;

import cl.duoc.tiendagas.model.Boleta;
import cl.duoc.tiendagas.model.EstadoBoleta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoletaResponse {
    private Long id;
    private LocalDateTime fecha;
    private Long usuarioId;
    private String cliente;
    private String correo;
    private String direccion;
    private EstadoBoleta estado;
    private Integer subtotal;
    private Integer descuento;
    private Integer total;
    private String cupon;
    private List<DetalleBoletaResponse> detalles;

    public static BoletaResponse desde(Boleta b) {
        return new BoletaResponse(b.getId(), b.getFecha(), b.getUsuario().getId(),
                b.getUsuario().getNombre() + " " + b.getUsuario().getApellidos(),
                b.getUsuario().getCorreo(), b.getDireccion(), b.getEstado(),
                b.getSubtotal(), b.getDescuento(), b.getTotal(), b.getCupon(),
                b.getDetalles().stream().map(DetalleBoletaResponse::desde).toList());
    }
}
