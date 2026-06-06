package sisie.capaDeLogica;

import org.springframework.stereotype.Component;
import sisie.capaDeDominio.Envio;
import sisie.capaDeDominio.ObservadorEnvio;

@Component
public class ActualizadorTablaEnvios implements ObservadorEnvio {

    @Override
    public void actualizar(Envio envio) {
        System.out.println("[ActualizadorTablaEnvios - SIMULACIÓN] Refrescando la tabla de envíos. El envío ID " 
                + envio.getIdEnvio() + " ahora está en estado: " + envio.getEstadoActual().getNombre());
    }
}
