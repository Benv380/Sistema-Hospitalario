package cl.duoc.rednorte.listaespera.dto;

import cl.duoc.rednorte.listaespera.model.ListaEspera.EstadoSolicitud;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ListaEsperaDTO {

    @NotNull(message = "El ID del paciente es obligatorio")
    private Long pacienteId;

    @NotBlank(message = "La especialidad es obligatoria")
    @Size(max = 100)
    private String especialidad;

    @NotBlank(message = "El hospital es obligatorio")
    @Size(max = 100)
    private String hospital;

    @NotNull(message = "La prioridad es obligatoria")
    @Min(value = 1, message = "Prioridad mínima es 1 (Alta)")
    @Max(value = 3, message = "Prioridad máxima es 3 (Baja)")
    private Integer prioridad; // 1=Alta, 2=Media, 3=Baja

    private String observaciones;
}