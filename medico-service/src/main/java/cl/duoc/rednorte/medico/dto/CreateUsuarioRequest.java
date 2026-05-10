package cl.duoc.rednorte.medico.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUsuarioRequest {
    private String email;
    private String password;
    private String nombreCompleto;
    private String rol;
}
