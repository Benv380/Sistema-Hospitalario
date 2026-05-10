package cl.duoc.rednorte.medico.service;

import cl.duoc.rednorte.medico.dto.MedicoDTO;
import cl.duoc.rednorte.medico.model.Medico;
import cl.duoc.rednorte.medico.repository.MedicoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicoService {

    private final MedicoRepository medicoRepository;

    public List<Medico> listarTodos() {
        return medicoRepository.findAll();
    }

    public List<Medico> listarActivos() {
        return medicoRepository.findByActivo(true);
    }

    public List<Medico> listarPorEspecialidad(String especialidad) {
        return medicoRepository.findByEspecialidad(especialidad);
    }

    public Medico obtenerPorId(Long id) {
        return medicoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Médico no encontrado: " + id));
    }

    public Medico crear(MedicoDTO dto) {
        if (medicoRepository.existsByRut(dto.getRut())) {
            throw new RuntimeException("Ya existe médico con RUT: " + dto.getRut());
        }
        Medico m = Medico.builder()
            .rut(dto.getRut())
            .nombre(dto.getNombre())
            .apellido(dto.getApellido())
            .especialidad(dto.getEspecialidad())
            .email(dto.getEmail())
            .telefono(dto.getTelefono())
            .numeroRegistro(dto.getNumeroRegistro())
            .activo(true)
            .build();
        Medico guardado = medicoRepository.save(m);
        log.info("Médico creado ID: {}", guardado.getId());
        return guardado;
    }

    public Medico actualizar(Long id, MedicoDTO dto) {
        Medico existente = obtenerPorId(id);
        existente.setNombre(dto.getNombre());
        existente.setApellido(dto.getApellido());
        existente.setEspecialidad(dto.getEspecialidad());
        existente.setEmail(dto.getEmail());
        existente.setTelefono(dto.getTelefono());
        return medicoRepository.save(existente);
    }

    public void eliminar(Long id) {
        obtenerPorId(id);
        medicoRepository.deleteById(id);
        log.info("Médico eliminado ID: {}", id);
    }
}
