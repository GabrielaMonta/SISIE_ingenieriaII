package sisie.capaDeDominio;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "transporte")
@Data
public class Transporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transporte")
    private Integer idTransporte;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "telefono", length = 15)
    private String telefono;
}
