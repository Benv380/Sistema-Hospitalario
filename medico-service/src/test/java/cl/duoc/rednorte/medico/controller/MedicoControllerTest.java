package cl.duoc.rednorte.medico.controller;

import cl.duoc.rednorte.medico.model.Medico;
import cl.duoc.rednorte.medico.repository.MedicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicoInternalController - Pruebas unitarias")
class MedicoInternalControllerTest {

    @Mock
    private MedicoRepository medicoRepository;

    @InjectMocks
    private MedicoInternalController controller;

    private MockMvc mockMvc;
    private Medico medico;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver())
                .build();

        medico = Medico.builder()
                .id(1L).usuarioId(5L).nombre("Carlos").apellido("Garcia")
                .especialidad("Cardiologia").email("dr.garcia@hospital.cl")
                .activo(true).build();
    }

    @Test
    @DisplayName("GET /{id} → 200 OK con datos del medico")
    void obtenerPorId_retorna200() throws Exception {
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));

        mockMvc.perform(get("/api/internal/medicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Carlos"))
                .andExpect(jsonPath("$.especialidad").value("Cardiologia"));
    }

    @Test
    @DisplayName("GET /{id} → excepcion cuando medico no existe")
    void obtenerPorId_noExiste_lanzaExcepcion() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
            Exception.class,
            () -> mockMvc.perform(get("/api/internal/medicos/99"))
        );
    }

    @Test
    @DisplayName("GET /usuario/{usuarioId} → 200 OK con datos del medico")
    void obtenerPorUsuarioId_retorna200() throws Exception {
        when(medicoRepository.findByUsuarioId(5L)).thenReturn(Optional.of(medico));

        mockMvc.perform(get("/api/internal/medicos/usuario/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(5))
                .andExpect(jsonPath("$.apellido").value("Garcia"));
    }

    @Test
    @DisplayName("GET /usuario/{usuarioId} → excepcion cuando no existe")
    void obtenerPorUsuarioId_noExiste_lanzaExcepcion() {
        when(medicoRepository.findByUsuarioId(99L)).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
            Exception.class,
            () -> mockMvc.perform(get("/api/internal/medicos/usuario/99"))
        );
    }
}