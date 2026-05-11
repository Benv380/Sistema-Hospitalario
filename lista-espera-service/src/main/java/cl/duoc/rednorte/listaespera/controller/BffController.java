package cl.duoc.rednorte.listaespera.controller;

import cl.duoc.rednorte.listaespera.model.CitaMedica;
import cl.duoc.rednorte.listaespera.model.ListaEspera;
import cl.duoc.rednorte.listaespera.model.ListaEspera.EstadoSolicitud;
import cl.duoc.rednorte.listaespera.repository.CitaMedicaRepository;
import cl.duoc.rednorte.listaespera.repository.ListaEsperaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bff")
@RequiredArgsConstructor
@Slf4j
public class BffController {

    private final ListaEsperaRepository listaEsperaRepo;
    private final CitaMedicaRepository  citaRepo;
    private final RestTemplate          restTemplate;

    // ── Dashboard Paciente ────────────────────────────────────────────
    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<Map<String, Object>> dashboardPaciente(@PathVariable Long pacienteId) {

        List<ListaEspera> solicitudes = listaEsperaRepo.findByPacienteId(pacienteId);
        List<CitaMedica>  citas       = citaRepo.findByPacienteId(pacienteId);

        long solicitudesActivas = solicitudes.stream()
            .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE
                      || s.getEstado() == EstadoSolicitud.ASIGNADO)
            .count();

        Optional<CitaMedica> proxima = citas.stream()
            .filter(c -> c.getEstado() == CitaMedica.EstadoCita.PROGRAMADA
                      || c.getEstado() == CitaMedica.EstadoCita.CONFIRMADA)
            .filter(c -> c.getFechaHoraCita().isAfter(LocalDateTime.now()))
            .min(Comparator.comparing(CitaMedica::getFechaHoraCita));

        List<ListaEspera> pendientesOrdenados = listaEsperaRepo.findPendientesOrdenados();

        List<Map<String, Object>> solicitudesDTO = solicitudes.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id",           s.getId());
            m.put("especialidad", s.getEspecialidad());
            m.put("hospital",     s.getHospital());
            m.put("estado",       s.getEstado().name());
            m.put("fechaSolicitud", s.getFechaSolicitud());
            m.put("observaciones",  s.getObservaciones());
            if (s.getEstado() == EstadoSolicitud.PENDIENTE) {
                int pos = pendientesOrdenados.indexOf(s) + 1;
                m.put("posicion",              pos > 0 ? pos : null);
                m.put("tiempoEstimadoMinutos", pos > 0 ? pos * 20 : null);
            } else {
                m.put("posicion",              null);
                m.put("tiempoEstimadoMinutos", null);
            }
            return m;
        }).collect(Collectors.toList());

        List<Map<String, Object>> citasDTO = citas.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id",          c.getId());
            m.put("estado",      c.getEstado().name());
            m.put("especialidad",c.getEspecialidad());
            m.put("nombreMedico",c.getNombreMedico());
            m.put("fechaHoraCita", c.getFechaHoraCita());
            m.put("hospital",    c.getHospital());
            m.put("boxNumero",   c.getBoxNumero());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> proximaCitaDTO = proxima.map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id",           c.getId());
            m.put("especialidad", c.getEspecialidad());
            m.put("nombreMedico", c.getNombreMedico());
            m.put("fechaHoraCita",c.getFechaHoraCita());
            m.put("hospital",     c.getHospital());
            return m;
        }).orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("solicitudes",       solicitudesDTO);
        response.put("citas",             citasDTO);
        response.put("solicitudesActivas",solicitudesActivas);
        response.put("proximaCita",       proximaCitaDTO);

        return ResponseEntity.ok(response);
    }

    // ── Dashboard Médico ──────────────────────────────────────────────
    @GetMapping("/medico/{medicoUsuarioId}")
    public ResponseEntity<Map<String, Object>> dashboardMedico(@PathVariable Long medicoUsuarioId) {

        // Citas de hoy (todo el día)
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia    = inicioDia.plusDays(1).minusNanos(1);

        List<CitaMedica> citasHoy;

        // Intentar filtrar por nombre del médico; si falla mostrar todas las citas de hoy
        String nombreMedico = fetchNombreMedico(medicoUsuarioId);
        if (nombreMedico != null) {
            citasHoy = citaRepo.findByNombreMedicoAndFechaHoraCitaBetween(nombreMedico, inicioDia, finDia);
        } else {
            citasHoy = citaRepo.findByFechaHoraCitaBetweenOrderByFechaHoraCitaAsc(inicioDia, finDia);
        }

        // Pacientes en espera (total pendientes)
        long totalPendientes = listaEsperaRepo.findByEstado(EstadoSolicitud.PENDIENTE).size();

        // KPIs del día
        long atendidosHoy = citasHoy.stream()
            .filter(c -> c.getEstado() == CitaMedica.EstadoCita.REALIZADA).count();
        long citasProgramadasHoy = citasHoy.stream()
            .filter(c -> c.getEstado() == CitaMedica.EstadoCita.PROGRAMADA
                      || c.getEstado() == CitaMedica.EstadoCita.CONFIRMADA).count();

        // Colectar pacienteIds únicos para enriquecer con nombre
        Set<Long> pacienteIds = citasHoy.stream()
            .map(c -> c.getListaEspera().getPacienteId())
            .collect(Collectors.toSet());

        Map<Long, String> nombresPacientes = fetchNombresPacientes(pacienteIds);

        List<Map<String, Object>> citasHoyDTO = citasHoy.stream().map(c -> {
            Long pacienteId    = c.getListaEspera().getPacienteId();
            String pacNombre   = nombresPacientes.getOrDefault(pacienteId, "Paciente #" + pacienteId);

            Map<String, Object> listaEsperaMap = new HashMap<>();
            listaEsperaMap.put("id",             c.getListaEspera().getId());
            listaEsperaMap.put("pacienteId",     pacienteId);
            listaEsperaMap.put("pacienteNombre", pacNombre);
            listaEsperaMap.put("especialidad",   c.getListaEspera().getEspecialidad());

            Map<String, Object> m = new HashMap<>();
            m.put("id",           c.getId());
            m.put("estado",       c.getEstado().name());
            m.put("especialidad", c.getEspecialidad());
            m.put("nombreMedico", c.getNombreMedico());
            m.put("fechaHoraCita",c.getFechaHoraCita());
            m.put("boxNumero",    c.getBoxNumero());
            m.put("hospital",     c.getHospital());
            m.put("listaEspera",  listaEsperaMap);
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("citasHoy",            citasHoyDTO);
        response.put("totalPendientes",      totalPendientes);
        response.put("atendidosHoy",         atendidosHoy);
        response.put("citasProgramadasHoy",  citasProgramadasHoy);

        return ResponseEntity.ok(response);
    }

    // ── Helpers internos ──────────────────────────────────────────────

    private String fetchNombreMedico(Long usuarioId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = restTemplate.getForObject(
                "http://localhost:8082/api/internal/medicos/usuario/" + usuarioId, Map.class);
            if (data == null) return null;
            String nombre   = (String) data.get("nombre");
            String apellido = (String) data.get("apellido");
            return (nombre + " " + apellido).trim();
        } catch (Exception e) {
            log.warn("No se pudo obtener nombre del médico {}: {}", usuarioId, e.getMessage());
            return null;
        }
    }

    private Map<Long, String> fetchNombresPacientes(Set<Long> ids) {
        Map<Long, String> result = new HashMap<>();
        for (Long id : ids) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = restTemplate.getForObject(
                    "http://localhost:8081/api/internal/pacientes/" + id, Map.class);
                if (data != null) {
                    String nombre   = (String) data.get("nombre");
                    String apellido = (String) data.get("apellido");
                    result.put(id, (nombre + " " + apellido).trim());
                }
            } catch (Exception e) {
                log.warn("No se pudo obtener nombre del paciente {}: {}", id, e.getMessage());
            }
        }
        return result;
    }
}
