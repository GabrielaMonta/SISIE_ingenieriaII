package sisie.capaDePresentacion;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuarioController {

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @GetMapping("/logistica")
    public String showLogisticaPanel() {
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
