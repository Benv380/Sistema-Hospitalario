package cl.duoc.rednorte.paciente.controller;

import cl.duoc.rednorte.paciente.dto.PacienteDTO;
import cl.duoc.rednorte.paciente.model.Paciente;
import cl.duoc.rednorte.paciente.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping
    @PreAuthorize("hasAnyRole('FUNCIONARIO','MEDICO','ADMIN_HOSPITAL')")
    public ResponseEntity<List<Paciente>> listarTodos() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FUNCIONARIO','MEDICO','ADMIN_HOSPITAL','PACIENTE')")
    public ResponseEntity<Paciente> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_HOSPITAL','ADMIN_SOFTWARE','FUNCIONARIO')")
    public ResponseEntity<Paciente> crear(@Valid @RequestBody PacienteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.crear(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PACIENTE','ADMIN_HOSPITAL','FUNCIONARIO')")
    public ResponseEntity<Paciente> actualizar(@PathVariable Long id, @Valid @RequestBody PacienteDTO dto) {
        return ResponseEntity.ok(pacienteService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_HOSPITAL')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pacienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
