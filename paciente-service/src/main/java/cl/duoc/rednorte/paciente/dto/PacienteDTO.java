package cl.duoc.rednorte.paciente.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PacienteDTO {

    private String rut;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    private LocalDate fechaNacimiento;

    @Email(message = "Formato de email inválido")
    private String email;

    private String telefono;
    private String direccion;
}
