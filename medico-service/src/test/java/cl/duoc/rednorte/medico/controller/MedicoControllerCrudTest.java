package cl.duoc.rednorte.medico.controller;

import cl.duoc.rednorte.medico.dto.MedicoDTO;
import cl.duoc.rednorte.medico.model.Medico;
import cl.duoc.rednorte.medico.service.MedicoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicoController - Pruebas unitarias")
class MedicoControllerCrudTest {

    @Mock
    private MedicoService medicoService;

    @InjectMocks
    private MedicoController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Medico medico;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        medico = Medico.builder()
                .id(1L).usuarioId(5L).rut("12345678-9")
                .nombre("Carlos").apellido("Garcia")
                .especialidad("Cardiologia").email("dr.garcia@hospital.cl")
                .activo(true).build();
    }

    @Test
    @DisplayName("GET / → 200 OK con lista de médicos activos")
    void listarTodos_retorna200() throws Exception {
        when(medicoService.listarActivos()).thenReturn(List.of(medico));

        mockMvc.perform(get("/api/v1/medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Carlos"));
    }

    @Test
    @DisplayName("GET /{id} → 200 OK con datos del médico")
    void obtenerPorId_retorna200() throws Exception {
        when(medicoService.obtenerPorId(1L)).thenReturn(medico);

        mockMvc.perform(get("/api/v1/medicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.especialidad").value("Cardiologia"));
    }

    @Test
    @DisplayName("GET /especialidad/{especialidad} → 200 OK con médicos filtrados")
    void listarPorEspecialidad_retorna200() throws Exception {
        when(medicoService.listarPorEspecialidad("Cardiologia")).thenReturn(List.of(medico));

        mockMvc.perform(get("/api/v1/medicos/especialidad/Cardiologia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].especialidad").value("Cardiologia"));
    }

    @Test
    @DisplayName("POST / → 201 CREATED al registrar médico")
    void crear_retorna201() throws Exception {
        MedicoDTO dto = MedicoDTO.builder()
                .rut("12345678-9").nombre("Carlos").apellido("Garcia")
                .especialidad("Cardiologia").email("dr.garcia@hospital.cl").build();

        when(medicoService.crear(any())).thenReturn(medico);

        mockMvc.perform(post("/api/v1/medicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Carlos"));
    }

    @Test
    @DisplayName("PUT /{id} → 200 OK al actualizar médico")
    void actualizar_retorna200() throws Exception {
        MedicoDTO dto = MedicoDTO.builder()
                .rut("12345678-9").nombre("Carlos Alberto").apellido("Garcia")
                .especialidad("Cardiologia").email("dr.garcia@hospital.cl").build();

        when(medicoService.actualizar(eq(1L), any())).thenReturn(medico);

        mockMvc.perform(put("/api/v1/medicos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /{id} → 204 NO CONTENT")
    void eliminar_retorna204() throws Exception {
        doNothing().when(medicoService).eliminar(1L);

        mockMvc.perform(delete("/api/v1/medicos/1"))
                .andExpect(status().isNoContent());
    }
}
