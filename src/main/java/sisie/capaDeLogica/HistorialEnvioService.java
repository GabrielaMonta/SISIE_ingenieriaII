package sisie.capaDeLogica;
import sisie.capaDeDominio.*;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sisie.capaDeDatos.HistorialEnvioRepository;

@Service
public class HistorialEnvioService {
    
    @Autowired private HistorialEnvioRepository historialRepository;

    public void registrarCambioEstado(Envio envio, String motivo, Usuario usuario) {
        HistorialEnvio h = new HistorialEnvio();
        h.setEnvio(envio);
        h.setEstado(envio.getEstadoActual());
        h.setFechaMovimiento(LocalDateTime.now());
        h.setMotivo(motivo);
        h.setUsuario(usuario);
        historialRepository.save(h);
    }

    public List<HistorialEnvio> obtenerHistorial(Integer idEnvio) {
        return historialRepository.findByEnvioIdEnvioOrderByFechaMovimientoAsc(idEnvio);
    }
}
