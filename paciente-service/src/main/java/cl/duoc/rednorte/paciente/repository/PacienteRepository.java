package cl.duoc.rednorte.paciente.repository;

import cl.duoc.rednorte.paciente.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    Optional<Paciente> findByRut(String rut);
    Boolean existsByRut(String rut);
    Optional<Paciente> findByEmail(String email);
    Optional<Paciente> findByUsuarioId(Long usuarioId);

    @Query("SELECT p FROM Paciente p WHERE LOWER(p.email) = LOWER(:q) OR LOWER(p.rut) = LOWER(:q)")
    List<Paciente> buscarPorEmailORut(@Param("q") String q);
}
