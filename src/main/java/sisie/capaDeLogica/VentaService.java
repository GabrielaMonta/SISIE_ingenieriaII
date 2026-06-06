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
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private CiudadRepository ciudadRepository;

    @Autowired
    private ProvinciaRepository provinciaRepository;

    @Autowired
    private TransporteRepository transporteRepository;

    @Autowired
    private EnvioService envioService;

    /**
     * Registra una venta en el sistema. Si la venta requiere envío,
     * se comunica con EnvioService para crear e instanciar el envío correspondiente.
     */
    @Transactional
    public Venta procesarVenta(Venta venta, Direccion direccionEnvio, Transporte transporte, BigDecimal costoEnvio, boolean requiereEnvio) {
        // 1. Guardar la venta en la base de datos
        Venta ventaGuardada = ventaRepository.save(venta);

        // 2. Si requiere envío, crear el envío asociado a esa venta usando la capa de lógica
        if (requiereEnvio && direccionEnvio != null && transporte != null) {
            envioService.registrarNuevoEnvio(ventaGuardada, direccionEnvio, transporte, costoEnvio);
        }

        return ventaGuardada;
    }

    /**
     * Genera una venta de prueba con datos aleatorios (Cliente, Dirección, Ciudad, Venta)
     * e invoca al método de negocio registrarNuevoEnvio de EnvioService.
     */
    @Transactional
    public void generarVentaYEnvioAleatorio() {
        Random random = new Random();
        List<Provincia> provincias = provinciaRepository.findAll();
        List<Transporte> transportes = transporteRepository.findAll();

        // 1. Crear Cliente con datos random
        String[] nombres = { "Juan", "Maria", "Ricardo", "Elena", "Lucas" };
        String[] apellidos = { "Perez", "Gomez", "Duarte", "Fernandez" };
        Cliente c = new Cliente();
        c.setNombre(nombres[random.nextInt(nombres.length)]);
        c.setApellido(apellidos[random.nextInt(apellidos.length)]);
        c.setDni(String.valueOf(10000000 + random.nextInt(80000000)));
        c = clienteRepository.save(c);

        // 2. Crear Ciudad y Dirección
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

        // 3. Crear Venta
        Venta v = new Venta();
        v.setCliente(c);
        v.setFechaCreacion(LocalDateTime.now());
        v = ventaRepository.save(v);

        // 4. Seleccionar Transporte y Costo aleatorios para el envío
        Transporte transporte = transportes.get(random.nextInt(transportes.size()));
        BigDecimal costo = new BigDecimal(random.nextInt(5000) + 1500);

        // 5. Invocar directamente al método de negocio de EnvioService
        envioService.registrarNuevoEnvio(v, d, transporte, costo);
    }
}
