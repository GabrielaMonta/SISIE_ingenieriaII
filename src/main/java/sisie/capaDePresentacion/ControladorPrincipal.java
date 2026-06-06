package sisie.capaDePresentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sisie.capaDeLogica.EnvioService;
import sisie.capaDeLogica.VentaService;
import sisie.capaDeDatos.UsuarioRepository;

@Controller
public class ControladorPrincipal {

    @Autowired
    private EnvioService envioService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @GetMapping("/logistica")
    public String showLogisticaPanel(Model model, Principal principal) {
        if (principal != null) {
            //Obtiene usuario logeado
            String email = principal.getName();
            //controlador-UsuarioRepository-Base de datos
            usuarioRepository.findByEmail(email).ifPresent(usuario -> {
                model.addAttribute("nombreUsuario", usuario.getNombre() + " " + usuario.getApellido());
            });
        }
        
        // Obtenemos todos los contadores de la base usando el EnvioService
        model.addAttribute("totalEnvios", envioService.contarTotalEnvios());
        model.addAttribute("nuevosEnvios", envioService.contarEnviosPorEstado("Pendiente"));
        model.addAttribute("enProceso", envioService.contarEnviosPorEstado("En proceso"));
        model.addAttribute("entregados", envioService.contarEnviosPorEstado("Entregado"));
        
        // Llamado a la función especificada por el usuario
        model.addAttribute("enviosPendientes", envioService.obtenerEnviosPorEstado("Pendiente"));

        return "panel-logistica";
    }

    @GetMapping("/envios")
    public String showEnvios(
            @RequestParam(value = "idVenta", required = false) Integer idVenta,
            @RequestParam(value = "codSeguimiento", required = false) String codSeguimiento,
            @RequestParam(value = "estado", required = false, defaultValue = "NoPendientes") String estado,
            @RequestParam(value = "fechaDesde", required = false) String fechaDesde,
            @RequestParam(value = "fechaHasta", required = false) String fechaHasta,
            Model model,
            Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            usuarioRepository.findByEmail(email).ifPresent(usuario -> {
                model.addAttribute("nombreUsuario", usuario.getNombre() + " " + usuario.getApellido());
            });
        }
        model.addAttribute("envios", envioService.filtrarEnvios(idVenta, codSeguimiento, estado, fechaDesde, fechaHasta));
        
        model.addAttribute("filtroIdVenta", idVenta);
        model.addAttribute("filtroCodSeguimiento", codSeguimiento);
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroFechaDesde", fechaDesde);
        model.addAttribute("filtroFechaHasta", fechaHasta);
        
        return "gestion-envios";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @PostMapping("/logistica/generar-test")
    public String generarTest(Principal principal) {
        // principal.getName() nos da el email del usuario que está logueado
        ventaService.generarVentaYEnvioAleatorio();
        return "redirect:/logistica";
    }

    //Procesa la acción de iniciar gestión de un envío.
    //Cambia el estado del envío a 'En proceso'.
    @GetMapping("/envios/gestionar/{id}")
    public String iniciarGestionEnvio(@PathVariable("id") Integer idEnvio, Principal principal) {
        // principal.getName() nos da el email del usuario que está logueado
        // Llamamos a la capa de lógica para procesar el cambio
        envioService.cambiarEstadoAEnProceso(idEnvio, principal.getName());
        
        // Redirigimos de vuelta al panel de logística para ver la tabla actualizada
        return  "redirect:/logistica";
    }

    @PostMapping("/envios/despachar")
    public String despacharEnvio(@RequestParam("idEnvio") Integer idEnvio,
                                 @RequestParam("codSeguimiento") String codSeguimiento,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            envioService.despacharOEditarSeguimiento(idEnvio, codSeguimiento, principal != null ? principal.getName() : null);
            redirectAttributes.addFlashAttribute("mensajeExito", "Código de seguimiento registrado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar el seguimiento: " + e.getMessage());
        }
        return "redirect:/envios";
    }

    @PostMapping("/envios/registrar-resultado")
    public String registrarResultado(@RequestParam("idEnvio") Integer idEnvio,
                                     @RequestParam("resultado") String resultado,
                                     @RequestParam("motivo") String motivo,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            envioService.registrarResultado(idEnvio, resultado, motivo, principal != null ? principal.getName() : null);
            redirectAttributes.addFlashAttribute("mensajeExito", "Resultado de entrega registrado con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar el resultado: " + e.getMessage());
        }
        return "redirect:/envios";
    }

    @GetMapping("/envios/historial/{id}")
    @ResponseBody
    public List<Map<String, Object>> verHistorial(@PathVariable("id") Integer idEnvio) {
        List<sisie.capaDeDominio.HistorialEnvio> historial = envioService.obtenerHistorial(idEnvio);
        List<Map<String, Object>> response = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (sisie.capaDeDominio.HistorialEnvio h : historial) {
            Map<String, Object> map = new HashMap<>();
            map.put("fecha", h.getFechaMovimiento().format(formatter));
            map.put("estado", h.getEstado().getNombre());
            map.put("motivo", h.getMotivo());
            map.put("usuario", h.getUsuario() != null ? (h.getUsuario().getNombre() + " " + h.getUsuario().getApellido()) : "Sistema");
            response.add(map);
        }
        return response;
    }
}