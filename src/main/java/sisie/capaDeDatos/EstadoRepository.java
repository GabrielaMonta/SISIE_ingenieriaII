package sisie.capaDeDatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sisie.capaDeDominio.Estado;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Integer> {
}
