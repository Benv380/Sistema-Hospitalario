package cl.duoc.rednorte.listaespera.service;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import cl.duoc.rednorte.listaespera.model.Usuario;
import cl.duoc.rednorte.listaespera.repository.UsuarioRepository;
import cl.duoc.rednorte.listaespera.model.RolUsuario;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario crearUsuario(String email, String passwordPlano, String nombreCompleto) {
        RolUsuario rol = inferirRolPorEmail(email);

        Usuario usuario = new Usuario();
        usuario.setEmail(email.toLowerCase());
        usuario.setPasswordHash(passwordEncoder.encode(passwordPlano));
        usuario.setRol(rol);
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    private RolUsuario inferirRolPorEmail(String email) {
        String dominio = email.substring(email.indexOf("@") + 1).toLowerCase();
        return switch (dominio) {
            case "funchospital.cl" -> RolUsuario.FUNCIONARIO;
            case "medhospital.cl" -> RolUsuario.MEDICO;
            case "admhospital.cl" -> RolUsuario.ADMIN_HOSPITAL;
            case "admsoft.cl" -> RolUsuario.ADMIN_SOFTWARE;
            default -> RolUsuario.PACIENTE;
        };
    }
}