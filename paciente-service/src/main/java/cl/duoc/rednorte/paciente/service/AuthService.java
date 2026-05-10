package cl.duoc.rednorte.paciente.service;

import cl.duoc.rednorte.paciente.config.JwtService;
import cl.duoc.rednorte.paciente.dto.AuthResponse;
import cl.duoc.rednorte.paciente.dto.RegisterRequest;
import cl.duoc.rednorte.paciente.model.Paciente;
import cl.duoc.rednorte.paciente.model.RolUsuario;
import cl.duoc.rednorte.paciente.model.Usuario;
import cl.duoc.rednorte.paciente.repository.PacienteRepository;
import cl.duoc.rednorte.paciente.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final PacienteRepository pacienteRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(String email, String password) {
        Usuario usuario = usuarioRepo.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!usuario.getActivo() || !passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String rol = usuario.getRol().name();
        String token = jwtService.generateToken(usuario, rol);
        return new AuthResponse(usuario.getId(), token, rol, usuario.getNombreCompleto(), resolverRedirect(rol));
    }

    public AuthResponse registrar(RegisterRequest req) {
        if (usuarioRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        RolUsuario rol = inferirRol(req.getEmail());
        Usuario nuevo = Usuario.builder()
            .email(req.getEmail().toLowerCase())
            .passwordHash(passwordEncoder.encode(req.getPassword()))
            .rol(rol)
            .nombreCompleto(req.getNombreCompleto())
            .activo(true)
            .build();

        Usuario guardado = usuarioRepo.save(nuevo);

        if (rol == RolUsuario.PACIENTE) {
            String[] partes = req.getNombreCompleto().trim().split("\\s+", 2);
            String nombre   = partes[0];
            String apellido = partes.length > 1 ? partes[1] : "—";
            pacienteRepo.save(Paciente.builder()
                .usuarioId(guardado.getId())
                .nombre(nombre)
                .apellido(apellido)
                .email(guardado.getEmail())
                .build());
        }

        String token = jwtService.generateToken(guardado, rol.name());
        log.info("Usuario registrado: {}", guardado.getEmail());
        return new AuthResponse(guardado.getId(), token, rol.name(), guardado.getNombreCompleto(), resolverRedirect(rol.name()));
    }

    private String resolverRedirect(String rol) {
        return switch (rol) {
            case "PACIENTE" -> "/paciente/dashboard";
            case "FUNCIONARIO" -> "/funcionario/dashboard";
            case "MEDICO" -> "/medico/dashboard";
            case "ADMIN_HOSPITAL" -> "/admin/hospital/dashboard";
            case "ADMIN_SOFTWARE" -> "/admin/software/dashboard";
            default -> "/login";
        };
    }

    private RolUsuario inferirRol(String email) {
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
