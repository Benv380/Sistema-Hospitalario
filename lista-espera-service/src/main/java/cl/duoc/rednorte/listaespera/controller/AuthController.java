package cl.duoc.rednorte.listaespera.controller;

import cl.duoc.rednorte.listaespera.dto.AuthResponse;
import cl.duoc.rednorte.listaespera.dto.LoginRequest;
import cl.duoc.rednorte.listaespera.dto.RegisterRequest;
import cl.duoc.rednorte.listaespera.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // Base: /api/auth
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    // DEBE SER POST
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req.getEmail(), req.getPassword()));
    }

    // DEBE SER POST
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
        // Retornamos 201 Created
        return ResponseEntity.status(201).body(authService.registrar(req));
    }
}