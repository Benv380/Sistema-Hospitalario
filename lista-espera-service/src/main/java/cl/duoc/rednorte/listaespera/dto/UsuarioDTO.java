package cl.duoc.rednorte.listaespera.dto;

public record UsuarioDTO(
    Long id, 
    String email, 
    String rol, 
    String nombre
) {}