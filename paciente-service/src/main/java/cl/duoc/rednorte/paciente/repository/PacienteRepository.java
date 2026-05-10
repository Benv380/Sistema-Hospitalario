package cl.duoc.rednorte.paciente.repository;

import cl.duoc.rednorte.paciente.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByRut(String rut);
    Boolean existsByRut(String rut);
    Optional<Paciente> findByEmail(String email);
    Optional<Paciente> findByUsuarioId(Long usuarioId);
}
