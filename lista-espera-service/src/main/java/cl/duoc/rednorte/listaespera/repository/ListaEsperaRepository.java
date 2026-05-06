package cl.duoc.rednorte.listaespera.repository;

import cl.duoc.rednorte.listaespera.model.ListaEspera;
import cl.duoc.rednorte.listaespera.model.ListaEspera.EstadoSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ListaEsperaRepository extends JpaRepository<ListaEspera, Long> {

    List<ListaEspera> findByEstado(EstadoSolicitud estado);

    List<ListaEspera> findByPacienteId(Long pacienteId);

    List<ListaEspera> findByEspecialidadAndEstadoOrderByPrioridadAsc(
        String especialidad, EstadoSolicitud estado);

    @Query("SELECT l FROM ListaEspera l WHERE l.estado = 'PENDIENTE' ORDER BY l.prioridad ASC, l.fechaSolicitud ASC")
    List<ListaEspera> findPendientesOrdenados();
    // Query JPQL personalizada — usa nombres de clases Java, no de tablas SQL
}