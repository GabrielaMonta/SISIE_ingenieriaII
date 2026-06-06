package sisie.capaDeDatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sisie.capaDeDominio.EstadoEnvio;
import java.util.Optional;

@Repository
public interface EstadoEnvioRepository extends JpaRepository<EstadoEnvio, Integer> {
    Optional<EstadoEnvio> findByNombre(String nombre);
}
