package cl.duoc.rednorte.listaespera.controller;

import cl.duoc.rednorte.listaespera.dto.AuthResponse;
import cl.duoc.rednorte.listaespera.dto.LoginRequest;
import cl.duoc.rednorte.listaespera.dto.RegisterRequest;
import cl.duoc.rednorte.listaespera.service.AuthService;
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
@DisplayName("AuthController (lista-espera) - Pruebas unitarias")
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
        AuthResponse response = new AuthResponse(1L, "jwt-token", "PACIENTE", "Juan Perez", "/paciente");
        when(authService.login(anyString(), anyString())).thenReturn(response);

        LoginRequest req = new LoginRequest("juan@mail.com", "pass123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.rol").value("PACIENTE"))
                .andExpect(jsonPath("$.nombre").value("Juan Perez"));
    }

    @Test
    @DisplayName("POST /register → 201 CREATED con token")
    void register_retorna201() throws Exception {
        AuthResponse response = new AuthResponse(3L, "register-token", "PACIENTE", "Maria Lopez", "/paciente");
        when(authService.registrar(any(RegisterRequest.class))).thenReturn(response);

        RegisterRequest req = new RegisterRequest("maria@mail.com", "pass456", "Maria Lopez");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("register-token"))
                .andExpect(jsonPath("$.nombre").value("Maria Lopez"))
                .andExpect(jsonPath("$.rol").value("PACIENTE"));
    }

}

