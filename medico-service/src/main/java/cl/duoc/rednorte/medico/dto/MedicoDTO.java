package cl.duoc.rednorte.medico.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicoDTO {

    @NotBlank(message = "El RUT es obligatorio")
    private String rut;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "La especialidad es obligatoria")
    private String especialidad;

    @Email(message = "Formato de email inválido")
    private String email;

    private String telefono;
    private String numeroRegistro;
}
