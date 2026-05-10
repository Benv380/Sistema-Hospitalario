package cl.duoc.rednorte.medico.service;

import cl.duoc.rednorte.medico.config.JwtService;
import cl.duoc.rednorte.medico.dto.AuthResponse;
import cl.duoc.rednorte.medico.dto.CreateUsuarioRequest;
import cl.duoc.rednorte.medico.model.RolUsuario;
import cl.duoc.rednorte.medico.model.Usuario;
import cl.duoc.rednorte.medico.repository.UsuarioRepository;
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

    public AuthResponse crearUsuario(CreateUsuarioRequest req) {
        if (usuarioRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        RolUsuario rol = req.getRol() != null ? RolUsuario.valueOf(req.getRol()) : RolUsuario.MEDICO;
        Usuario nuevo = Usuario.builder()
            .email(req.getEmail().toLowerCase())
            .passwordHash(passwordEncoder.encode(req.getPassword()))
            .rol(rol)
            .nombreCompleto(req.getNombreCompleto())
            .activo(true)
            .build();

        Usuario guardado = usuarioRepo.save(nuevo);
        String token = jwtService.generateToken(guardado, rol.name());
        log.info("Usuario médico creado: {}", guardado.getEmail());
        return new AuthResponse(guardado.getId(), token, rol.name(), guardado.getNombreCompleto(), resolverRedirect(rol.name()));
    }

    private String resolverRedirect(String rol) {
        return switch (rol) {
            case "MEDICO" -> "/medico/dashboard";
            case "ADMIN_HOSPITAL" -> "/admin/hospital/dashboard";
            case "ADMIN_SOFTWARE" -> "/admin/software/dashboard";
            default -> "/login";
        };
    }
}
