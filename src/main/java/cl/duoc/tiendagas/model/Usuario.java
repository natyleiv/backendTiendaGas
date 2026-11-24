package cl.duoc.tiendagas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, unique = true, length = 12)
    private String run;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 80)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    // Se guarda el HASH de la contraseña (BCrypt), nunca la contraseña real.
    @Column(nullable = false, length = 100)
    private String password;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, length = 60)
    private String region;

    @Column(nullable = false, length = 150)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    // Regla de la EP1: bloqueo a los 3 intentos fallidos.
    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;

    @Column(nullable = false)
    private boolean bloqueado;
}
