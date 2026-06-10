package sisie.capaDeLogica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import sisie.capaDeDatos.UsuarioRepository;
import sisie.capaDeDominio.*;

@Component
public class RegistradorHistorial implements ObservadorEnvio {

    @Autowired
    private HistorialEnvioService historialEnvioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void actualizar(Envio envio) {
        registrarCambioEstado(envio);
    }

    private void registrarCambioEstado(Envio envio) {
        // Determinar el motivo según el estado actual o el motivo personalizado
        String motivo = envio.getMotivoTransicion();
        if (motivo == null || motivo.trim().isEmpty()) {
            motivo = "Cambio de estado";
            EstadoEnvio estado = envio.getEstadoActual();
            if (estado instanceof EstadoPendiente) {
                motivo = "Generación de envío";
            } else if (estado instanceof EstadoEnProceso) {
                motivo = "Inicio de gestión";
            } else if (estado instanceof EstadoEnTransito) {
                motivo = "Envío en tránsito";
            } else if (estado instanceof EstadoEntregado) {
                motivo = "Envío entregado";
            } else if (estado instanceof EstadoNoEntregado) {
                motivo = "Envío no entregado";
            }
        }

        // Obtener usuario autenticado en Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth != null && auth.isAuthenticated()) ? auth.getName() : null;
        Usuario usuario = null;
        if (email != null && !email.equals("anonymousUser")) {
            usuario = usuarioRepository.findByEmail(email).orElse(null);
        }
        if (usuario == null) {
            // Si no hay usuario autenticado (ej. generación automática), se usa el primer usuario en la BD
            usuario = usuarioRepository.findAll().stream().findFirst().orElse(null);
        }

        // Delegar la persistencia al servicio de historial
        historialEnvioService.registrarCambioEstado(envio, motivo, usuario);
    }
}
