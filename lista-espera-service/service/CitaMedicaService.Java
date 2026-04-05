package cl.duoc.rednorte.listaespera.service;

import cl.duoc.rednorte.listaespera.dto.CitaMedicaDTO;
import cl.duoc.rednorte.listaespera.model.CitaMedica;
import cl.duoc.rednorte.listaespera.model.CitaMedica.EstadoCita;
import cl.duoc.rednorte.listaespera.model.ListaEspera;
import cl.duoc.rednorte.listaespera.model.ListaEspera.EstadoSolicitud;
import cl.duoc.rednorte.listaespera.repository.CitaMedicaRepository;
import cl.duoc.rednorte.listaespera.repository.ListaEsperaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CitaMedicaService {

    private final CitaMedicaRepository citaRepo;
    private final ListaEsperaRepository listaEsperaRepo;
    // Inyectamos NotificacionFactory para avisar al paciente
    private final NotificacionFactory notificacionFactory;

    // ── Agendar nueva cita ───────────────────────────────────────────
    // Vincula una CitaMedica a una solicitud de ListaEspera existente
    // y actualiza el estado de la solicitud a ASIGNADO
    public CitaMedica agendar(CitaMedicaDTO dto) {
        // 1. Verificar que la solicitud de lista de espera existe
        ListaEspera solicitud = listaEsperaRepo.findById(dto.getListaEsperaId())
            .orElseThrow(() -> new RuntimeException(
                "Solicitud de lista de espera no encontrada: " + dto.getListaEsperaId()));

        // 2. Verificar que la solicitud esté PENDIENTE
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new RuntimeException(
                "Solo se pueden agendar citas para solicitudes PENDIENTES. Estado: "
                + solicitud.getEstado());
        }

        // 3. Verificar que no tenga ya una cita agendada
        if (citaRepo.existsByListaEsperaId(dto.getListaEsperaId())) {
            throw new RuntimeException(
                "La solicitud ya tiene una cita agendada");
        }

        // 4. Crear la cita
        CitaMedica cita = CitaMedica.builder()
            .listaEspera(solicitud)
            .nombreMedico(dto.getNombreMedico())
            .especialidad(dto.getEspecialidad())
            .hospital(dto.getHospital())
            .boxNumero(dto.getBoxNumero())
            .fechaHoraCita(dto.getFechaHoraCita())
            .estado(EstadoCita.PROGRAMADA)
            .observaciones(dto.getObservaciones())
            .build();

        CitaMedica guardada = citaRepo.save(cita);

        // 5. Actualizar estado de la solicitud → ASIGNADO
        solicitud.setEstado(EstadoSolicitud.ASIGNADO);
        solicitud.setFechaAtencion(dto.getFechaHoraCita());
        listaEsperaRepo.save(solicitud);

        // 6. Notificar al paciente por email
        String emailPaciente = solicitud.getPaciente().getEmail();
        if (emailPaciente != null) {
            notificacionFactory.crear("EMAIL").enviar(
                emailPaciente,
                "Su cita médica fue agendada para: " + dto.getFechaHoraCita()
                + " con Dr/a. " + dto.getNombreMedico()
                + " en " + dto.getHospital()
            );
        }

        log.info("Cita agendada ID: {} para solicitud: {}", guardada.getId(), solicitud.getId());
        return guardada;
    }

    // ── Confirmar asistencia ─────────────────────────────────────────
    // PROGRAMADA → CONFIRMADA
    public CitaMedica confirmar(Long id) {
        CitaMedica cita = obtenerPorId(id);
        if (cita.getEstado() != EstadoCita.PROGRAMADA) {
            throw new RuntimeException(
                "Solo se pueden confirmar citas PROGRAMADAS. Estado actual: " + cita.getEstado());
        }
        cita.setEstado(EstadoCita.CONFIRMADA);
        log.info("Cita {} confirmada", id);
        return citaRepo.save(cita);
    }

    // ── Marcar como realizada ────────────────────────────────────────
    // CONFIRMADA o PROGRAMADA → REALIZADA + solicitud → ATENDIDO
    public CitaMedica marcarRealizada(Long id) {
        CitaMedica cita = obtenerPorId(id);
        if (cita.getEstado() == EstadoCita.CANCELADA
                || cita.getEstado() == EstadoCita.REALIZADA) {
            throw new RuntimeException(
                "No se puede marcar como realizada. Estado actual: " + cita.getEstado());
        }
        cita.setEstado(EstadoCita.REALIZADA);

        // Actualizar la solicitud → ATENDIDO
        ListaEspera solicitud = cita.getListaEspera();
        solicitud.setEstado(EstadoSolicitud.ATENDIDO);
        listaEsperaRepo.save(solicitud);

        log.info("Cita {} marcada como REALIZADA", id);
        return citaRepo.save(cita);
    }

    // ── Cancelar y disparar reasignación ─────────────────────────────
    // PROGRAMADA o CONFIRMADA → CANCELADA
    // La solicitud vuelve a PENDIENTE para ser reasignada
    public CitaMedica cancelar(Long id, String motivo) {
        CitaMedica cita = obtenerPorId(id);
        if (cita.getEstado() == EstadoCita.REALIZADA
                || cita.getEstado() == EstadoCita.CANCELADA) {
            throw new RuntimeException(
                "No se puede cancelar. Estado actual: " + cita.getEstado());
        }

        cita.setEstado(EstadoCita.CANCELADA);
        cita.setObservaciones("CANCELADA: " + motivo);

        // La solicitud vuelve a PENDIENTE → queda disponible para reasignación
        ListaEspera solicitud = cita.getListaEspera();
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        solicitud.setFechaAtencion(null);
        listaEsperaRepo.save(solicitud);

        // Notificar al paciente
        String email = solicitud.getPaciente().getEmail();
        if (email != null) {
            notificacionFactory.crear("EMAIL").enviar(
                email,
                "Su cita médica fue cancelada. Motivo: " + motivo
                + ". Será contactado para reagendar."
            );
        }

        log.info("Cita {} cancelada. Motivo: {}", id, motivo);
        return citaRepo.save(cita);
    }

    // ── Marcar no asistió ────────────────────────────────────────────
    public CitaMedica marcarNoAsistio(Long id) {
        CitaMedica cita = obtenerPorId(id);
        if (cita.getEstado() != EstadoCita.PROGRAMADA
                && cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new RuntimeException(
                "Estado inválido para marcar inasistencia: " + cita.getEstado());
        }
        cita.setEstado(EstadoCita.NO_ASISTIO);
        log.info("Cita {} marcada como NO_ASISTIO", id);
        return citaRepo.save(cita);
    }

    // ── Consultas ────────────────────────────────────────────────────
    public CitaMedica obtenerPorId(Long id) {
        return citaRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Cita médica no encontrada: " + id));
    }

    public CitaMedica obtenerPorSolicitud(Long listaEsperaId) {
        return citaRepo.findByListaEsperaId(listaEsperaId)
            .orElseThrow(() -> new RuntimeException(
                "No hay cita para la solicitud: " + listaEsperaId));
    }

    public List<CitaMedica> obtenerPorEstado(EstadoCita estado) {
        return citaRepo.findByEstado(estado);
    }

    public List<CitaMedica> obtenerDisponiblesParaReasignacion() {
        return citaRepo.findCitasDisponiblesParaReasignacion(LocalDateTime.now());
    }
}