package cl.duoc.rednorte.paciente.service;

import cl.duoc.rednorte.paciente.dto.PacienteDTO;
import cl.duoc.rednorte.paciente.model.Paciente;
import cl.duoc.rednorte.paciente.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    public Paciente obtenerPorId(Long id) {
        return pacienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + id));
    }

    public Paciente obtenerPorUsuarioId(Long usuarioId) {
        return pacienteRepository.findByUsuarioId(usuarioId)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado para usuario: " + usuarioId));
    }

    public Paciente obtenerPorRut(String rut) {
        return pacienteRepository.findByRut(rut)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado con RUT: " + rut));
    }

    public Paciente crear(PacienteDTO dto) {
        if (pacienteRepository.existsByRut(dto.getRut())) {
            throw new RuntimeException("Ya existe paciente con RUT: " + dto.getRut());
        }
        Paciente p = Paciente.builder()
            .rut(dto.getRut())
            .nombre(dto.getNombre())
            .apellido(dto.getApellido())
            .fechaNacimiento(dto.getFechaNacimiento())
            .email(dto.getEmail())
            .telefono(dto.getTelefono())
            .direccion(dto.getDireccion())
            .build();
        Paciente guardado = pacienteRepository.save(p);
        log.info("Paciente creado ID: {}", guardado.getId());
        return guardado;
    }

    public Paciente actualizar(Long id, PacienteDTO dto) {
        Paciente existente = obtenerPorId(id);
        existente.setNombre(dto.getNombre());
        existente.setApellido(dto.getApellido());
        existente.setEmail(dto.getEmail());
        existente.setTelefono(dto.getTelefono());
        existente.setDireccion(dto.getDireccion());
        if (dto.getRut() != null && !dto.getRut().isBlank()) existente.setRut(dto.getRut());
        if (dto.getFechaNacimiento() != null) existente.setFechaNacimiento(dto.getFechaNacimiento());
        return pacienteRepository.save(existente);
    }

    public void eliminar(Long id) {
        obtenerPorId(id);
        pacienteRepository.deleteById(id);
        log.info("Paciente eliminado ID: {}", id);
    }
}
