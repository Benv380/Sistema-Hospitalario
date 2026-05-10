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
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        Medico m = medicoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Médico no encontrado: " + id));
        return ResponseEntity.ok(toMap(m));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Map<String, Object>> obtenerPorUsuarioId(@PathVariable Long usuarioId) {
        Medico m = medicoRepository.findByUsuarioId(usuarioId)
            .orElseThrow(() -> new RuntimeException("Médico no encontrado para usuario: " + usuarioId));
        return ResponseEntity.ok(toMap(m));
    }

    private Map<String, Object> toMap(Medico m) {
        return Map.of(
            "id",          m.getId(),
            "usuarioId",   m.getUsuarioId() != null ? m.getUsuarioId() : 0L,
            "nombre",      m.getNombre() != null ? m.getNombre() : "",
            "apellido",    m.getApellido() != null ? m.getApellido() : "",
            "especialidad",m.getEspecialidad() != null ? m.getEspecialidad() : "",
            "email",       m.getEmail() != null ? m.getEmail() : ""
        );
    }
}
