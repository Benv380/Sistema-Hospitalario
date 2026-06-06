package cl.duoc.rednorte.listaespera.controller;

import cl.duoc.rednorte.listaespera.model.CitaMedica;
import cl.duoc.rednorte.listaespera.model.CitaMedica.EstadoCita;
import cl.duoc.rednorte.listaespera.model.ListaEspera;
import cl.duoc.rednorte.listaespera.model.ListaEspera.EstadoSolicitud;
import cl.duoc.rednorte.listaespera.repository.CitaMedicaRepository;
import cl.duoc.rednorte.listaespera.repository.ListaEsperaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("BffController (lista-espera) - Pruebas unitarias")
class BffControllerTest {

    @Mock
    private ListaEsperaRepository listaEsperaRepo;

    @Mock
    private CitaMedicaRepository citaRepo;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private BffController controller;

    private MockMvc mockMvc;
    private ListaEspera solicitud;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        ReflectionTestUtils.setField(controller, "medicoServiceUrl", "http://medico-service");
        ReflectionTestUtils.setField(controller, "pacienteServiceUrl", "http://paciente-service");

        solicitud = ListaEspera.builder()
                .id(1L).pacienteId(10L).especialidad("Cardiologia")
                .hospital("Hospital Norte").prioridad(1)
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now()).build();
    }

    @Test
    @DisplayName("GET /paciente/{id} → 200 OK con listas vacías")
    void dashboardPaciente_listaVacia_retorna200() throws Exception {
        when(listaEsperaRepo.findByPacienteId(1L)).thenReturn(List.of());
        when(citaRepo.findByPacienteId(1L)).thenReturn(List.of());
        when(listaEsperaRepo.findPendientesOrdenados()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/bff/paciente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solicitudes").isArray())
                .andExpect(jsonPath("$.citas").isArray())
                .andExpect(jsonPath("$.solicitudesActivas").value(0));
    }

    @Test
    @DisplayName("GET /paciente/{id} → 200 OK con solicitud PENDIENTE y posicion calculada")
    void dashboardPaciente_conSolicitudPendiente_calculaPosicion() throws Exception {
        when(listaEsperaRepo.findByPacienteId(10L)).thenReturn(List.of(solicitud));
        when(citaRepo.findByPacienteId(10L)).thenReturn(List.of());
        when(listaEsperaRepo.findPendientesOrdenados()).thenReturn(List.of(solicitud));

        mockMvc.perform(get("/api/v1/bff/paciente/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solicitudes.length()").value(1))
                .andExpect(jsonPath("$.solicitudesActivas").value(1))
                .andExpect(jsonPath("$.solicitudes[0].posicion").value(1))
                .andExpect(jsonPath("$.solicitudes[0].tiempoEstimadoMinutos").value(20));
    }

    @Test
    @DisplayName("GET /medico/{id} → 200 OK cuando medico-service no responde (null)")
    void dashboardMedico_sinNombreMedico_retorna200() throws Exception {
        when(restTemplate.getForObject(anyString(), eq(Map.class))).thenReturn(null);
        when(citaRepo.findByFechaHoraCitaBetweenOrderByFechaHoraCitaAsc(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(listaEsperaRepo.findByEstado(EstadoSolicitud.PENDIENTE)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/bff/medico/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citasHoy").isArray())
                .andExpect(jsonPath("$.totalPendientes").value(0))
                .andExpect(jsonPath("$.atendidosHoy").value(0))
                .andExpect(jsonPath("$.citasProgramadasHoy").value(0));
    }

    @Test
    @DisplayName("GET /medico/{id} → 200 OK con nombre de medico resuelto")
    void dashboardMedico_conNombreMedico_retorna200() throws Exception {
        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(Map.of("nombre", "Carlos", "apellido", "Garcia"));
        when(citaRepo.findByNombreMedicoAndFechaHoraCitaBetween(
                eq("Carlos Garcia"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(listaEsperaRepo.findByEstado(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));

        mockMvc.perform(get("/api/v1/bff/medico/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPendientes").value(1));
    }
}
