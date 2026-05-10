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

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerDatosPaciente(@PathVariable Long id) {
        Paciente p = pacienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + id));

        return ResponseEntity.ok(Map.of(
            "id", p.getId(),
            "nombre", p.getNombre(),
            "apellido", p.getApellido(),
            "rut", p.getRut(),
            "email", p.getEmail() != null ? p.getEmail() : ""
        ));
    }
}
