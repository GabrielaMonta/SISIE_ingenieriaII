package sisie.capaDeLogica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisie.capaDeDatos.EnvioRepository;
import sisie.capaDeDatos.HistorialEnvioRepository;
import sisie.capaDeDominio.*;
import java.math.BigDecimal;
import java.util.List;

@Service
public class EnvioService {

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private HistorialEnvioRepository historialRepository;

    @Autowired
    private List<ObservadorEnvio> observadores;

    // Métodos auxiliares para registrar observadores en los envíos
    private Envio registrarObservadores(Envio envio) {
        if (envio != null) {
            for (ObservadorEnvio obs : observadores) {
                envio.agregarObservador(obs);
            }
        }
        return envio;
    }

    private List<Envio> registrarObservadores(List<Envio> envios) {
        if (envios != null) {
            for (Envio e : envios) {
                registrarObservadores(e);
            }
        }
        return envios;
    }

    // Métodos de consulta
    public long contarTotalEnvios() {
        return envioRepository.count();
    }

    public long contarEnviosPorEstado(String estado) {
        return envioRepository.countByEstadoActualNombre(estado);
    }

    public List<Envio> obtenerEnviosPorEstado(String estado) {
        List<Envio> envios = envioRepository.findByEstadoActualNombre(estado);
        return registrarObservadores(envios);
    }

    public List<Envio> obtenerEnviosNoPendientes() {
        List<Envio> envios = envioRepository.findByEstadoActualNombreNot("Pendiente");
        return registrarObservadores(envios);
    }

    /**
     * Registra un nuevo envío en el sistema usando el constructor limpio de la entidad
     * e inicializando los observadores para guardar el historial inicial.
     */
    @Transactional
    public Envio registrarNuevoEnvio(Venta venta, Direccion direccion, Transporte transporte, BigDecimal costo) {
        EstadoEnvio estadoPendiente = EstadoProvider.getPendiente();

        // Instanciación usando el constructor nuevo de Envio
        Envio e = new Envio(venta, direccion, estadoPendiente, transporte, costo);

        // Registrar observadores
        registrarObservadores(e);
        e = envioRepository.save(e);

        // Notificar para guardar el historial inicial
        e.notificarObservadores();

        return e;
    }

    // Busca un envío y cambia su estado a 'En proceso' usando patrones
    @Transactional
    public void cambiarEstadoAEnProceso(Integer idEnvio, String emailUsuarioLogueado) {
        Envio envio = envioRepository.findById(idEnvio)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado: ID " + idEnvio));

        // Registrar observadores
        registrarObservadores(envio);

        // Cambiar estado mediante patrón State (el cual gatilla notificaciones automáticas)
        envio.IniciarGestion();

        // Persistir el cambio
        envioRepository.save(envio);
    }

    // Registra o edita el código de seguimiento de un envío
    @Transactional
    public void despacharOEditarSeguimiento(Integer idEnvio, String codSeguimiento, String emailUsuarioLogueado) {
        if (codSeguimiento == null || codSeguimiento.trim().isEmpty()) {
            throw new IllegalArgumentException("El código de seguimiento no puede estar vacío.");
        }
        Envio duplicado = envioRepository.findByCodSeguimiento(codSeguimiento.trim());
        if (duplicado != null && !duplicado.getIdEnvio().equals(idEnvio)) {
            throw new IllegalArgumentException("Ya existe otro envío con el mismo código de seguimiento.");
        }

        Envio envio = envioRepository.findById(idEnvio)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado: ID " + idEnvio));

        // Registrar observadores
        registrarObservadores(envio);

        String oldCod = envio.getCodSeguimiento();
        if (oldCod != null && oldCod.trim().equalsIgnoreCase(codSeguimiento.trim())) {
            throw new IllegalArgumentException("El código de seguimiento ingresado es idéntico al actual. No se realizaron cambios.");
        }

        envio.setCodSeguimiento(codSeguimiento.trim());

        if (oldCod == null || oldCod.trim().isEmpty()) {
            // Si el envío no tenía seguimiento, pasa automáticamente al estado En Tránsito
            envio.setMotivoTransicion("Asignación de código de seguimiento");
            envio.transicionar();
            envio.setMotivoTransicion(null);
        } else {
            // Si ya tenía seguimiento y únicamente se modifica el código, el estado permanece en En Tránsito
            envio.setMotivoTransicion("Corrección de código de seguimiento");
            envio.notificarObservadores();
            envio.setMotivoTransicion(null);
        }

        envioRepository.save(envio);
    }

    // Registra el resultado de un envio, cambia el estado a Entregado o No Entregado con motivo
    @Transactional
    public void registrarResultado(Integer idEnvio, String resultado, String motivo, String emailUsuarioLogueado) {
        Envio envio = envioRepository.findById(idEnvio)
                .orElseThrow(() -> new RuntimeException("Envío no encontrado: ID " + idEnvio));

        // Registrar observadores
        registrarObservadores(envio);

        EstadoEnvio estadoFinal;
        if ("Entregado".equalsIgnoreCase(resultado)) {
            estadoFinal = EstadoProvider.getEntregado();
        } else if ("No Entregado".equalsIgnoreCase(resultado) || "NoEntregado".equalsIgnoreCase(resultado)) {
            estadoFinal = EstadoProvider.getNoEntregado();
        } else {
            throw new IllegalArgumentException("Resultado no válido: " + resultado);
        }

        // Si se especificó un motivo, lo asignamos antes de transicionar (con truncado de seguridad a 150 caracteres)
        if (motivo != null && !motivo.trim().isEmpty()) {
            String motivoFinal = motivo.trim();
            if (motivoFinal.length() > 150) {
                motivoFinal = motivoFinal.substring(0, 150);
            }
            envio.setMotivoTransicion(motivoFinal);
        }

        // Ejecutar la transición mediante patrón State
        envio.registrarResultado(estadoFinal);

        // Limpiar el motivo de la transición
        envio.setMotivoTransicion(null);

        // Guardar cambios
        envioRepository.save(envio);
    }

    // Filtra los envíos aplicando criterios combinados
    public List<Envio> filtrarEnvios(Integer idVenta, String codSeguimiento, String estado, String fechaDesdeStr, String fechaHastaStr) {
        List<Envio> todos = envioRepository.findAll();
        java.time.LocalDate fechaDesde = (fechaDesdeStr != null && !fechaDesdeStr.trim().isEmpty()) ? java.time.LocalDate.parse(fechaDesdeStr) : null;
        java.time.LocalDate fechaHasta = (fechaHastaStr != null && !fechaHastaStr.trim().isEmpty()) ? java.time.LocalDate.parse(fechaHastaStr) : null;

        List<Envio> filtrados = todos.stream().filter(e -> {
            // 1. ID Venta
            if (idVenta != null) {
                if (e.getVenta() == null || !e.getVenta().getIdVenta().equals(idVenta)) {
                    return false;
                }
            }
            // 2. Código de seguimiento
            if (codSeguimiento != null && !codSeguimiento.trim().isEmpty()) {
                if (e.getCodSeguimiento() == null || !e.getCodSeguimiento().toLowerCase().contains(codSeguimiento.trim().toLowerCase())) {
                    return false;
                }
            }
            // 3. Estado
            if (estado != null && !estado.trim().isEmpty()) {
                if ("NoPendientes".equalsIgnoreCase(estado)) {
                    if ("Pendiente".equalsIgnoreCase(e.getEstadoActual().getNombre())) {
                        return false;
                    }
                } else if (!"Todos".equalsIgnoreCase(estado)) {
                    if (!e.getEstadoActual().getNombre().equalsIgnoreCase(estado)) {
                        return false;
                    }
                }
            }
            // 4. Rango de Fechas
            if (fechaDesde != null) {
                if (e.getFechaCreacion().toLocalDate().isBefore(fechaDesde)) {
                    return false;
                }
            }
            if (fechaHasta != null) {
                if (e.getFechaCreacion().toLocalDate().isAfter(fechaHasta)) {
                    return false;
                }
            }
            return true;
        }).collect(java.util.stream.Collectors.toList());

        return registrarObservadores(filtrados);
    }

    // Obtiene el historial de envios ordenado por fecha
    public List<HistorialEnvio> obtenerHistorial(Integer idEnvio) {
        return historialRepository.findByEnvioIdEnvioOrderByFechaMovimientoAsc(idEnvio);
    }
}
