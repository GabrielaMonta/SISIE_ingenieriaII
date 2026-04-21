package sisie.capaDeDatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sisie.capaDeDominio.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {
}
