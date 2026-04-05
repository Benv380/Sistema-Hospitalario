package cl.duoc.rednorte.listaespera.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CitaMedicaDTO {

    @NotNull(message = "El ID de la solicitud de lista de espera es obligatorio")
    private Long listaEsperaId;

    @NotBlank(message = "El nombre del médico es obligatorio")
    @Size(max = 100)
    private String nombreMedico;

    @NotBlank(message = "La especialidad es obligatoria")
    private String especialidad;

    @NotBlank(message = "El hospital es obligatorio")
    private String hospital;

    @NotBlank(message = "El número de box es obligatorio")
    private String boxNumero;

    @NotNull(message = "La fecha y hora de la cita es obligatoria")
    @Future(message = "La fecha de la cita debe ser en el futuro")
    private LocalDateTime fechaHoraCita;

    private String observaciones;
}