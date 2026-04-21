package sisie.capaDeDatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sisie.capaDeDominio.Ciudad;

@Repository
public interface CiudadRepository extends JpaRepository<Ciudad, Integer> {
}