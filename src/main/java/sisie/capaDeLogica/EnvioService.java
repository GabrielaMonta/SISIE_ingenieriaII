package sisie.capaDeLogica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisie.capaDeDatos.EnvioRepository;
import sisie.capaDeDatos.TransporteRepository;
import sisie.capaDeDatos.HistorialEnvioRepository;
import sisie.capaDeDominio.*;
import java.math.BigDecimal;
import java.util.List;

@Service
public class EnvioService {

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private TransporteRepository transporteRepository;

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

    // Despacha un envio, asigna codigo de seguimiento y cambia el estado a En Tránsito
    @Transactional
    public void despacharEnvio(Integer idEnvio, String codSeguimiento, String emailUsuarioLogueado) {
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

        // Asignar código de seguimiento
        envio.setCodSeguimiento(codSeguimiento.trim());

        // Transicionar a En Tránsito
        envio.transicionar();

        // Persistir el cambio
        envioRepository.save(envio);
    }

    // Registra el resultado de un envio, cambia el estado a Entregado o No Entregado
    @Transactional
    public void registrarResultado(Integer idEnvio, String resultado, String emailUsuarioLogueado) {
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

        // Ejecutar la transición mediante patrón State
        envio.registrarResultado(estadoFinal);

        // Guardar cambios
        envioRepository.save(envio);
    }

    // Obtiene el historial de envios ordenado por fecha
    public List<HistorialEnvio> obtenerHistorial(Integer idEnvio) {
        return historialRepository.findByEnvioIdEnvioOrderByFechaMovimientoAsc(idEnvio);
    }
}
