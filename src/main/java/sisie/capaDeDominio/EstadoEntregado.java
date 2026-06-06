package sisie.capaDeDominio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Entregado")
public class EstadoEntregado extends EstadoEnvio {

    @Override
    public void transicionar(Envio envio) {
        throw new IllegalStateException("El envío ya ha sido entregado. No se permiten más transiciones.");
    }
}
