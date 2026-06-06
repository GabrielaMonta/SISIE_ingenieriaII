package sisie.capaDeDominio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Pendiente")
public class EstadoPendiente extends EstadoEnvio {

    @Override
    public void transicionar(Envio envio) {
        envio.setEstadoActual(EstadoProvider.getEnProceso());
    }
}
