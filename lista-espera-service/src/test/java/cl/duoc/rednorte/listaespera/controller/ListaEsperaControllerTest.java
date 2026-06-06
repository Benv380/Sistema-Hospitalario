package cl.duoc.rednorte.listaespera.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import cl.duoc.rednorte.listaespera.dto.ListaEsperaDTO;
import cl.duoc.rednorte.listaespera.model.ListaEspera;
import cl.duoc.rednorte.listaespera.model.ListaEspera.EstadoSolicitud;
import cl.duoc.rednorte.listaespera.service.ListaEsperaService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListaEsperaController - Pruebas unitarias")
class ListaEsperaControllerTest {

    @Mock
    private ListaEsperaService listaEsperaService;

    @InjectMocks
    private ListaEsperaController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private ListaEspera solicitud;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
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
    }

    @Test
    @DisplayName("GET /pendientes → 200 OK con lista de solicitudes")
    void obtenerPendientes_retorna200() throws Exception {
        when(listaEsperaService.obtenerPendientes()).thenReturn(List.of(solicitud));

        mockMvc.perform(get("/api/v1/lista-espera/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /pendientes → 200 OK lista vacía")
    void obtenerPendientes_retorna200_listaVacia() throws Exception {
        when(listaEsperaService.obtenerPendientes()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/lista-espera/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /estado/{estado} → 200 OK filtrado por estado")
    void obtenerPorEstado_retorna200() throws Exception {
        when(listaEsperaService.obtenerPorEstado(EstadoSolicitud.PENDIENTE))
                .thenReturn(List.of(solicitud));

        mockMvc.perform(get("/api/v1/lista-espera/estado/PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /estado/ASIGNADO → 200 OK")
    void obtenerPorEstado_asignado_retorna200() throws Exception {
        solicitud.setEstado(EstadoSolicitud.ASIGNADO);
        when(listaEsperaService.obtenerPorEstado(EstadoSolicitud.ASIGNADO))
                .thenReturn(List.of(solicitud));

        mockMvc.perform(get("/api/v1/lista-espera/estado/ASIGNADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("ASIGNADO"));
    }

    @Test
    @DisplayName("GET /paciente/{id} → 200 OK con solicitudes del paciente")
    void obtenerPorPaciente_retorna200() throws Exception {
        when(listaEsperaService.obtenerPorPaciente(10L)).thenReturn(List.of(solicitud));

        mockMvc.perform(get("/api/v1/lista-espera/paciente/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /paciente/{id} → 200 OK lista vacía")
    void obtenerPorPaciente_sinSolicitudes_retorna200() throws Exception {
        when(listaEsperaService.obtenerPorPaciente(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/lista-espera/paciente/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /{id} → 200 OK con solicitud")
    void obtenerPorId_retorna200() throws Exception {
        when(listaEsperaService.obtenerPorId(1L)).thenReturn(solicitud);

        mockMvc.perform(get("/api/v1/lista-espera/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.especialidad").value("Cardiologia"));
    }

    @Test
    @DisplayName("POST / → 201 CREATED al registrar solicitud")
    void registrar_retorna201() throws Exception {
        ListaEsperaDTO dto = ListaEsperaDTO.builder()
                .pacienteId(10L).especialidad("Cardiologia")
                .hospital("Hospital Norte").prioridad(1).build();

        when(listaEsperaService.registrar(any())).thenReturn(solicitud);

        mockMvc.perform(post("/api/v1/lista-espera")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.pacienteId").value(10));
    }

    @Test
    @DisplayName("PUT /{id}/cancelar → 200 OK")
    void cancelar_retorna200() throws Exception {
        solicitud.setEstado(EstadoSolicitud.CANCELADO);
        when(listaEsperaService.cancelar(1L)).thenReturn(solicitud);

        mockMvc.perform(put("/api/v1/lista-espera/1/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    @Test
    @DisplayName("PUT /{id}/asignar → 200 OK")
    void asignar_retorna200() throws Exception {
        solicitud.setEstado(EstadoSolicitud.ASIGNADO);
        when(listaEsperaService.asignar(1L)).thenReturn(solicitud);

        mockMvc.perform(put("/api/v1/lista-espera/1/asignar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ASIGNADO"));
    }

    @Test
    @DisplayName("PUT /{id}/atender → 200 OK")
    void atender_retorna200() throws Exception {
        solicitud.setEstado(EstadoSolicitud.ATENDIDO);
        when(listaEsperaService.atender(1L)).thenReturn(solicitud);

        mockMvc.perform(put("/api/v1/lista-espera/1/atender"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ATENDIDO"));
    }

    @Test
    @DisplayName("DELETE /{id} → 204 NO CONTENT")
    void eliminar_retorna204() throws Exception {
        doNothing().when(listaEsperaService).eliminar(1L);

        mockMvc.perform(delete("/api/v1/lista-espera/1"))
                .andExpect(status().isNoContent());
    }
}