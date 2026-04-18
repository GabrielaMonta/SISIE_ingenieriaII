package sisie.capaDeDominio;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre", length = 20)
    private String nombre;

    @Column(name = "apellido", length = 20)
    private String apellido;

    @Column(name = "dni", length = 8, unique = true, nullable = false)
    private String dni;

    @Column(name = "telefono", length = 15)
    private String telefono;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "contraseña", length = 200, nullable = false)
    private String contraseña;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "baja", length = 2)
    private String baja;
}
