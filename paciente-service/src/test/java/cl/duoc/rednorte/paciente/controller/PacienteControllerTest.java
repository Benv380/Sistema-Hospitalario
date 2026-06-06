package cl.duoc.rednorte.paciente.controller;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import cl.duoc.rednorte.paciente.dto.PacienteDTO;
import cl.duoc.rednorte.paciente.model.Paciente;
import cl.duoc.rednorte.paciente.service.PacienteService;

@ExtendWith(MockitoExtension.class)
@DisplayName("PacienteController - Pruebas unitarias")
class PacienteControllerTest {

    @Mock
    private PacienteService pacienteService;

    @InjectMocks
    private PacienteController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        paciente = Paciente.builder()
                .id(1L).rut("12345678-9").nombre("Juan").apellido("Perez")
                .email("juan@mail.com").telefono("56912345678")
                .fechaNacimiento(LocalDate.of(1990, 5, 15)).build();
    }

    @Test
    @DisplayName("GET / → 200 OK con lista de pacientes")
    void listarTodos_retorna200() throws Exception {
        when(pacienteService.listarTodos()).thenReturn(List.of(paciente));

        mockMvc.perform(get("/api/v1/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET / → 200 OK lista vacía")
    void listarTodos_retorna200_listaVacia() throws Exception {
        when(pacienteService.listarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /buscar?q= → 200 OK con resultados")
    void buscar_retorna200() throws Exception {
        when(pacienteService.buscar("12345678-9")).thenReturn(List.of(paciente));

        mockMvc.perform(get("/api/v1/pacientes/buscar").param("q", "12345678-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].rut").value("12345678-9"));
    }

    @Test
    @DisplayName("GET /buscar?q= → 200 OK sin resultados")
    void buscar_sinResultados_retorna200() throws Exception {
        when(pacienteService.buscar("noexiste")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/pacientes/buscar").param("q", "noexiste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /{id} → 200 OK con paciente")
    void obtenerPorId_retorna200() throws Exception {
        when(pacienteService.obtenerPorId(1L)).thenReturn(paciente);

        mockMvc.perform(get("/api/v1/pacientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rut").value("12345678-9"))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @DisplayName("GET /usuario/{usuarioId} → 200 OK con paciente")
    void obtenerPorUsuarioId_retorna200() throws Exception {
        when(pacienteService.obtenerPorUsuarioId(5L)).thenReturn(paciente);

        mockMvc.perform(get("/api/v1/pacientes/usuario/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rut").value("12345678-9"));
    }

    @Test
    @DisplayName("POST / → 201 CREATED al registrar paciente")
    void crear_retorna201() throws Exception {
        PacienteDTO dto = PacienteDTO.builder()
                .rut("12345678-9").nombre("Juan").apellido("Perez")
                .email("juan@mail.com").build();

        when(pacienteService.crear(any())).thenReturn(paciente);

        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @DisplayName("PUT /{id} → 200 OK al actualizar paciente")
    void actualizar_retorna200() throws Exception {
        PacienteDTO dto = PacienteDTO.builder()
                .nombre("Juan Carlos").apellido("Perez").email("jc@mail.com").build();

        when(pacienteService.actualizar(eq(1L), any())).thenReturn(paciente);

        mockMvc.perform(put("/api/v1/pacientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /{id} → 204 NO CONTENT")
    void eliminar_retorna204() throws Exception {
        doNothing().when(pacienteService).eliminar(1L);

        mockMvc.perform(delete("/api/v1/pacientes/1"))
                .andExpect(status().isNoContent());
    }
}