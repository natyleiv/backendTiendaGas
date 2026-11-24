package cl.duoc.tiendagas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Una línea de la boleta: qué producto, cuántas unidades y a qué precio.
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "detalle_boleta")
public class DetalleBoleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "boleta_id", nullable = false)
    private Boleta boleta;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    // Se copia el nombre y el precio del momento de la compra:
    // si después cambia el precio del producto, la boleta no cambia.
    @Column(name = "nombre_producto", nullable = false, length = 80)
    private String nombreProducto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario", nullable = false)
    private Integer precioUnitario;

    @Column(nullable = false)
    private Integer subtotal;
}
