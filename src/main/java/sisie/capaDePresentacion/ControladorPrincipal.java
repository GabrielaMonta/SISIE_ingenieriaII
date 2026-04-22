package sisie.capaDePresentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
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
        model.addAttribute("enviosPendientes", envioService.ObtenerEnviosPendientes());

        return "panel-logistica";
    }

    @GetMapping("/envios")
    public String showEnvios(Model model, Principal principal) {
       if (principal != null) {
        String email = principal.getName();
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            model.addAttribute("nombreUsuario", usuario.getNombre() + " " + usuario.getApellido());
        });
    }
        model.addAttribute("envios", envioService.obtenerEnviosNoPendientes());
        return "gestion-envios";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @PostMapping("/logistica/generar-test")
    public String generarTest(Principal principal) {
        // principal.getName() nos da el email del usuario que está logueado
        envioService.generarEnvioAleatorio(principal.getName());
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
}