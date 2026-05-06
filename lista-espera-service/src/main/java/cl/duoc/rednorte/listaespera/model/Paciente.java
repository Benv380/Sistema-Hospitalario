package cl.duoc.rednorte.listaespera.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@Entity                     // Esta clase = tabla en base de datos
@Table(name = "pacientes") // Nombre exacto de la tabla en PostgreSQL
@Data                       // Lombok: genera getters, setters, toString, equals, hashCode
@NoArgsConstructor          // Lombok: constructor vacío (obligatorio para JPA)
@AllArgsConstructor         // Lombok: constructor con todos los campos
@Builder                    // Lombok: patrón builder para crear objetos
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremento en BD
    private Long id;

    @Column(nullable = false, length = 12, unique = true)
    private String rut;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Column(length = 150)
    private String email;

    @Column(length = 12)
    private String telefono;

    @Column(length = 200)
    private String direccion;
}