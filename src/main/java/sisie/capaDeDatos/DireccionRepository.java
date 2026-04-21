package sisie.capaDeDatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sisie.capaDeDominio.Direccion;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Integer> {
}