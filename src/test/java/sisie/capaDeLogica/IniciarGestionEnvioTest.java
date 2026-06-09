package sisie.capaDeLogica;
 
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
 
import java.util.List;
import java.util.Optional;
 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
 
import sisie.capaDeDatos.EnvioRepository;
import sisie.capaDeDominio.*;
 
/**
 * Pruebas unitarias del contrato de operación crítica "Iniciar Gestión del Envío".
 *
 * El contrato se encuentra expuesto en la capa de presentación mediante el
 * método iniciarGestionEnvio() del ControladorPrincipal. Sin embargo, dicho
 * método únicamente recibe la solicitud del usuario y delega la lógica de
 * negocio al método cambiarEstadoAEnProceso() de EnvioService.
 *
 * Por este motivo, las pruebas unitarias se realizan sobre EnvioService,
 * ya que es allí donde se implementan las responsabilidades definidas en el
 * contrato de operación:
 *
 * - Cambiar el estado del envío a "En proceso".
 * - Registrar el cambio en el historial (via patrón Observer).
 * - Persistir el cambio en la base de datos.
 *
 * Las dependencias externas (repositorios, observadores) se reemplazan con
 * mocks de Mockito para aislar la unidad bajo prueba y no afectar la base
 * de datos del sistema.
 */
public class IniciarGestionEnvioTest {
 
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
     * Corrida 1 - envío existente con estado Pendiente.
     *
     * Descripción:   Envío existente con estado inicial Pendiente.
     * Resultado esp: Envío cambiado a En proceso, cambio persistido y
     *                observadores notificados (historial registrado).
     */
    @Test
    void corrida1_iniciarGestionEnvioCorrectamente() {
        System.out.println("Corrida 1: iniciar gestión de envío pendiente");
 
        Envio envio = new Envio();
        envio.setIdEnvio(10);
        envio.setEstadoActual(EstadoProvider.getPendiente());
 
        when(envioRepository.findById(10)).thenReturn(Optional.of(envio));
 
        envioService.cambiarEstadoAEnProceso(10, "admin@sisie.com");
 
        // Verifica que el estado cambió correctamente
        assertTrue(envio.getEstadoActual() instanceof EstadoEnProceso,
                "El estado debe ser EstadoEnProceso después de iniciar la gestión");
 
        // Verifica que el envío fue persistido en la base de datos
        verify(envioRepository).save(envio);
 
        // Verifica que el observador fue notificado (registra el historial)
        verify(observadorEnvio).actualizar(envio);
 
        System.out.println("Resultado obtenido: envío cambiado a En proceso, persistido y observador notificado");
    }
 
    /**
     * Corrida 2 - Envío inexistente.
     *
     * Descripción:   El estado del envío cambia a En proceso y se guarda la
     *                modificación, pero el ID 999 no existe en el sistema.
     * Resultado esp: Se lanza la excepción "Envío no encontrado: ID 999".
     */
    @Test
    void corrida2_envioNoExiste() {
        System.out.println("Corrida 2: envío inexistente");
 
        when(envioRepository.findById(999)).thenReturn(Optional.empty());
 
        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> envioService.cambiarEstadoAEnProceso(999, "admin@sisie.com")
        );
 
        assertTrue(error.getMessage().contains("Envío no encontrado"),
                "El mensaje debe indicar que el envío no fue encontrado");
 
        // Verifica que nunca se intentó persistir nada
        verify(envioRepository, never()).save(any());
 
        System.out.println("Resultado obtenido: " + error.getMessage());
    }
 
    /**
     * Corrida 3 - Envío ya en estado En proceso, sin código de seguimiento.
     *
     * Descripción:   Envío existente que ya se encuentra con estado En proceso.
     *                Representa un intento de iniciar la gestión nuevamente.
     * Resultado esp: Se lanza una excepción: "No se puede iniciar el tránsito
     *                sin un código de seguimiento asignado".
     */
    @Test
    void corrida3_envioYaEnProceso() {
        System.out.println("Corrida 3: envío ya en gestión");
 
        Envio envio = new Envio();
        envio.setIdEnvio(11);
        envio.setEstadoActual(EstadoProvider.getEnProceso());
 
        when(envioRepository.findById(11)).thenReturn(Optional.of(envio));
 
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> envioService.cambiarEstadoAEnProceso(11, "admin@sisie.com")
        );
 
        // Verifica el mensaje exacto que lanza EstadoEnProceso.transicionar()
        assertTrue(error.getMessage().contains(
                "No se puede iniciar el tránsito sin un código de seguimiento asignado"),
                "El mensaje debe indicar que falta el código de seguimiento");
 
        // Verifica que nunca se intentó persistir nada
        verify(envioRepository, never()).save(any());
 
        System.out.println("Resultado obtenido: " + error.getMessage());
    }
 
    /**
     * Corrida 4 - Error al guardar el inicio de gestión.
     *
     * Descripción:   Envío existente con estado Pendiente, pero ocurre un
     *                error al guardar la actualización en la base de datos.
     * Resultado esp: Se propaga la excepción "Error al registrar el inicio
     *                de gestión".
     */
    @Test
    void corrida4_errorAlGuardarInicioGestion() {
        System.out.println("Corrida 4: error al guardar inicio de gestión");
 
        Envio envio = new Envio();
        envio.setIdEnvio(12);
        envio.setEstadoActual(EstadoProvider.getPendiente());
 
        when(envioRepository.findById(12)).thenReturn(Optional.of(envio));
        when(envioRepository.save(envio)).thenThrow(
                new RuntimeException("Error al registrar el inicio de gestión"));
 
        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> envioService.cambiarEstadoAEnProceso(12, "admin@sisie.com")
        );
 
        assertTrue(error.getMessage().contains("Error al registrar"),
                "El mensaje debe indicar que falló el registro");
 
        System.out.println("Resultado obtenido: " + error.getMessage());
    }
}