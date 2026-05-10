package cl.duoc.rednorte.medico.repository;

import cl.duoc.rednorte.medico.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {
    Optional<Medico> findByRut(String rut);
    List<Medico> findByEspecialidad(String especialidad);
    List<Medico> findByActivo(Boolean activo);
    Boolean existsByRut(String rut);
}
