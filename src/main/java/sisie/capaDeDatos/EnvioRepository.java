package sisie.capaDeDatos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sisie.capaDeDominio.Envio;
import java.util.List;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {
    
    Long countByEstadoActualNombre(String nombreEstado); 
    List<Envio> findByEstadoActualNombre(String nombreEstado);
    List<Envio> findByEstadoActualNombreNot(String nombreEstado);
    Envio findByCodSeguimiento(String codSeguimiento);

    @Modifying
    @Transactional
    @Query(value = "EXEC sp_ActualizarSeguimientoYEstado :id_envio, :cod_seguimiento, :id_estado", nativeQuery = true)
    void actualizarSeguimientoYEstado(
        @Param("id_envio") Integer idEnvio,
        @Param("cod_seguimiento") String codSeguimiento,
        @Param("id_estado") Integer idEstado
    );

    @Query(value = "EXEC sp_ConsultarEnviosDetallados :id_envio, NULL, NULL, NULL", nativeQuery = true)
    List<Object[]> consultarEnvioDetallado(@Param("id_envio") Integer idEnvio);
}
