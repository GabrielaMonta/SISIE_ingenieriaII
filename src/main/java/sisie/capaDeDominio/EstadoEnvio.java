package sisie.capaDeDominio;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "estado")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "nombre", discriminatorType = DiscriminatorType.STRING)
@Data
public abstract class EstadoEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Integer idEstado;

    @Column(name = "nombre", insertable = false, updatable = false)
    private String nombre;

    @Column(name = "descripcion", length = 30)
    private String descripcion;

    public abstract void transicionar(Envio envio);
}
