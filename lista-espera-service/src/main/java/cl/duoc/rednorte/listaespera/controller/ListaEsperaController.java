package cl.duoc.rednorte.listaespera.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.validation.Valid;
import cl.duoc.rednorte.listaespera.dto.ListaEsperaDTO;
import cl.duoc.rednorte.listaespera.model.ListaEspera;
import cl.duoc.rednorte.listaespera.model.ListaEspera.EstadoSolicitud;
import cl.duoc.rednorte.listaespera.service.ListaEsperaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController                              // Devuelve JSON automáticamente
@RequestMapping("/api/v1/lista-espera")     // URL base de todos los endpoints
@RequiredArgsConstructor
public class ListaEsperaController {

    private final ListaEsperaService listaEsperaService;

    // ─────────────────────────────────────────────────
    //  GET /api/v1/lista-espera/pendientes
    //  Retorna todas las solicitudes PENDIENTES
    //  ordenadas por prioridad y fecha (usa Circuit Breaker)
    // ─────────────────────────────────────────────────
    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('FUNCIONARIO','MEDICO','ADMIN_HOSPITAL')")
    public ResponseEntity<List<ListaEspera>> obtenerPendientes() {
        return ResponseEntity.ok(listaEsperaService.obtenerPendientes());
    }

    // ─────────────────────────────────────────────────
    //  GET /api/v1/lista-espera/estado/{estado}
    //  Filtra por estado: PENDIENTE, ASIGNADO,
    //  CANCELADO o ATENDIDO
    //  Ejemplo: GET /api/v1/lista-espera/estado/ASIGNADO
    // ─────────────────────────────────────────────────
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<ListaEspera>> obtenerPorEstado(
            @PathVariable EstadoSolicitud estado) {
        return ResponseEntity.ok(listaEsperaService.obtenerPorEstado(estado));
    }

    // ─────────────────────────────────────────────────
    //  GET /api/v1/lista-espera/paciente/{pacienteId}
    //  Retorna todas las solicitudes de un paciente
    //  Ejemplo: GET /api/v1/lista-espera/paciente/3
    // ─────────────────────────────────────────────────
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<ListaEspera>> obtenerPorPaciente(
            @PathVariable Long pacienteId) {
        return ResponseEntity.ok(listaEsperaService.obtenerPorPaciente(pacienteId));
    }

    // ─────────────────────────────────────────────────
    //  GET /api/v1/lista-espera/{id}
    //  Retorna una solicitud específica por su ID
    // ─────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ListaEspera> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(listaEsperaService.obtenerPorId(id));
    }

    // ─────────────────────────────────────────────────
    //  POST /api/v1/lista-espera
    //  Registra una nueva solicitud en lista de espera
    //  Body JSON: { "paciente": {"id":1},
    //               "especialidad": "Cardiología",
    //               "hospital": "Hospital Norte",
    //               "prioridad": 1,
    //               "fechaSolicitud": "2025-03-29T10:00:00",
    //               "observaciones": "Paciente hipertenso" }
    // ─────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('PACIENTE','FUNCIONARIO')")
    public ResponseEntity<ListaEspera> registrar(@Valid @RequestBody ListaEsperaDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(listaEsperaService.registrar(dto));
    }

    // ─────────────────────────────────────────────────
    //  PUT /api/v1/lista-espera/{id}/cancelar
    //  Cancela una solicitud → cambia estado a CANCELADO
    //  Esto libera la hora médica para reasignación
    // ─────────────────────────────────────────────────
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<ListaEspera> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(listaEsperaService.cancelar(id));
    }

    // ─────────────────────────────────────────────────
    //  PUT /api/v1/lista-espera/{id}/asignar
    //  Asigna una hora médica a una solicitud pendiente
    //  Cambia estado PENDIENTE → ASIGNADO
    // ─────────────────────────────────────────────────
    @PutMapping("/{id}/asignar")
    @PreAuthorize("hasAnyRole('FUNCIONARIO','ADMIN_HOSPITAL')")
    public ResponseEntity<ListaEspera> asignar(@PathVariable Long id) {
        return ResponseEntity.ok(listaEsperaService.asignar(id));
    }

    // ─────────────────────────────────────────────────
    //  PUT /api/v1/lista-espera/{id}/atender
    //  Marca una solicitud como atendida
    //  Cambia estado ASIGNADO → ATENDIDO
    // ─────────────────────────────────────────────────
    @PutMapping("/{id}/atender")
    public ResponseEntity<ListaEspera> atender(@PathVariable Long id) {
        return ResponseEntity.ok(listaEsperaService.atender(id));
    }

    // ─────────────────────────────────────────────────
    //  DELETE /api/v1/lista-espera/{id}
    //  Elimina una solicitud de la base de datos
    //  (solo para administradores — en producción
    //  preferir el cambio de estado)
    // ─────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_HOSPITAL','ADMIN_SOFTWARE')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        listaEsperaService.eliminar(id);
        return ResponseEntity.noContent().build(); // HTTP 204
    }
}