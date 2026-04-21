package sisie.capaDeDatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sisie.capaDeDominio.Transporte;

@Repository
public interface TransporteRepository extends JpaRepository<Transporte, Integer> {
}