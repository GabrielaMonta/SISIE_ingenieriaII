package sisie.capaDeDatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sisie.capaDeDominio.Envio;
import java.util.List;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {
    
    Long countByEstadoNombre(String nombreEstado); 
    List<Envio> findByEstadoNombre(String nombreEstado);
    List<Envio> findByEstadoNombreNot(String nombreEstado);
}
