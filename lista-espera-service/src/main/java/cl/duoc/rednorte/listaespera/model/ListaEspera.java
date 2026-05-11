package cl.duoc.rednorte.listaespera.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "lista_espera")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ListaEspera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(nullable = false, length = 100)
    private String especialidad;

    @Column(nullable = false, length = 100)
    private String hospital;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;

    @Column(nullable = false)
    private Integer prioridad; // 1=Alta, 2=Media, 3=Baja

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaAtencion;

    @Column(length = 500)
    private String observaciones;

    public enum EstadoSolicitud {
        PENDIENTE, ASIGNADO, CANCELADO, ATENDIDO
    }
}