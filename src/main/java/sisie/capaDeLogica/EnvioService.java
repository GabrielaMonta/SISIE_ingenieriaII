package sisie.capaDeLogica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sisie.capaDeDatos.*;
import sisie.capaDeDominio.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class EnvioService {

    @Autowired private EnvioRepository envioRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private DireccionRepository direccionRepository;
    @Autowired private EstadoEnvioRepository estadoEnvioRepository;
    @Autowired private TransporteRepository transporteRepository;
    @Autowired private ProvinciaRepository provinciaRepository;
    @Autowired private CiudadRepository ciudadRepository;
    @Autowired private HistorialEnvioRepository historialRepository;
    @Autowired private UsuarioRepository usuarioRepository;

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

    public long contarTotalEnvios() {
        return envioRepository.count();
    }

    public long contarEnviosPorEstado(String estado) {
        return envioRepository.countByEstadoActualNombre(estado);
    }

    // Unificación de la consulta de envíos por estado
    public List<Envio> obtenerEnviosPorEstado(String estado) {
        List<Envio> envios = envioRepository.findByEstadoActualNombre(estado);
        return registrarObservadores(envios);
    }

    @Transactional
    public void generarEnvioAleatorio(String emailUsuarioLogueado) {
        Random random = new Random();

        // 1. Obtener listas de datos maestros
        List<Provincia> provincias = provinciaRepository.findAll();
        List<Transporte> transportes = transporteRepository.findAll();
        EstadoEnvio estadoPendiente = EstadoProvider.getPendiente();

        // 2. Crear Cliente con datos random
        String[] nombres = {"Juan", "Maria", "Ricardo", "Elena", "Lucas"};
        String[] apellidos = {"Perez", "Gomez", "Duarte", "Fernandez"};
        Cliente c = new Cliente();
        c.setNombre(nombres[random.nextInt(nombres.length)]);
        c.setApellido(apellidos[random.nextInt(apellidos.length)]);
        c.setDni(String.valueOf(10000000 + random.nextInt(80000000)));
        c = clienteRepository.save(c);

        // 3. Crear Ciudad y Dirección
        Ciudad ciu = new Ciudad();
        ciu.setNombre("Ciudad " + (random.nextInt(900) + 100));
        ciu.setProvincia(provincias.get(random.nextInt(provincias.size())));
        ciu = ciudadRepository.save(ciu);

        Direccion d = new Direccion();
        d.setCalle("Calle Falsa");
        d.setAltura(String.valueOf(random.nextInt(5000)));
        d.setCodigoPostal(String.valueOf(random.nextInt(9000) + 1000));
        d.setCliente(c);
        d.setCiudad(ciu);
        d = direccionRepository.save(d);

        // 4. Crear Venta
        Venta v = new Venta();
        v.setCliente(c);
        v.setFechaCreacion(LocalDateTime.now());
        v = ventaRepository.save(v);

        // 5. Crear Envío
        Envio e = new Envio();
        e.setVenta(v);
        e.setDireccion(d);
        e.setEstadoActual(estadoPendiente);
        e.setTransporte(transportes.get(random.nextInt(transportes.size())));
        e.setCosto(new BigDecimal(random.nextInt(5000) + 1500));
        e.setFechaCreacion(LocalDateTime.now());
        
        // Registrar observadores antes de disparar la primera notificación
        registrarObservadores(e);
        e = envioRepository.save(e);

        // 6. Notificar a los observadores para registrar en historial
        e.notificarObservadores();
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
}
