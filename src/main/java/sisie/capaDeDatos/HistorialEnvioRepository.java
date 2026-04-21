package sisie.capaDeDatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sisie.capaDeDominio.HistorialEnvio;

@Repository
public interface HistorialEnvioRepository extends JpaRepository<HistorialEnvio, Integer> {
}