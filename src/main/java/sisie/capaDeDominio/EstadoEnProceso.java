package sisie.capaDeDominio;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("En proceso")
public class EstadoEnProceso extends EstadoEnvio {

    @Override
    public void transicionar(Envio envio) {
        if (envio.getCodSeguimiento() == null || envio.getCodSeguimiento().trim().isEmpty()) {
            throw new IllegalStateException("No se puede iniciar el tránsito sin un código de seguimiento asignado.");
        }
        envio.setEstadoActual(EstadoProvider.getEnTransito());
    }
}
