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
 * Pruebas unitarias del contrato de operación crítica "Registrar Resultado del Envío".
 *
 * Este caso de uso representa el cierre del ciclo de vida del envío. Permite cambiar
 * un envío que está "En tránsito" a uno de sus estados finales: "Entregado" o
 * "No entregado".
 *
 * Las pruebas se realizan sobre EnvioService, porque allí se valida el estado actual
 * del envío, se aplica la transición al estado final, se guarda el cambio y se notifica
 * a los observadores para registrar la trazabilidad en el historial.
 */
public class RegistrarResultadoEnvioTest {

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
     * Corrida 1 - Caso feliz: registrar resultado Entregado.
     *
     * Descripción:   Envío existente con estado En tránsito y código de seguimiento.
     * Datos entrada: idEnvio = 30, resultado = "Entregado", motivo = "Entrega exitosa".
     * Resultado esp: El estado cambia a Entregado, el envío se guarda y se notifica
     *                al observador para registrar el historial.
     */
    @Test
    void corrida1_registrarResultadoEntregado() {
        System.out.println("Corrida 1: registrar resultado Entregado");

        Envio envio = new Envio();
        envio.setIdEnvio(30);
        envio.setEstadoActual(EstadoProvider.getEnTransito());
        envio.setCodSeguimiento("COD-001");

        when(envioRepository.findById(30)).thenReturn(Optional.of(envio));

        envioService.registrarResultadoEnvio(30, "Entregado", "Entrega exitosa", "admin@sisie.com");

        assertTrue(envio.getEstadoActual() instanceof EstadoEntregado);
        verify(envioRepository).save(envio);
        verify(observadorEnvio).actualizar(envio);

        System.out.println("Resultado de entrega registrado con éxito.");
    }
    /**
     * Corrida 2 - Caso feliz: registrar resultado No Entregado.
     *
     * Descripción:   Envío existente con estado En tránsito y código de seguimiento.
     * Datos entrada: idEnvio = 31, resultado = "No Entregado", motivo = "Cliente ausente".
     * Resultado esp: El estado cambia a No entregado, el envío se guarda y se notifica
     *                al observador para registrar el historial.
     */
    @Test
    void corrida2_registrarResultadoNoEntregado() {
        System.out.println("Corrida 2: registrar resultado No Entregado");

        Envio envio = new Envio();
        envio.setIdEnvio(31);
        envio.setEstadoActual(EstadoProvider.getEnTransito());
        envio.setCodSeguimiento("COD-002");

        when(envioRepository.findById(31)).thenReturn(Optional.of(envio));

        envioService.registrarResultadoEnvio(31, "No Entregado", "Cliente ausente", "admin@sisie.com");

        assertTrue(envio.getEstadoActual() instanceof EstadoNoEntregado);
        verify(envioRepository).save(envio);
        verify(observadorEnvio).actualizar(envio);

        System.out.println("Resultado de entrega registrado con éxito.");
    }
    /**
     * Corrida 3 - Envío inexistente.
     *
     * Descripción:   Se intenta registrar un resultado final para un envío que no
     *                existe en el sistema.
     * Datos entrada: idEnvio = 999, resultado = "Entregado".
     * Resultado esp: Se lanza una RuntimeException indicando que el envío no fue
     *                encontrado. No debe guardarse ningún cambio.
     */
    @Test
    void corrida3_envioNoExiste() {
        System.out.println("Corrida 3: envio inexistente");

        when(envioRepository.findById(999)).thenReturn(Optional.empty());

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> envioService.registrarResultadoEnvio(999, "Entregado", null, "admin@sisie.com")
        );

        assertTrue(error.getMessage().contains("Envío no encontrado: ID 999"));
        verify(envioRepository, never()).save(any());

        System.out.println("Error al registrar el resultado: " + error.getMessage());
    }
    /**
     * Corrida 4 - Envío no se encuentra en tránsito.
     *
     * Descripción:   Se intenta registrar resultado final para un envío que todavía
     *                está Pendiente.
     * Datos entrada: idEnvio = 32, estado inicial = Pendiente.
     * Resultado esp: Se lanza una IllegalStateException porque solo se puede registrar
     *                resultado final desde el estado En tránsito. No debe guardarse.
     */
    @Test
    void corrida4_envioNoEstaEnTransito() {
        System.out.println("Corrida 4: envio no esta en transito");

        Envio envio = new Envio();
        envio.setIdEnvio(32);
        envio.setEstadoActual(EstadoProvider.getPendiente());

        when(envioRepository.findById(32)).thenReturn(Optional.of(envio));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> envioService.registrarResultadoEnvio(32, "Entregado", null, "admin@sisie.com")
        );

        assertTrue(error.getMessage().contains(
                "Solo se puede registrar el resultado final desde el estado En tránsito."));
        verify(envioRepository, never()).save(any());

        System.out.println("Error al registrar el resultado: " + error.getMessage());
    }
    /**
     * Corrida 5 - Envío ya tiene condición final.
     *
     * Descripción:   Se intenta registrar nuevamente un resultado para un envío que
     *                ya está Entregado.
     * Datos entrada: idEnvio = 33, estado inicial = Entregado.
     * Resultado esp: Se lanza una IllegalStateException porque un envío finalizado
     *                no puede volver a registrar resultado. No debe guardarse.
     */
    @Test
    void corrida5_envioYaTieneCondicionFinal() {
        System.out.println("Corrida 5: envio ya tiene condicion final");

        Envio envio = new Envio();
        envio.setIdEnvio(33);
        envio.setEstadoActual(EstadoProvider.getEntregado());
        envio.setCodSeguimiento("COD-003");

        when(envioRepository.findById(33)).thenReturn(Optional.of(envio));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> envioService.registrarResultadoEnvio(33, "Entregado", null, "admin@sisie.com")
        );

        assertTrue(error.getMessage().contains(
                "Solo se puede registrar el resultado final desde el estado En tránsito."));
        verify(envioRepository, never()).save(any());

        System.out.println("Error al registrar el resultado: " + error.getMessage());
    }
    /**
     * Corrida 6 - Error al guardar el resultado.
     *
     * Descripción:   El envío existe y está En tránsito, pero el repositorio falla
     *                cuando intenta guardar el cambio.
     * Datos entrada: idEnvio = 34, resultado = "Entregado".
     * Resultado esp: Se propaga una RuntimeException con el mensaje de error del
     *                guardado. Esto simula una falla de persistencia.
     */
    @Test
    void corrida6_errorAlGuardarResultado() {
        System.out.println("Corrida 6: error al guardar resultado");

        Envio envio = new Envio();
        envio.setIdEnvio(34);
        envio.setEstadoActual(EstadoProvider.getEnTransito());
        envio.setCodSeguimiento("COD-004");

        when(envioRepository.findById(34)).thenReturn(Optional.of(envio));
        when(envioRepository.save(envio)).thenThrow(
                new RuntimeException("Error al registrar el resultado del envío"));

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> envioService.registrarResultadoEnvio(34, "Entregado", null, "admin@sisie.com")
        );

        assertTrue(error.getMessage().contains("Error al registrar el resultado del envío"));

        System.out.println(" " + error.getMessage());
    }
}