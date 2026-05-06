package cl.duoc.rednorte.listaespera.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "citas_medicas")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CitaMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con la solicitud de lista de espera
    // Una cita pertenece a una solicitud
    @OneToOne
    @JoinColumn(name = "lista_espera_id", nullable = false, unique = true)
    private ListaEspera listaEspera;

    @Column(nullable = false, length = 100)
    private String nombreMedico;

    @Column(nullable = false, length = 100)
    private String especialidad;

    @Column(nullable = false, length = 100)
    private String hospital;

    @Column(nullable = false, length = 10)
    private String boxNumero;  // Ej: "Box 3", "Sala 12"

    @Column(nullable = false)
    private LocalDateTime fechaHoraCita;  // Fecha y hora exacta de la cita

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estado;

    @Column(length = 500)
    private String observaciones;

    public enum EstadoCita {
        PROGRAMADA,   // Cita agendada, esperando al paciente
        CONFIRMADA,   // Paciente confirmó asistencia
        REALIZADA,    // Atención completada
        CANCELADA,    // Cancelada → dispara reasignación automática
        NO_ASISTIO    // Paciente no se presentó
    }
}