package sisie.capaDeLogica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sisie.capaDeDatos.EnvioRepository;
import sisie.capaDeDatos.ClienteRepository;
import sisie.capaDeDatos.VentaRepository;
import sisie.capaDeDatos.DireccionRepository;
import sisie.capaDeDatos.EstadoRepository;
import sisie.capaDeDatos.TransporteRepository;
import sisie.capaDeDatos.ProvinciaRepository;
import sisie.capaDeDatos.CiudadRepository;
import sisie.capaDeDatos.HistorialEnvioRepository;
import sisie.capaDeDatos.UsuarioRepository;

import sisie.capaDeDominio.*;

// Herramientas de Java y Spring
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional; 
import java.util.List;
import java.util.Random;
import java.math.BigDecimal;
import java.time.LocalDateTime;



import sisie.capaDeDominio.Envio;

import java.util.List;

@Service
public class EnvioService {

    @Autowired private EnvioRepository envioRepository;

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private DireccionRepository direccionRepository;
    @Autowired private EstadoRepository estadoRepository;
    @Autowired private TransporteRepository transporteRepository;
    @Autowired private ProvinciaRepository provinciaRepository;
    @Autowired private CiudadRepository ciudadRepository;
    @Autowired private HistorialEnvioRepository historialRepository;
    @Autowired private UsuarioRepository usuarioRepository; // Para el historial

    public long contarTotalEnvios() {
        return envioRepository.count();
    }

    public long contarEnviosPorEstado(String estado) {
        return envioRepository.countByEstadoNombre(estado);
    }

    public List<Envio> ObtenerEnviosPendientes() {
        return envioRepository.findByEstadoNombre("Pendiente");
    }

    @Transactional
    public void generarEnvioAleatorio(String emailUsuarioLogueado) {
        Random random = new Random();

        // 1. Obtener listas de datos maestros
        List<Provincia> provincias = provinciaRepository.findAll();
        List<Transporte> transportes = transporteRepository.findAll();
        Estado estadoPendiente = estadoRepository.findByNombre("Pendiente");
        Usuario usuario = usuarioRepository.findByEmail(emailUsuarioLogueado).orElse(null);

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
        e.setEstado(estadoPendiente);
        e.setTransporte(transportes.get(random.nextInt(transportes.size())));
        e.setCosto(new BigDecimal(random.nextInt(5000) + 1500));
        e.setFechaCreacion(LocalDateTime.now());
        e = envioRepository.save(e);

        // 6. Crear Historial
        HistorialEnvio h = new HistorialEnvio();
        h.setEnvio(e);
        h.setEstado(estadoPendiente);
        h.setFechaMovimiento(LocalDateTime.now());
        h.setMotivo("Generación automática");
        h.setUsuario(usuario);
        historialRepository.save(h);
    }


}
