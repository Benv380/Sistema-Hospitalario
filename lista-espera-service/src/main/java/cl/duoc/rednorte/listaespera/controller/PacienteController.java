package cl.duoc.rednorte.listaespera.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import cl.duoc.rednorte.listaespera.dto.PacienteDTO;
import cl.duoc.rednorte.listaespera.model.Paciente;
import cl.duoc.rednorte.listaespera.service.PacienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController                        // Controlador que devuelve JSON automáticamente
@RequestMapping("/api/v1/pacientes")  // URL base de este controlador
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping  
    @PreAuthorize("hasAnyRole('FUNCIONARIO','MEDICO','ADMIN_HOSPITAL')")                     // GET  /api/v1/pacientes
    public ResponseEntity<List<Paciente>> listarTodos() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    @GetMapping("/{id}")              // GET  /api/v1/pacientes/5
    @PreAuthorize("hasAnyRole('FUNCIONARIO','MEDICO','ADMIN_HOSPITAL','PACIENTE')")
    public ResponseEntity<Paciente> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.obtenerPorId(id));
    }

    @PostMapping       
    @PreAuthorize("hasAnyRole('ADMIN_HOSPITAL','ADMIN_SOFTWARE')")               // POST /api/v1/pacientes
    public ResponseEntity<Paciente> crear(@Valid @RequestBody PacienteDTO dto) {
        // @Valid activa las validaciones del DTO
        // @RequestBody convierte el JSON entrante en objeto Java
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.crear(dto));
    }

    @PutMapping("/{id}")             // PUT  /api/v1/pacientes/5
    @PreAuthorize("hasAnyRole('PACIENTE','ADMIN_HOSPITAL')")
    public ResponseEntity<Paciente> actualizar(
            @PathVariable Long id, @Valid @RequestBody PacienteDTO dto) {
        return ResponseEntity.ok(pacienteService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")          // DELETE /api/v1/pacientes/5
    @PreAuthorize("hasRole('ADMIN_HOSPITAL')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pacienteService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}