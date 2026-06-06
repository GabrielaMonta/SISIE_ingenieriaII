package sisie.capaDeLogica;

import org.springframework.stereotype.Component;
import sisie.capaDeDominio.Envio;
import sisie.capaDeDominio.ObservadorEnvio;

@Component
public class NotificadorCliente implements ObservadorEnvio {

    @Override
    public void actualizar(Envio envio) {
        if (envio.getVenta() != null && envio.getVenta().getCliente() != null) {
            String clienteNombre = envio.getVenta().getCliente().getNombre() + " " + envio.getVenta().getCliente().getApellido();
            System.out.println("[NotificadorCliente - SIMULACIÓN] Enviando notificación al cliente " 
                    + clienteNombre + " - Su envío con ID " + envio.getIdEnvio() 
                    + " ha cambiado al estado: " + envio.getEstadoActual().getNombre());
        } else {
            System.out.println("[NotificadorCliente - SIMULACIÓN] Su envío con ID " + envio.getIdEnvio() 
                    + " ha cambiado al estado: " + envio.getEstadoActual().getNombre());
        }
    }
}
