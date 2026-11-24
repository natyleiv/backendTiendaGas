package cl.duoc.tiendagas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Mensaje enviado desde el formulario de Contacto.
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "consulta")
public class Consulta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_consulta")
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String correo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fecha;
}
