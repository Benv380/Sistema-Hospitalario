package cl.duoc.rednorte.listaespera.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String rol;
    private String nombre;
    private String redirectUrl;
}