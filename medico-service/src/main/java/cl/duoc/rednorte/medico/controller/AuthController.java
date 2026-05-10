package cl.duoc.rednorte.medico.controller;

import cl.duoc.rednorte.medico.dto.AuthResponse;
import cl.duoc.rednorte.medico.dto.CreateUsuarioRequest;
import cl.duoc.rednorte.medico.dto.LoginRequest;
import cl.duoc.rednorte.medico.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req.getEmail(), req.getPassword()));
    }

    @PostMapping("/crear-usuario")
    public ResponseEntity<AuthResponse> crearUsuario(@RequestBody CreateUsuarioRequest req) {
        return ResponseEntity.status(201).body(authService.crearUsuario(req));
    }
}
