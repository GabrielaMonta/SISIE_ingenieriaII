package sisie.capaDeDatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sisie.capaDeDominio.Envio;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {
}
