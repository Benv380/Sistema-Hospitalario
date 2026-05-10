package cl.duoc.rednorte.medico.controller;

import cl.duoc.rednorte.medico.model.Medico;
import cl.duoc.rednorte.medico.repository.MedicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/medicos")
@RequiredArgsConstructor
public class MedicoInternalController {

    private final MedicoRepository medicoRepository;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerDatosMedico(@PathVariable Long id) {
        Medico m = medicoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Médico no encontrado: " + id));

        return ResponseEntity.ok(Map.of(
            "id", m.getId(),
            "nombre", m.getNombre(),
            "apellido", m.getApellido(),
            "especialidad", m.getEspecialidad(),
            "email", m.getEmail() != null ? m.getEmail() : ""
        ));
    }
}
