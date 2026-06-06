package sisie.capaDeDatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sisie.capaDeDominio.Envio;
import java.util.List;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {
    
    Long countByEstadoActualNombre(String nombreEstado); 
    List<Envio> findByEstadoActualNombre(String nombreEstado);
    List<Envio> findByEstadoActualNombreNot(String nombreEstado);
    Envio findByCodSeguimiento(String codSeguimiento);
}
