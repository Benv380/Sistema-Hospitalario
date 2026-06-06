package cl.duoc.rednorte.bff.controller;

import cl.duoc.rednorte.bff.client.ListaEsperaClient;
import cl.duoc.rednorte.bff.client.MedicoClient;
import cl.duoc.rednorte.bff.client.PacienteClient;
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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("BffController (bff-service) - Pruebas unitarias")
class BffControllerTest {

    @Mock
    private ListaEsperaClient listaEsperaClient;

    @Mock
    private MedicoClient medicoClient;

    @Mock
    private PacienteClient pacienteClient;

    @InjectMocks
    private BffController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    @DisplayName("GET /paciente/{id} → 200 OK con listas vacías")
    void dashboardPaciente_sinDatos_retorna200() throws Exception {
        when(listaEsperaClient.getSolicitudesPaciente(1L)).thenReturn(List.of());
        when(listaEsperaClient.getCitasPaciente(1L)).thenReturn(List.of());
        when(listaEsperaClient.getPendientesOrdenados()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/bff/paciente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solicitudes").isArray())
                .andExpect(jsonPath("$.citas").isArray())
                .andExpect(jsonPath("$.solicitudesActivas").value(0));
    }

    @Test
    @DisplayName("GET /paciente/{id} → 200 OK con solicitud PENDIENTE y posicion calculada")
    void dashboardPaciente_conSolicitudPendiente_calculaPosicion() throws Exception {
        Map<String, Object> solicitud = new LinkedHashMap<>();
        solicitud.put("id", 1);
        solicitud.put("estado", "PENDIENTE");
        solicitud.put("especialidad", "Cardiologia");

        when(listaEsperaClient.getSolicitudesPaciente(1L)).thenReturn(List.of(solicitud));
        when(listaEsperaClient.getCitasPaciente(1L)).thenReturn(List.of());
        when(listaEsperaClient.getPendientesOrdenados()).thenReturn(List.of(solicitud));

        mockMvc.perform(get("/api/v1/bff/paciente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.solicitudesActivas").value(1))
                .andExpect(jsonPath("$.solicitudes[0].posicion").value(1))
                .andExpect(jsonPath("$.solicitudes[0].tiempoEstimadoMinutos").value(20));
    }

    @Test
    @DisplayName("GET /medico/{id} → 200 OK con resumen del día")
    void dashboardMedico_conDatos_retorna200() throws Exception {
        when(medicoClient.getNombreMedico(5L)).thenReturn("Dr. Carlos Garcia");
        when(listaEsperaClient.getCitasHoy(anyString(), isNull())).thenReturn(List.of());
        when(listaEsperaClient.countPendientes()).thenReturn(3L);
        when(pacienteClient.getNombresPacientes(any(Set.class))).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/api/v1/bff/medico/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citasHoy").isArray())
                .andExpect(jsonPath("$.totalPendientes").value(3))
                .andExpect(jsonPath("$.atendidosHoy").value(0))
                .andExpect(jsonPath("$.citasProgramadasHoy").value(0));
    }

    @Test
    @DisplayName("GET /medico/{id} → 200 OK cuando medico-service no responde")
    void dashboardMedico_sinNombreMedico_retorna200() throws Exception {
        when(medicoClient.getNombreMedico(anyLong())).thenReturn(null);
        when(listaEsperaClient.getCitasHoy(isNull(), isNull())).thenReturn(List.of());
        when(listaEsperaClient.countPendientes()).thenReturn(0L);
        when(pacienteClient.getNombresPacientes(any(Set.class))).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/api/v1/bff/medico/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPendientes").value(0));
    }
}
