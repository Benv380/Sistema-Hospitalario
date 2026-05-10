package cl.duoc.rednorte.paciente.controller;

import cl.duoc.rednorte.paciente.dto.AuthResponse;
import cl.duoc.rednorte.paciente.dto.LoginRequest;
import cl.duoc.rednorte.paciente.dto.RegisterRequest;
import cl.duoc.rednorte.paciente.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req.getEmail(), req.getPassword()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.status(201).body(authService.registrar(req));
    }
}
