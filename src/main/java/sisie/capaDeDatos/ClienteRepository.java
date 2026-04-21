package sisie.capaDeDatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sisie.capaDeDominio.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}