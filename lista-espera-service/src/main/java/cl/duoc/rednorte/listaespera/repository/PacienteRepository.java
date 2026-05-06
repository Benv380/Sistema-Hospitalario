package cl.duoc.rednorte.listaespera.repository;

import cl.duoc.rednorte.listaespera.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    // JpaRepository te da gratis: save(), findById(), findAll(), deleteById(), count(), etc.
    // Solo declaras los métodos especiales — Spring genera el SQL automáticamente:

    Optional<Paciente> findByRut(String rut);
    // → SELECT * FROM pacientes WHERE rut = ?

    Boolean existsByRut(String rut);
    // → SELECT COUNT(*) > 0 FROM pacientes WHERE rut = ?
}