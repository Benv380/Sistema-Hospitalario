package cl.duoc.rednorte.paciente.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthResponse {
    private Long id;
    private String token;
    private String rol;
    private String nombre;
    private String redirectUrl;

    public AuthResponse(Long id, String token, String rol, String nombre, String redirectUrl) {
        this.id = id;
        this.token = token;
        this.rol = rol;
        this.nombre = nombre;
        this.redirectUrl = redirectUrl;
    }
}
