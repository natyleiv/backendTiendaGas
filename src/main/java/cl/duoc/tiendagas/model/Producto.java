package cl.duoc.tiendagas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nombre;

    // Relación: muchos productos pertenecen a una categoría (clave foránea categoria_id).
    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    // Montos en pesos chilenos (sin decimales).
    @Column(nullable = false)
    private Integer precio;

    // Opcional: si existe, es el precio rebajado.
    @Column(name = "precio_oferta")
    private Integer precioOferta;

    @Column(nullable = false, length = 150)
    private String imagen;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "stock_critico", nullable = false)
    private Integer stockCritico;

    @Column(nullable = false, length = 500)
    private String descripcion;

    // Precio que se cobra: el de oferta si existe.
    public int precioFinal() {
        return precioOferta != null ? precioOferta : precio;
    }
}
