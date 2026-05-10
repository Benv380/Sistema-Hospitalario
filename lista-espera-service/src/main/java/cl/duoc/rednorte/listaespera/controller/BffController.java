package cl.duoc.rednorte.listaespera.controller;

import cl.duoc.rednorte.listaespera.model.CitaMedica;
import cl.duoc.rednorte.listaespera.model.ListaEspera;
import cl.duoc.rednorte.listaespera.model.ListaEspera.EstadoSolicitud;
import cl.duoc.rednorte.listaespera.repository.CitaMedicaRepository;
import cl.duoc.rednorte.listaespera.repository.ListaEsperaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/bff")
@RequiredArgsConstructor
public class BffController {

    private final ListaEsperaRepository listaEsperaRepo;
    private final CitaMedicaRepository citaRepo;

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<Map<String, Object>> dashboardPaciente(@PathVariable Long pacienteId) {

        List<ListaEspera> solicitudes = listaEsperaRepo.findByPacienteId(pacienteId);
        List<CitaMedica>  citas       = citaRepo.findByPacienteId(pacienteId);

        long solicitudesActivas = solicitudes.stream()
            .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE
                      || s.getEstado() == EstadoSolicitud.ASIGNADO)
            .count();

        // Próxima cita futura con estado activo
        Optional<CitaMedica> proxima = citas.stream()
            .filter(c -> c.getEstado() == CitaMedica.EstadoCita.PROGRAMADA
                      || c.getEstado() == CitaMedica.EstadoCita.CONFIRMADA)
            .filter(c -> c.getFechaHoraCita().isAfter(LocalDateTime.now()))
            .min(Comparator.comparing(CitaMedica::getFechaHoraCita));

        // Calcular posición en cola para solicitudes PENDIENTES
        List<ListaEspera> pendientesOrdenados = listaEsperaRepo.findPendientesOrdenados();

        List<Map<String, Object>> solicitudesDTO = solicitudes.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("especialidad", s.getEspecialidad());
            m.put("hospital", s.getHospital());
            m.put("estado", s.getEstado().name());
            m.put("fechaSolicitud", s.getFechaSolicitud());
            m.put("observaciones", s.getObservaciones());
            if (s.getEstado() == EstadoSolicitud.PENDIENTE) {
                int pos = pendientesOrdenados.indexOf(s) + 1;
                m.put("posicion", pos > 0 ? pos : null);
                m.put("tiempoEstimadoMinutos", pos > 0 ? pos * 20 : null);
            } else {
                m.put("posicion", null);
                m.put("tiempoEstimadoMinutos", null);
            }
            return m;
        }).collect(Collectors.toList());

        List<Map<String, Object>> citasDTO = citas.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("estado", c.getEstado().name());
            m.put("especialidad", c.getEspecialidad());
            m.put("nombreMedico", c.getNombreMedico());
            m.put("fechaHoraCita", c.getFechaHoraCita());
            m.put("hospital", c.getHospital());
            m.put("boxNumero", c.getBoxNumero());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> proximaCitaDTO = proxima.map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("especialidad", c.getEspecialidad());
            m.put("nombreMedico", c.getNombreMedico());
            m.put("fechaHoraCita", c.getFechaHoraCita());
            m.put("hospital", c.getHospital());
            return m;
        }).orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("solicitudes", solicitudesDTO);
        response.put("citas", citasDTO);
        response.put("solicitudesActivas", solicitudesActivas);
        response.put("proximaCita", proximaCitaDTO);

        return ResponseEntity.ok(response);
    }
}
