package cl.duoc.rednorte.paciente.controller;

import cl.duoc.rednorte.paciente.model.Paciente;
import cl.duoc.rednorte.paciente.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("PacienteInternalController - Pruebas unitarias")
class PacienteInternalControllerTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteInternalController controller;

    private MockMvc mockMvc;
    private Paciente paciente;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        paciente = Paciente.builder()
                .id(1L).usuarioId(5L).rut("12345678-9")
                .nombre("Juan").apellido("Perez")
                .email("juan@mail.com").fechaNacimiento(LocalDate.of(1990, 5, 15))
                .build();
    }

    @Test
    @DisplayName("GET /{usuarioId} → 200 OK con datos del paciente")
    void obtenerDatosPaciente_retorna200() throws Exception {
        when(pacienteRepository.findByUsuarioId(5L)).thenReturn(Optional.of(paciente));

        mockMvc.perform(get("/api/internal/pacientes/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.apellido").value("Perez"))
                .andExpect(jsonPath("$.rut").value("12345678-9"))
                .andExpect(jsonPath("$.email").value("juan@mail.com"));
    }

    @Test
    @DisplayName("GET /{usuarioId} → retorna campos vacíos cuando nombre/apellido son null")
    void obtenerDatosPaciente_conCamposNulos_retornaCadenaVacia() throws Exception {
        Paciente sinDatos = Paciente.builder().id(2L).usuarioId(9L).build();
        when(pacienteRepository.findByUsuarioId(9L)).thenReturn(Optional.of(sinDatos));

        mockMvc.perform(get("/api/internal/pacientes/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(""))
                .andExpect(jsonPath("$.apellido").value(""));
    }

    @Test
    @DisplayName("GET /{usuarioId} → lanza excepción cuando paciente no existe")
    void obtenerDatosPaciente_noExiste_lanzaExcepcion() {
        when(pacienteRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class,
                () -> mockMvc.perform(get("/api/internal/pacientes/99")));
    }
}
