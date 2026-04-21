package sisie.capaDeDominio;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "direccion")
@Data
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Integer idDireccion;

    @Column(name = "calle", length = 50, nullable = false)
    private String calle;

    @Column(name = "altura", length = 11, nullable = false)
    private String altura;

    @Column(name = "codigo_postal", length = 11, nullable = false)
    private String codigoPostal;

    @ManyToOne
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_ciudad", nullable = false)
    private Ciudad ciudad;
}