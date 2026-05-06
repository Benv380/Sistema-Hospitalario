package cl.duoc.rednorte.listaespera.service;

import cl.duoc.rednorte.listaespera.dto.PacienteDTO;
import cl.duoc.rednorte.listaespera.model.Paciente;
import cl.duoc.rednorte.listaespera.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service              // Spring gestiona este bean automáticamente
@RequiredArgsConstructor  // Lombok: inyección por constructor (recomendado sobre @Autowired)
@Slf4j                // Lombok: activa logger → log.info(), log.error(), etc.
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public List<Paciente> listarTodos() {
        log.info("Obteniendo todos los pacientes");
        return pacienteRepository.findAll();
    }

    public Paciente obtenerPorId(Long id) {
        return pacienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Paciente no encontrado: " + id));
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
        log.info("Paciente creado con ID: {}", guardado.getId());
        return guardado;
    }

    public Paciente actualizar(Long id, PacienteDTO dto) {
        Paciente existente = obtenerPorId(id);
        existente.setNombre(dto.getNombre());
        existente.setApellido(dto.getApellido());
        existente.setEmail(dto.getEmail());
        existente.setTelefono(dto.getTelefono());
        existente.setDireccion(dto.getDireccion());
        return pacienteRepository.save(existente);
    }

    public void eliminar(Long id) {
        obtenerPorId(id);
        pacienteRepository.deleteById(id);
        log.info("Paciente eliminado ID: {}", id);
    }
}