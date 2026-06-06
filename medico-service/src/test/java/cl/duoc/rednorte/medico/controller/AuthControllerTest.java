package cl.duoc.rednorte.medico.controller;

import cl.duoc.rednorte.medico.dto.AuthResponse;
import cl.duoc.rednorte.medico.dto.CreateUsuarioRequest;
import cl.duoc.rednorte.medico.dto.LoginRequest;
import cl.duoc.rednorte.medico.service.AuthService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController (medico) - Pruebas unitarias")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("POST /login → 200 OK con token JWT")
    void login_retorna200() throws Exception {
        AuthResponse response = new AuthResponse(1L, "jwt-medico-token", "MEDICO", "Dr. Carlos Garcia", "/medico");
        when(authService.login(anyString(), anyString())).thenReturn(response);

        LoginRequest req = new LoginRequest("dr.garcia@hospital.cl", "pass123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-medico-token"))
                .andExpect(jsonPath("$.rol").value("MEDICO"))
                .andExpect(jsonPath("$.nombre").value("Dr. Carlos Garcia"));
    }

    @Test
    @DisplayName("POST /crear-usuario → 201 CREATED con datos del usuario creado")
    void crearUsuario_retorna201() throws Exception {
        AuthResponse response = new AuthResponse(3L, "jwt-nuevo-token", "MEDICO", "Ana Martinez", "/medico");
        when(authService.crearUsuario(any(CreateUsuarioRequest.class))).thenReturn(response);

        CreateUsuarioRequest req = CreateUsuarioRequest.builder()
                .email("ana.martinez@hospital.cl")
                .password("secure123")
                .nombreCompleto("Ana Martinez")
                .rol("MEDICO")
                .build();

        mockMvc.perform(post("/api/v1/auth/crear-usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-nuevo-token"))
                .andExpect(jsonPath("$.nombre").value("Ana Martinez"))
                .andExpect(jsonPath("$.id").value(3));
    }

}

