package sisie.capaDeLogica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sisie.capaDeDatos.EnvioRepository;
import sisie.capaDeDominio.Envio;

import java.util.List;

@Service
public class EnvioService {

    @Autowired
    private EnvioRepository envioRepository;

    public long contarTotalEnvios() {
        return envioRepository.count();
    }

    public long contarEnviosPorEstado(String estado) {
        return envioRepository.countByEstadoNombre(estado);
    }

    public List<Envio> ObtenerEnviosPendientes() {
        return envioRepository.findByEstadoNombre("Pendiente");
    }
}
