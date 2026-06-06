package sisie.capaDeDominio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("No entregado")
public class EstadoNoEntregado extends EstadoEnvio {

    @Override
    public void transicionar(Envio envio) {
        throw new IllegalStateException("El envío figura como no entregado. No se permiten más transiciones.");
    }
}
