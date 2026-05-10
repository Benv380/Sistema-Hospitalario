package cl.duoc.rednorte.paciente.controller;

import cl.duoc.rednorte.paciente.model.Paciente;
import cl.duoc.rednorte.paciente.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/pacientes")
@RequiredArgsConstructor
public class PacienteInternalController {

    private final PacienteRepository pacienteRepository;

    @GetMapping("/{usuarioId}")
    public ResponseEntity<Map<String, Object>> obtenerDatosPaciente(@PathVariable Long usuarioId) {
        Paciente p = pacienteRepository.findByUsuarioId(usuarioId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado para usuario: " + usuarioId));

        return ResponseEntity.ok(Map.of(
            "id", p.getUsuarioId(),
            "nombre", p.getNombre() != null ? p.getNombre() : "",
            "apellido", p.getApellido() != null ? p.getApellido() : "",
            "rut", p.getRut() != null ? p.getRut() : "",
            "email", p.getEmail() != null ? p.getEmail() : ""
        ));
    }
}
