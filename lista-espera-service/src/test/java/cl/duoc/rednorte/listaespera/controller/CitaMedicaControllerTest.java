package cl.duoc.rednorte.listaespera.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cl.duoc.rednorte.listaespera.dto.CitaMedicaDTO;
import cl.duoc.rednorte.listaespera.model.CitaMedica;
import cl.duoc.rednorte.listaespera.model.CitaMedica.EstadoCita;
import cl.duoc.rednorte.listaespera.model.ListaEspera;
import cl.duoc.rednorte.listaespera.model.ListaEspera.EstadoSolicitud;
import cl.duoc.rednorte.listaespera.service.CitaMedicaService;

@ExtendWith(MockitoExtension.class)
@DisplayName("CitaMedicaController - Pruebas unitarias")
class CitaMedicaControllerTest {

    @Mock
    private CitaMedicaService citaMedicaService;

    @InjectMocks
    private CitaMedicaController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private CitaMedica cita;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        ListaEspera solicitud = ListaEspera.builder()
                .id(1L).pacienteId(10L).especialidad("Cardiologia")
                .hospital("Hospital Norte").prioridad(1)
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now()).build();

        cita = CitaMedica.builder()
                .id(100L).listaEspera(solicitud)
                .nombreMedico("Dr. Garcia").especialidad("Cardiologia")
                .hospital("Hospital Norte").boxNumero("Box 3")
                .fechaHoraCita(LocalDateTime.now().plusDays(3))
                .estado(EstadoCita.PROGRAMADA).build();
    }

    @Test
    @DisplayName("POST / → 201 CREATED al agendar cita")
    void agendar_retorna201() throws Exception {
        CitaMedicaDTO dto = CitaMedicaDTO.builder()
                .listaEsperaId(1L).nombreMedico("Dr. Garcia")
                .especialidad("Cardiologia").hospital("Hospital Norte")
                .boxNumero("Box 3").fechaHoraCita(LocalDateTime.now().plusDays(3)).build();

        when(citaMedicaService.agendar(any())).thenReturn(cita);

        mockMvc.perform(post("/api/v1/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"));
    }

    @Test
    @DisplayName("GET /{id} → 200 OK con cita")
    void obtenerPorId_retorna200() throws Exception {
        when(citaMedicaService.obtenerPorId(100L)).thenReturn(cita);

        mockMvc.perform(get("/api/v1/citas/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.nombreMedico").value("Dr. Garcia"));
    }

    @Test
    @DisplayName("GET /solicitud/{id} → 200 OK con cita de la solicitud")
    void obtenerPorSolicitud_retorna200() throws Exception {
        when(citaMedicaService.obtenerPorSolicitud(1L)).thenReturn(cita);

        mockMvc.perform(get("/api/v1/citas/solicitud/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    @DisplayName("GET /estado/{estado} → 200 OK filtrado por estado")
    void obtenerPorEstado_retorna200() throws Exception {
        when(citaMedicaService.obtenerPorEstado(EstadoCita.PROGRAMADA))
                .thenReturn(List.of(cita));

        mockMvc.perform(get("/api/v1/citas/estado/PROGRAMADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("PROGRAMADA"));
    }

    @Test
    @DisplayName("GET /estado/CANCELADA → 200 OK lista vacía")
    void obtenerPorEstado_cancelada_retorna200() throws Exception {
        when(citaMedicaService.obtenerPorEstado(EstadoCita.CANCELADA))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/citas/estado/CANCELADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /paciente/{id} → 200 OK con citas del paciente")
    void obtenerPorPaciente_retorna200() throws Exception {
        when(citaMedicaService.obtenerPorPaciente(10L)).thenReturn(List.of(cita));

        mockMvc.perform(get("/api/v1/citas/paciente/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /paciente/{id} → 200 OK sin citas")
    void obtenerPorPaciente_sinCitas_retorna200() throws Exception {
        when(citaMedicaService.obtenerPorPaciente(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/citas/paciente/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /reasignacion → 200 OK con citas disponibles")
    void obtenerDisponiblesReasignacion_retorna200() throws Exception {
        when(citaMedicaService.obtenerDisponiblesParaReasignacion()).thenReturn(List.of(cita));

        mockMvc.perform(get("/api/v1/citas/reasignacion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("PUT /{id}/confirmar → 200 OK")
    void confirmar_retorna200() throws Exception {
        cita.setEstado(EstadoCita.CONFIRMADA);
        when(citaMedicaService.confirmar(100L)).thenReturn(cita);

        mockMvc.perform(put("/api/v1/citas/100/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    @Test
    @DisplayName("PUT /{id}/realizar → 200 OK")
    void marcarRealizada_retorna200() throws Exception {
        cita.setEstado(EstadoCita.REALIZADA);
        when(citaMedicaService.marcarRealizada(100L)).thenReturn(cita);

        mockMvc.perform(put("/api/v1/citas/100/realizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REALIZADA"));
    }

    @Test
    @DisplayName("PUT /{id}/cancelar → 200 OK con motivo")
    void cancelar_conMotivo_retorna200() throws Exception {
        cita.setEstado(EstadoCita.CANCELADA);
        when(citaMedicaService.cancelar(eq(100L), any())).thenReturn(cita);

        mockMvc.perform(put("/api/v1/citas/100/cancelar")
                        .param("motivo", "Medico no disponible"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    @DisplayName("PUT /{id}/cancelar → 200 OK sin motivo usa default")
    void cancelar_sinMotivo_retorna200() throws Exception {
        cita.setEstado(EstadoCita.CANCELADA);
        when(citaMedicaService.cancelar(eq(100L), any())).thenReturn(cita);

        mockMvc.perform(put("/api/v1/citas/100/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    @Test
    @DisplayName("PUT /{id}/no-asistio → 200 OK")
    void marcarNoAsistio_retorna200() throws Exception {
        cita.setEstado(EstadoCita.NO_ASISTIO);
        when(citaMedicaService.marcarNoAsistio(100L)).thenReturn(cita);

        mockMvc.perform(put("/api/v1/citas/100/no-asistio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("NO_ASISTIO"));
    }
}