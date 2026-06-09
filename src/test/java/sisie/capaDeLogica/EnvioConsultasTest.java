package sisie.capaDeLogica;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import sisie.capaDeDatos.EnvioRepository;
import sisie.capaDeDominio.*;
/**
 * Pruebas unitarias de consultas, métricas, filtros y consulta detallada de envíos.
 *
 * A diferencia de los otros tests, este archivo no prueba principalmente cambios de
 * estado, sino operaciones de lectura: contar envíos, obtener envíos por estado,
 * obtener envíos no pendientes, consultar detalle mediante procedimiento almacenado
 * y filtrar envíos por distintos criterios.
 *
 * El repositorio se simula con Mockito para no consultar una base de datos real.
 * En las pruebas del procedimiento almacenado se simula la fila Object[] que devolvería
 * sp_ConsultarEnviosDetallados.
 */
public class EnvioConsultasTest {

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

        EstadoEnvio pendiente = new EstadoPendiente();
        ReflectionTestUtils.setField(pendiente, "nombre", "Pendiente");
        ReflectionTestUtils.setField(pendiente, "idEstado", 1);

        EstadoEnvio enProceso = new EstadoEnProceso();
        ReflectionTestUtils.setField(enProceso, "nombre", "En proceso");
        ReflectionTestUtils.setField(enProceso, "idEstado", 2);

        EstadoEnvio enTransito = new EstadoEnTransito();
        ReflectionTestUtils.setField(enTransito, "nombre", "En transito");
        ReflectionTestUtils.setField(enTransito, "idEstado", 3);

        EstadoEnvio entregado = new EstadoEntregado();
        ReflectionTestUtils.setField(entregado, "nombre", "Entregado");
        ReflectionTestUtils.setField(entregado, "idEstado", 4);

        EstadoEnvio noEntregado = new EstadoNoEntregado();
        ReflectionTestUtils.setField(noEntregado, "nombre", "No entregado");
        ReflectionTestUtils.setField(noEntregado, "idEstado", 5);

        EstadoProvider.init(pendiente, enProceso, enTransito, entregado, noEntregado);
    }
    /**
     * Test 1 - Contar total de envíos.
     *
     * Descripción:   Se verifica que el servicio pueda consultar la cantidad total
     *                de envíos registrados.
     * Datos entrada: El repositorio simula que existen 15 envíos.
     * Resultado esp: El servicio devuelve 15 y se comprueba que llamó al método
     *                count() del repositorio.
     */
    @Test
    void testContarTotalEnvios() {
        System.out.println("Test: contarTotalEnvios");
        when(envioRepository.count()).thenReturn(15L);

        long resultado = envioService.contarTotalEnvios();

        assertEquals(15L, resultado);
        verify(envioRepository).count();
    }
    /**
     * Test 2 - Contar envíos por estado.
     *
     * Descripción:   Se verifica que el servicio cuente los envíos que poseen un
     *                estado determinado.
     * Datos entrada: estado = "Pendiente". El repositorio simula 3 coincidencias.
     * Resultado esp: El servicio devuelve 3 y se comprueba que llamó a
     *                countByEstadoActualNombre("Pendiente").
     */
    @Test
    void testContarEnviosPorEstado() {
        System.out.println("Test: contarEnviosPorEstado");
        when(envioRepository.countByEstadoActualNombre("Pendiente")).thenReturn(3L);

        long resultado = envioService.contarEnviosPorEstado("Pendiente");

        assertEquals(3L, resultado);
        verify(envioRepository).countByEstadoActualNombre("Pendiente");
    }
     /**
     * Test 3 - Obtener envíos por estado.
     *
     * Descripción:   Se solicita la lista de envíos que están en un estado específico.
     * Datos entrada: estado = "En proceso". El repositorio devuelve dos envíos.
     * Resultado esp: La lista no es nula, contiene 2 elementos y los envíos devueltos
     *                tienen asociado el observador del servicio.
     */
    @Test
    void testObtenerEnviosPorEstado() {
        System.out.println("Test: obtenerEnviosPorEstado");
        Envio e1 = new Envio();
        e1.setIdEnvio(1);
        Envio e2 = new Envio();
        e2.setIdEnvio(2);

        when(envioRepository.findByEstadoActualNombre("En proceso")).thenReturn(List.of(e1, e2));

        List<Envio> resultado = envioService.obtenerEnviosPorEstado("En proceso");

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(envioRepository).findByEstadoActualNombre("En proceso");

        // Verifica que se registraron los observadores en los envíos devueltos
        resultado.get(0).notificarObservadores();
        verify(observadorEnvio, times(1)).actualizar(e1);
    }
    /**
     * Test 4 - Obtener envíos no pendientes.
     *
     * Descripción:   Se verifica la consulta de envíos cuyo estado no sea Pendiente.
     * Datos entrada: El repositorio devuelve una lista con un envío no pendiente.
     * Resultado esp: La lista no es nula, contiene 1 elemento y se comprueba la
     *                llamada al método findByEstadoActualNombreNot("Pendiente").
     */
    @Test
    void testObtenerEnviosNoPendientes() {
        System.out.println("Test: obtenerEnviosNoPendientes");
        Envio e1 = new Envio();
        e1.setIdEnvio(10);

        when(envioRepository.findByEstadoActualNombreNot("Pendiente")).thenReturn(List.of(e1));

        List<Envio> resultado = envioService.obtenerEnviosNoPendientes();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(envioRepository).findByEstadoActualNombreNot("Pendiente");

        resultado.get(0).notificarObservadores();
        verify(observadorEnvio, times(1)).actualizar(e1);
    }
    /**
     * Test 5 - Consultar detalle de envío mediante procedimiento almacenado.
     *
     * Descripción:   Se simula que sp_ConsultarEnviosDetallados devuelve una fila
     *                completa con datos del envío, cliente, dirección, transporte y estado.
     * Datos entrada: idEnvio = 100.
     * Resultado esp: El servicio transforma el Object[] recibido en un Map con claves
     *                entendibles como idEnvio, codSeguimiento, costo, estadoActual, etc.
     */
    @Test
    void testObtenerDetalleEnvioPorSP_Existe() {
        System.out.println("Test: obtenerDetalleEnvioPorSP_Existe");
        Object[] fila = new Object[11];
        fila[0] = 100; // idEnvio
        fila[1] = "COD-100"; // codSeguimiento
        fila[2] = BigDecimal.valueOf(250.50); // costo
        fila[3] = "2026-06-09T10:00:00"; // fechaCreacion
        fila[4] = "En transito"; // estadoActual
        fila[5] = "Transportes ACME"; // transporteNombre
        fila[6] = "Juan"; // clienteNombre
        fila[7] = "Perez"; // clienteApellido
        fila[8] = "Av. Corrientes"; // calle
        fila[9] = 1234; // altura
        fila[10] = "1043"; // codigoPostal

        when(envioRepository.consultarEnvioDetallado(100)).thenReturn(Collections.singletonList(fila));

        Map<String, Object> det = envioService.obtenerDetalleEnvioPorSP(100);

        assertNotNull(det);
        assertEquals(100, det.get("idEnvio"));
        assertEquals("COD-100", det.get("codSeguimiento"));
        assertEquals(BigDecimal.valueOf(250.50), det.get("costo"));
        assertEquals("En transito", det.get("estadoActual"));
        assertEquals("Transportes ACME", det.get("transporteNombre"));
        assertEquals("Juan", det.get("clienteNombre"));
        assertEquals("Perez", det.get("clienteApellido"));
        assertEquals("Av. Corrientes", det.get("calle"));
        assertEquals(1234, det.get("altura"));
        assertEquals("1043", det.get("codigoPostal"));
    }
    /**
     * Test 6 - Consultar detalle sin código de seguimiento ni fecha.
     *
     * Descripción:   Se simula que el procedimiento almacenado devuelve un envío
     *                sin código de seguimiento y sin fecha de creación.
     * Datos entrada: idEnvio = 101, codSeguimiento = null, fechaCreacion = null.
     * Resultado esp: El servicio reemplaza el código nulo por "Sin asignar" y la
     *                fecha nula por una cadena vacía.
     */
    @Test
    void testObtenerDetalleEnvioPorSP_SinSeguimiento() {
        System.out.println("Test: obtenerDetalleEnvioPorSP_SinSeguimiento");
        Object[] fila = new Object[11];
        fila[0] = 101;
        fila[1] = null; // codSeguimiento is null
        fila[2] = BigDecimal.valueOf(120.00);
        fila[3] = null;
        fila[4] = "Pendiente";
        fila[5] = "Transportes ACME";
        fila[6] = "Juan";
        fila[7] = "Perez";
        fila[8] = "Av. Corrientes";
        fila[9] = 1234;
        fila[10] = "1043";

        when(envioRepository.consultarEnvioDetallado(101)).thenReturn(Collections.singletonList(fila));

        Map<String, Object> det = envioService.obtenerDetalleEnvioPorSP(101);

        assertNotNull(det);
        assertEquals("Sin asignar", det.get("codSeguimiento"));
        assertEquals("", det.get("fechaCreacion"));
    }
    /**
     * Test 7 - Consultar detalle de envío inexistente.
     *
     * Descripción:   Se simula que el procedimiento almacenado no devuelve registros
     *                para el ID consultado.
     * Datos entrada: idEnvio = 999.
     * Resultado esp: El servicio devuelve null, indicando que no encontró detalle.
     */
    @Test
    void testObtenerDetalleEnvioPorSP_Inexistente() {
        System.out.println("Test: obtenerDetalleEnvioPorSP_Inexistente");
        when(envioRepository.consultarEnvioDetallado(999)).thenReturn(Collections.emptyList());

        Map<String, Object> det = envioService.obtenerDetalleEnvioPorSP(999);

        assertNull(det);
    }
    /**
     * Test 8 - Filtrar envíos por distintos criterios.
     *
     * Descripción:   Se crean tres envíos ficticios con ventas, estados, códigos y
     *                fechas diferentes. Luego se prueban varios filtros sobre esa lista.
     * Datos entrada: lista de 3 envíos; filtros por idVenta, idEnvio, código,
     *                estado, fecha desde, fecha hasta y rango de fechas.
     * Resultado esp: Cada filtro devuelve únicamente los envíos que cumplen el criterio.
     */
    @Test
    void testFiltrarEnvios() {
        System.out.println("Test: filtrarEnvios");

        Venta v1 = new Venta();
        v1.setIdVenta(10);
        Venta v2 = new Venta();
        v2.setIdVenta(20);

        Envio e1 = new Envio();
        e1.setIdEnvio(1);
        e1.setVenta(v1);
        e1.setEstadoActual(EstadoProvider.getPendiente());
        e1.setCodSeguimiento(null);
        e1.setFechaCreacion(LocalDateTime.of(2026, 6, 1, 12, 0));

        Envio e2 = new Envio();
        e2.setIdEnvio(2);
        e2.setVenta(v2);
        e2.setEstadoActual(EstadoProvider.getEnProceso());
        e2.setCodSeguimiento("COD-123");
        e2.setFechaCreacion(LocalDateTime.of(2026, 6, 5, 12, 0));

        Envio e3 = new Envio();
        e3.setIdEnvio(3);
        e3.setVenta(v1);
        e3.setEstadoActual(EstadoProvider.getEnTransito());
        e3.setCodSeguimiento("ABC-987");
        e3.setFechaCreacion(LocalDateTime.of(2026, 6, 9, 12, 0));

        when(envioRepository.findAll()).thenReturn(List.of(e1, e2, e3));

        // 1. Filtrar por idVenta = 10
        List<Envio> filVenta = envioService.filtrarEnvios(10, null, null, null, null);
        assertEquals(2, filVenta.size());
        assertTrue(filVenta.contains(e1));
        assertTrue(filVenta.contains(e3));

        // Filtrar por idEnvio = 2 (se pasa en idVenta)
        List<Envio> filEnvio = envioService.filtrarEnvios(2, null, null, null, null);
        assertEquals(1, filEnvio.size());
        assertTrue(filEnvio.contains(e2));

        // 2. Filtrar por codSeguimiento (parcial e insensible a mayúsculas/minúsculas)
        List<Envio> filCod = envioService.filtrarEnvios(null, "cod-12", null, null, null);
        assertEquals(1, filCod.size());
        assertTrue(filCod.contains(e2));

        // 3. Filtrar por estado "NoPendientes"
        List<Envio> filNoPend = envioService.filtrarEnvios(null, null, "NoPendientes", null, null);
        assertEquals(2, filNoPend.size());
        assertFalse(filNoPend.contains(e1));

        // Filtrar por estado "Todos"
        List<Envio> filTodos = envioService.filtrarEnvios(null, null, "Todos", null, null);
        assertEquals(3, filTodos.size());

        // Filtrar por estado específico "En transito"
        List<Envio> filTrans = envioService.filtrarEnvios(null, null, "En transito", null, null);
        assertEquals(1, filTrans.size());
        assertTrue(filTrans.contains(e3));

        // 4. Filtrar por fechas
        // Desde 2026-06-02
        List<Envio> filDesde = envioService.filtrarEnvios(null, null, null, "2026-06-02", null);
        assertEquals(2, filDesde.size());
        assertFalse(filDesde.contains(e1));

        // Hasta 2026-06-06
        List<Envio> filHasta = envioService.filtrarEnvios(null, null, null, null, "2026-06-06");
        assertEquals(2, filHasta.size());
        assertFalse(filHasta.contains(e3));

        // Rango completo 2026-06-02 a 2026-06-06
        List<Envio> filRango = envioService.filtrarEnvios(null, null, null, "2026-06-02", "2026-06-06");
        assertEquals(1, filRango.size());
        assertTrue(filRango.contains(e2));
    }
}
