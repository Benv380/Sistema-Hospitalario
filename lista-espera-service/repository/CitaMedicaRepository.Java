package cl.duoc.rednorte.listaespera.repository;

import cl.duoc.rednorte.listaespera.model.CitaMedica;
import cl.duoc.rednorte.listaespera.model.CitaMedica.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaMedicaRepository extends JpaRepository<CitaMedica, Long> {

    // Buscar la cita de una solicitud específica
    Optional<CitaMedica> findByListaEsperaId(Long listaEsperaId);

    // Citas por estado (ej: todas las CANCELADAS para reasignar)
    List<CitaMedica> findByEstado(EstadoCita estado);

    // Citas de un médico en un rango de fechas
    List<CitaMedica> findByNombreMedicoAndFechaHoraCitaBetween(
        String nombreMedico,
        LocalDateTime desde,
        LocalDateTime hasta
    );

    // Citas disponibles para reasignación (CANCELADAS de hoy en adelante)
    @Query("SELECT c FROM CitaMedica c WHERE c.estado = 'CANCELADA' " +
           "AND c.fechaHoraCita >= :ahora ORDER BY c.fechaHoraCita ASC")
    List<CitaMedica> findCitasDisponiblesParaReasignacion(LocalDateTime ahora);

    // Verificar si ya existe una cita para una solicitud
    boolean existsByListaEsperaId(Long listaEsperaId);
}