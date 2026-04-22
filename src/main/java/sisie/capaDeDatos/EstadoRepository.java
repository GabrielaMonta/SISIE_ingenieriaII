package sisie.capaDeDatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sisie.capaDeDominio.Estado;
import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Integer> {
    Optional<Estado> findByNombre(String nombre); // Para buscar "Pendiente"
}
