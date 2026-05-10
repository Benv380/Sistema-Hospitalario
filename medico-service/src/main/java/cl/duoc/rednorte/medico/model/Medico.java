package cl.duoc.rednorte.medico.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medicos")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 12, unique = true)
    private String rut;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    private String apellido;

    @Column(nullable = false, length = 100)
    private String especialidad;

    @Column(length = 150)
    private String email;

    @Column(length = 12)
    private String telefono;

    @Column(name = "numero_registro", length = 50)
    private String numeroRegistro;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
