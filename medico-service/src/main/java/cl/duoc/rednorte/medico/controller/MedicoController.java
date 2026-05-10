package cl.duoc.rednorte.medico.controller;

import cl.duoc.rednorte.medico.dto.MedicoDTO;
import cl.duoc.rednorte.medico.model.Medico;
import cl.duoc.rednorte.medico.service.MedicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final MedicoService medicoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN_HOSPITAL','PACIENTE')")
    public ResponseEntity<List<Medico>> listarTodos() {
        return ResponseEntity.ok(medicoService.listarActivos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('FUNCIONARIO','MEDICO','ADMIN_HOSPITAL','PACIENTE')")
    public ResponseEntity<Medico> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(medicoService.obtenerPorId(id));
    }

    @GetMapping("/especialidad/{especialidad}")
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN_HOSPITAL','PACIENTE')")
    public ResponseEntity<List<Medico>> listarPorEspecialidad(@PathVariable String especialidad) {
        return ResponseEntity.ok(medicoService.listarPorEspecialidad(especialidad));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN_HOSPITAL','ADMIN_SOFTWARE')")
    public ResponseEntity<Medico> crear(@Valid @RequestBody MedicoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicoService.crear(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_HOSPITAL','MEDICO')")
    public ResponseEntity<Medico> actualizar(@PathVariable Long id, @Valid @RequestBody MedicoDTO dto) {
        return ResponseEntity.ok(medicoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN_HOSPITAL')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        medicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
