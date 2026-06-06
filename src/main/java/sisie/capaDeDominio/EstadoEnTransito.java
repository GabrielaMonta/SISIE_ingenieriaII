package sisie.capaDeDominio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("En transito")
public class EstadoEnTransito extends EstadoEnvio {

    @Override
    public void transicionar(Envio envio) {
        envio.setEstadoActual(EstadoProvider.getEntregado());
    }
}
