package sisie.capaDeLogica;
import sisie.capaDeDominio.*;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sisie.capaDeDatos.HistorialEnvioRepository;

@Service
public class HistorialEnvioService {
    
    @Autowired private HistorialEnvioRepository historialRepository;

    public void registrarCambioEstado(Envio envio, Estado estado, Usuario usuario){

        
        HistorialEnvio h = new HistorialEnvio();
        h.setEnvio(envio);
        h.setEstado(estado);
        h.setFechaMovimiento(LocalDateTime.now());
        h.setUsuario(usuario);
        h.setMotivo("Inicio de gestión"); // Se asigna motivo, ya que es nullable = false en la BD

        historialRepository.save(h);
    }
}
