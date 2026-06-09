package sisie.capaDeLogica;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import sisie.capaDeDatos.EnvioRepository;
import sisie.capaDeDominio.*;

/**
 * Pruebas unitarias del contrato de operación crítica "Actualizar Datos del Envío".
 *
 * Este caso de uso permite registrar o modificar el código de seguimiento de un envío.
 * Si el envío está "En proceso" y no tiene código, el sistema lo despacha y cambia
 * su estado a "En tránsito". Si el envío ya está "En tránsito" y tiene código,
 * permite modificar el código sin cambiar el estado final del envío.
 *
 * Las pruebas se realizan sobre EnvioService porque allí se encuentran las reglas
 * de negocio. El repositorio y el observador se reemplazan por mocks para no usar
 * una base de datos real durante la prueba.
 *
 * En las corridas correctas se verifica la invocación de actualizarSeguimientoYEstado(),
 * método del repositorio que ejecuta el procedimiento almacenado
 * sp_ActualizarSeguimientoYEstado.
 */
public class ActualizarDatosEnvioTest {

    private EnvioRepository envioRepository;
    private ObservadorEnvio observadorEnvio;
    private EnvioService envioService;

    @BeforeEach
    void inicializar() {
        envioRepository = mock(EnvioRepository.class);
        observadorEnvio = mock(ObservadorEnvio.class);

        envioService = new EnvioService();

        ReflectionTestUtils.setField(envioService, "envioRepository", envioRepository);
        ReflectionTestUtils.setField(envioService, "observadores", List.of(observadorEnvio));

        EstadoProvider.init(
                new EstadoPendiente(),
                new EstadoEnProceso(),
                new EstadoEnTransito(),
                new EstadoEntregado(),
                new EstadoNoEntregado()
        );
    }
    /**
     * Corrida 1 - despachar envío con código de seguimiento.
     *
     * Descripción:   Envío existente con estado inicial En proceso y sin código de seguimiento.
     * Datos entrada: idEnvio = 20, código = "COD-001", usuario = "admin@sisie.com".
     * Resultado esp: El código queda asignado, el estado cambia a En tránsito,
     *                se ejecuta el procedimiento almacenado de actualización y
     *                se notifica al observador para registrar el historial.
     */
    @Test
    void corrida1_despacharEnvioConCodigoSeguimiento() {
        System.out.println("Corrida 1: despachar envio con codigo de seguimiento");

        Envio envio = new Envio();
        envio.setIdEnvio(20);
        envio.setEstadoActual(EstadoProvider.getEnProceso());
        envio.setCodSeguimiento(null);

        when(envioRepository.findByCodSeguimiento("COD-001")).thenReturn(null);
        when(envioRepository.findById(20)).thenReturn(Optional.of(envio));

        envioService.actualizarDatosEnvio(20, "COD-001", "admin@sisie.com");

        assertEquals("COD-001", envio.getCodSeguimiento());
        assertTrue(envio.getEstadoActual() instanceof EstadoEnTransito);
        verify(envioRepository).actualizarSeguimientoYEstado(20, "COD-001", envio.getEstadoActual().getIdEstado());
        verify(observadorEnvio).actualizar(envio);

        System.out.println("Resultado obtenido: Envío despachado con éxito. Ahora está En Tránsito.");
    }
    /**
     * Corrida 2 - modificar código de seguimiento.
     *
     * Descripción:   Envío existente con estado En tránsito y con un código previo.
     * Datos entrada: idEnvio = 21, código nuevo = "COD-002-NUEVO".
     * Resultado esp: Se reemplaza el código anterior por el nuevo, el envío se
     *                mantiene en estado En tránsito, se ejecuta el procedimiento
     *                almacenado y se notifica al observador.
     */
    @Test
    void corrida2_modificarCodigoSeguimiento() {
        System.out.println("Corrida 2: modificar codigo de seguimiento");

        Envio envio = new Envio();
        envio.setIdEnvio(21);
        envio.setEstadoActual(EstadoProvider.getEnTransito());
        envio.setCodSeguimiento("COD-002");

        when(envioRepository.findByCodSeguimiento("COD-002-NUEVO")).thenReturn(null);
        when(envioRepository.findById(21)).thenReturn(Optional.of(envio));

        envioService.actualizarDatosEnvio(21, "COD-002-NUEVO", "admin@sisie.com");

        assertEquals("COD-002-NUEVO", envio.getCodSeguimiento());
        assertTrue(envio.getEstadoActual() instanceof EstadoEnTransito);
        verify(envioRepository).actualizarSeguimientoYEstado(21, "COD-002-NUEVO", envio.getEstadoActual().getIdEstado());
        verify(observadorEnvio).actualizar(envio);

        System.out.println("Resultado obtenido: Código de seguimiento modificado con éxito.");
    }
    /**
     * Corrida 3 - Envío inexistente.
     *
     * Descripción:   Se intenta actualizar un envío cuyo ID no existe en el sistema.
     * Datos entrada: idEnvio = 999, código = "COD-003".
     * Resultado esp: Se lanza una RuntimeException indicando que el envío no fue
     *                encontrado. No debe guardarse ningún cambio en la base de datos.
     */
    @Test
    void corrida3_envioNoExiste() {
        System.out.println("Corrida 3: envio inexistente");

        when(envioRepository.findByCodSeguimiento("COD-003")).thenReturn(null);
        when(envioRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> envioService.actualizarDatosEnvio(999, "COD-003", "admin@sisie.com")
        );

        assertTrue(error.getMessage().contains("Envío no encontrado: ID 999"));
        verify(envioRepository, never()).save(any());

        System.out.println("Resultado obtenido: Error al despachar el envío: " + error.getMessage());
    }
    /**
     * Corrida 4 - Código de seguimiento vacío.
     *
     * Descripción:   Se intenta actualizar un envío enviando una cadena vacía como
     *                código de seguimiento.
     * Datos entrada: idEnvio = 22, código = "".
     * Resultado esp: Se lanza una IllegalArgumentException porque el código de
     *                seguimiento es obligatorio. No debe persistirse ningún cambio.
     */
    @Test
    void corrida4_codigoSeguimientoVacio() {
        System.out.println("Corrida 4: codigo de seguimiento vacio");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> envioService.actualizarDatosEnvio(22, "", "admin@sisie.com")
        );

        assertTrue(error.getMessage().contains("El código de seguimiento no puede estar vacío."));
        verify(envioRepository, never()).save(any());

        System.out.println("Resultado obtenido: Error al despachar el envío: " + error.getMessage());
    }
    /**
     * Corrida 5 - Código de seguimiento duplicado.
     *
     * Descripción:   Se intenta asignar un código de seguimiento que ya pertenece
     *                a otro envío.
     * Datos entrada: código = "COD-DUPLICADO".
     * Resultado esp: Se lanza una IllegalArgumentException indicando que ya existe
     *                otro envío con el mismo código. No debe guardarse el cambio.
     */
    @Test
    void corrida5_codigoSeguimientoDuplicado() {
        System.out.println("Corrida 5: codigo de seguimiento duplicado");

        Envio envioDuplicado = new Envio();
        envioDuplicado.setIdEnvio(99);
        envioDuplicado.setCodSeguimiento("COD-DUPLICADO");

        when(envioRepository.findByCodSeguimiento("COD-DUPLICADO")).thenReturn(envioDuplicado);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> envioService.actualizarDatosEnvio(23, "COD-DUPLICADO", "admin@sisie.com")
        );

        assertTrue(error.getMessage().contains("Ya existe otro envío con el mismo código de seguimiento."));
        verify(envioRepository, never()).save(any());

        System.out.println("Resultado obtenido: Error al despachar el envío: " + error.getMessage());
    }
    /**
     * Corrida 6 - Código de seguimiento idéntico al actual.
     *
     * Descripción:   Se intenta guardar el mismo código de seguimiento que el envío
     *                ya tenía asignado.
     * Datos entrada: idEnvio = 24, código = "COD-004".
     * Resultado esp: Se lanza una IllegalArgumentException porque no hay cambios
     *                reales para registrar. No debe guardarse ni actualizarse nada.
     */
    @Test
    void corrida6_codigoSeguimientoIdenticoAlActual() {
        System.out.println("Corrida 6: codigo de seguimiento identico al actual");

        Envio envio = new Envio();
        envio.setIdEnvio(24);
        envio.setEstadoActual(EstadoProvider.getEnTransito());
        envio.setCodSeguimiento("COD-004");

        when(envioRepository.findByCodSeguimiento("COD-004")).thenReturn(envio);
        when(envioRepository.findById(24)).thenReturn(Optional.of(envio));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> envioService.actualizarDatosEnvio(24, "COD-004", "admin@sisie.com")
        );

        assertTrue(error.getMessage().contains(
                "El código de seguimiento ingresado es idéntico al actual. No se realizaron cambios."));
        verify(envioRepository, never()).save(any());

        System.out.println("Resultado obtenido: Error al modificar el seguimiento: " + error.getMessage());
    }

    
}