package sisie.capaDePresentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import java.security.Principal;
import sisie.capaDeLogica.EnvioService;
import sisie.capaDeDatos.UsuarioRepository;

@Controller
public class ControladorPrincipal {

    @Autowired
    private EnvioService envioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @GetMapping("/logistica")
    public String showLogisticaPanel(Model model, Principal principal) {
        if (principal != null) {
            String email = principal.getName();
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
        model.addAttribute("enviosPendientes", envioService.ObtenerEnviosPendientes());

        return "panel-logistica";
    }

    @GetMapping("/envios")
    public String showEnvios() {
        return "gestion-envios";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }
}