package cl.duoc.rednorte.listaespera.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import cl.duoc.rednorte.listaespera.dto.CitaMedicaDTO;
import cl.duoc.rednorte.listaespera.model.CitaMedica;
import cl.duoc.rednorte.listaespera.model.CitaMedica.EstadoCita;
import cl.duoc.rednorte.listaespera.service.CitaMedicaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/citas")
@RequiredArgsConstructor
public class CitaMedicaController {

    private final CitaMedicaService citaMedicaService;

    // POST /api/v1/citas → agendar nueva cita
    @PostMapping
    @PreAuthorize("hasAnyRole('PACIENTE','FUNCIONARIO')")
    public ResponseEntity<CitaMedica> agendar(@Valid @RequestBody CitaMedicaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(citaMedicaService.agendar(dto));
    }

    // GET /api/v1/citas/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CitaMedica> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(citaMedicaService.obtenerPorId(id));
    }

    // GET /api/v1/citas/solicitud/{listaEsperaId}
    // Busca la cita vinculada a una solicitud de lista de espera
    @GetMapping("/solicitud/{listaEsperaId}")
    public ResponseEntity<CitaMedica> obtenerPorSolicitud(
            @PathVariable Long listaEsperaId) {
        return ResponseEntity.ok(citaMedicaService.obtenerPorSolicitud(listaEsperaId));
    }

    // GET /api/v1/citas/estado/{estado}
    // Ej: GET /api/v1/citas/estado/CANCELADA
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<CitaMedica>> obtenerPorEstado(
            @PathVariable EstadoCita estado) {
        return ResponseEntity.ok(citaMedicaService.obtenerPorEstado(estado));
    }

    // GET /api/v1/citas/paciente/{pacienteId}
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<CitaMedica>> obtenerPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(citaMedicaService.obtenerPorPaciente(pacienteId));
    }

    // GET /api/v1/citas/reasignacion
    // Citas canceladas disponibles para reasignar a pacientes en espera
    @GetMapping("/reasignacion")
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN_HOSPITAL')")
    public ResponseEntity<List<CitaMedica>> obtenerDisponiblesReasignacion() {
        return ResponseEntity.ok(citaMedicaService.obtenerDisponiblesParaReasignacion());
    }

    // PUT /api/v1/citas/{id}/confirmar
    @PutMapping("/{id}/confirmar")
    @PreAuthorize("hasAnyRole('FUNCIONARIO','MEDICO')")
    public ResponseEntity<CitaMedica> confirmar(@PathVariable Long id) {
        return ResponseEntity.ok(citaMedicaService.confirmar(id));
    }

    // PUT /api/v1/citas/{id}/realizar
    @PutMapping("/{id}/realizar")
    @PreAuthorize("hasRole('MEDICO')")
    public ResponseEntity<CitaMedica> marcarRealizada(@PathVariable Long id) {
        return ResponseEntity.ok(citaMedicaService.marcarRealizada(id));
    }

    // PUT /api/v1/citas/{id}/cancelar?motivo=...
    @PutMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN_HOSPITAL','PACIENTE')")
    public ResponseEntity<CitaMedica> cancelar(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Sin motivo especificado") String motivo) {
        return ResponseEntity.ok(citaMedicaService.cancelar(id, motivo));
    }

    // PUT /api/v1/citas/{id}/no-asistio
    @PutMapping("/{id}/no-asistio")
    public ResponseEntity<CitaMedica> marcarNoAsistio(@PathVariable Long id) {
        return ResponseEntity.ok(citaMedicaService.marcarNoAsistio(id));
    }
}