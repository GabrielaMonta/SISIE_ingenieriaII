package sisie.capaDeLogica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import sisie.capaDeDatos.EstadoEnvioRepository;
import sisie.capaDeDominio.EstadoProvider;

@Component
public class EstadoInitializer implements CommandLineRunner {

    @Autowired
    private EstadoEnvioRepository estadoEnvioRepository;

    @Override
    public void run(String... args) throws Exception {
        EstadoProvider.init(
            estadoEnvioRepository.findByNombre("Pendiente")
                .orElseThrow(() -> new RuntimeException("Estado 'Pendiente' no encontrado en la base de datos")),
            estadoEnvioRepository.findByNombre("En proceso")
                .orElseThrow(() -> new RuntimeException("Estado 'En proceso' no encontrado en la base de datos")),
            estadoEnvioRepository.findByNombre("En transito")
                .orElseThrow(() -> new RuntimeException("Estado 'En transito' no encontrado en la base de datos")),
            estadoEnvioRepository.findByNombre("Entregado")
                .orElseThrow(() -> new RuntimeException("Estado 'Entregado' no encontrado en la base de datos")),
            estadoEnvioRepository.findByNombre("No entregado")
                .orElseThrow(() -> new RuntimeException("Estado 'No entregado' no encontrado en la base de datos"))
        );
    }
}
