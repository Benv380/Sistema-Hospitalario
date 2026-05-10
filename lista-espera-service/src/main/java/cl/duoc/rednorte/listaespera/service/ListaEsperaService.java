package cl.duoc.rednorte.listaespera.service;

import cl.duoc.rednorte.listaespera.dto.ListaEsperaDTO;
import cl.duoc.rednorte.listaespera.model.ListaEspera;
import cl.duoc.rednorte.listaespera.model.ListaEspera.EstadoSolicitud;
import cl.duoc.rednorte.listaespera.repository.ListaEsperaRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ListaEsperaService {

    private final ListaEsperaRepository repo;

    @CircuitBreaker(name = "listaEsperaService", fallbackMethod = "fallbackPendientes")
    public List<ListaEspera> obtenerPendientes() {
        log.info("Consultando solicitudes pendientes...");
        return repo.findPendientesOrdenados();
    }

    public List<ListaEspera> fallbackPendientes(Exception ex) {
        log.warn("[Circuit Breaker ABIERTO] Error: {}", ex.getMessage());
        return Collections.emptyList();
    }

    public ListaEspera obtenerPorId(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con id: " + id));
    }

    public List<ListaEspera> obtenerPorEstado(EstadoSolicitud estado) {
        log.info("Buscando solicitudes con estado: {}", estado);
        return repo.findByEstado(estado);
    }

    public List<ListaEspera> obtenerPorPaciente(Long pacienteId) {
        log.info("Buscando solicitudes del paciente ID: {}", pacienteId);
        return repo.findByPacienteId(pacienteId);
    }

    public ListaEspera registrar(ListaEsperaDTO dto) {
        ListaEspera solicitud = ListaEspera.builder()
            .pacienteId(dto.getPacienteId())
            .especialidad(dto.getEspecialidad())
            .hospital(dto.getHospital())
            .prioridad(dto.getPrioridad())
            .estado(EstadoSolicitud.PENDIENTE)
            .fechaSolicitud(LocalDateTime.now())
            .observaciones(dto.getObservaciones())
            .build();

        ListaEspera guardada = repo.save(solicitud);
        log.info("Nueva solicitud registrada ID: {} para paciente ID: {}", guardada.getId(), dto.getPacienteId());
        return guardada;
    }

    public ListaEspera asignar(Long id) {
        ListaEspera s = obtenerPorId(id);
        if (s.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException(
                "Solo se pueden asignar solicitudes PENDIENTES. Estado actual: " + s.getEstado());
        }
        s.setEstado(EstadoSolicitud.ASIGNADO);
        s.setFechaAtencion(LocalDateTime.now());
        log.info("Solicitud {} asignada", id);
        return repo.save(s);
    }

    public ListaEspera atender(Long id) {
        ListaEspera s = obtenerPorId(id);
        if (s.getEstado() != EstadoSolicitud.ASIGNADO) {
            throw new RuntimeException(
                "Solo se pueden atender solicitudes ASIGNADAS. Estado actual: " + s.getEstado());
        }
        s.setEstado(EstadoSolicitud.ATENDIDO);
        log.info("Solicitud {} marcada como ATENDIDA", id);
        return repo.save(s);
    }

    public ListaEspera cancelar(Long id) {
        ListaEspera s = obtenerPorId(id);
        if (s.getEstado() == EstadoSolicitud.ATENDIDO) {
            throw new RuntimeException("No se puede cancelar una solicitud ya ATENDIDA");
        }
        s.setEstado(EstadoSolicitud.CANCELADO);
        log.info("Solicitud {} cancelada", id);
        return repo.save(s);
    }

    public void eliminar(Long id) {
        obtenerPorId(id);
        repo.deleteById(id);
        log.info("Solicitud {} eliminada", id);
    }
}