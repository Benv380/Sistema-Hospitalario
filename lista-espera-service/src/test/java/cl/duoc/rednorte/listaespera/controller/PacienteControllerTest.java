package cl.duoc.rednorte.listaespera.controller;

import cl.duoc.rednorte.listaespera.dto.PacienteDTO;
import cl.duoc.rednorte.listaespera.model.Paciente;
import cl.duoc.rednorte.listaespera.service.PacienteService;
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
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PacienteController (lista-espera) - Pruebas unitarias")
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
                .email("juan@mail.com").fechaNacimiento(LocalDate.of(1990, 5, 15)).build();
    }

    @Test
    @DisplayName("GET / → 200 OK con lista de pacientes")
    void listarTodos_retorna200() throws Exception {
        when(pacienteService.listarTodos()).thenReturn(List.of(paciente));

        mockMvc.perform(get("/api/v1/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Juan"));
    }

    @Test
    @DisplayName("GET /{id} → 200 OK con datos del paciente")
    void obtenerPorId_retorna200() throws Exception {
        when(pacienteService.obtenerPorId(1L)).thenReturn(paciente);

        mockMvc.perform(get("/api/v1/pacientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rut").value("12345678-9"))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    @DisplayName("POST / → 201 CREATED al crear paciente")
    void crear_retorna201() throws Exception {
        PacienteDTO dto = PacienteDTO.builder()
                .rut("12345678-9").nombre("Juan").apellido("Perez")
                .email("juan@mail.com").fechaNacimiento(LocalDate.of(1990, 5, 15)).build();

        when(pacienteService.crear(any())).thenReturn(paciente);

        mockMvc.perform(post("/api/v1/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rut").value("12345678-9"));
    }

    @Test
    @DisplayName("PUT /{id} → 200 OK al actualizar paciente")
    void actualizar_retorna200() throws Exception {
        PacienteDTO dto = PacienteDTO.builder()
                .rut("12345678-9").nombre("Juan Carlos").apellido("Perez")
                .email("jc@mail.com").fechaNacimiento(LocalDate.of(1990, 5, 15)).build();

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
