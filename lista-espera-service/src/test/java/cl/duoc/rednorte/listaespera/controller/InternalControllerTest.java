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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalController - Pruebas unitarias")
class InternalControllerTest {

    @Mock
    private ListaEsperaRepository listaEsperaRepo;

    @Mock
    private CitaMedicaRepository citaRepo;

    @InjectMocks
    private InternalController controller;

    private MockMvc mockMvc;
    private ListaEspera solicitud;
    private CitaMedica cita;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        solicitud = ListaEspera.builder()
                .id(1L).pacienteId(10L).especialidad("Cardiologia")
                .hospital("Hospital Norte").prioridad(1)
                .estado(EstadoSolicitud.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now()).build();

        cita = CitaMedica.builder()
                .id(1L).listaEspera(solicitud).nombreMedico("Dr. Carlos Garcia")
                .especialidad("Cardiologia").hospital("Hospital Norte")
                .boxNumero("Box 3").fechaHoraCita(LocalDateTime.now().plusDays(1))
                .estado(EstadoCita.PROGRAMADA).build();
    }

    @Test
    @DisplayName("GET /lista-espera/paciente/{pacienteId} → lista de solicitudes del paciente")
    void getSolicitudesPaciente_retornaLista() throws Exception {
        when(listaEsperaRepo.findByPacienteId(10L)).thenReturn(List.of(solicitud));

        mockMvc.perform(get("/api/internal/lista-espera/paciente/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pacienteId").value(10));
    }

    @Test
    @DisplayName("GET /lista-espera/pendientes → solicitudes pendientes ordenadas")
    void getPendientesOrdenados_retornaLista() throws Exception {
        when(listaEsperaRepo.findPendientesOrdenados()).thenReturn(List.of(solicitud));

        mockMvc.perform(get("/api/internal/lista-espera/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("GET /lista-espera/pendientes/count → conteo de pendientes")
    void countPendientes_retornaConteo() throws Exception {
        when(listaEsperaRepo.findByEstado(EstadoSolicitud.PENDIENTE)).thenReturn(List.of(solicitud));

        mockMvc.perform(get("/api/internal/lista-espera/pendientes/count"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /citas/paciente/{pacienteId} → citas del paciente")
    void getCitasPaciente_retornaLista() throws Exception {
        when(citaRepo.findByPacienteId(10L)).thenReturn(List.of(cita));

        mockMvc.perform(get("/api/internal/citas/paciente/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /citas/hoy → citas del dia sin filtro")
    void getCitasHoy_sinFiltro_retornaLista() throws Exception {
        when(citaRepo.findByFechaHoraCitaBetweenOrderByFechaHoraCitaAsc(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(cita));

        mockMvc.perform(get("/api/internal/citas/hoy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /citas/hoy?nombreMedico= → citas filtradas por medico")
    void getCitasHoy_filtrandoPorMedico_retornaLista() throws Exception {
        when(citaRepo.findByNombreMedicoAndFechaHoraCitaBetween(
                eq("Dr. Carlos Garcia"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(cita));

        mockMvc.perform(get("/api/internal/citas/hoy").param("nombreMedico", "Dr. Carlos Garcia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

}

