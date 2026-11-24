package cl.duoc.tiendagas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Una compra (pedido) de un usuario. Sus productos están en DetalleBoleta.
// Se usa @Getter/@Setter en vez de @Data porque la relación es de ida y vuelta
// (Boleta -> detalles -> boleta) y @Data generaría un ciclo infinito en toString/hashCode.
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "boleta")
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_boleta")
    private Long id;

    // Relación: un usuario tiene muchas boletas (clave foránea usuario_id).
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 150)
    private String direccion;

    @Column(nullable = false)
    private Integer subtotal;

    @Column(nullable = false)
    private Integer descuento;

    @Column(nullable = false)
    private Integer total;

    @Column(length = 20)
    private String cupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoBoleta estado;

    // Relación: una boleta tiene muchos detalles. cascade = se guardan junto con la boleta.
    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleBoleta> detalles = new ArrayList<>();
}
