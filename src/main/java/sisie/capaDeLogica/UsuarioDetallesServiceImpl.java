package sisie.capaDeLogica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sisie.capaDeDatos.UsuarioRepository;
import sisie.capaDeDominio.Usuario;



@Service
public class UsuarioDetallesServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Por ahora, como solo hay empleados de logística, les asignamos ese rol
        return User.withUsername(usuario.getEmail())
                .password(usuario.getContraseña()) // La contraseña debe estar cifrada en la DB
                .roles("EMPLEADO_LOGISTICA")
                .build();
    }
}