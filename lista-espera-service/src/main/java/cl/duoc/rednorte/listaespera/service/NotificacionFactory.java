package cl.duoc.rednorte.listaespera.service;

import cl.duoc.rednorte.listaespera.model.*;
import org.springframework.stereotype.Component;

@Component
public class NotificacionFactory {

    // Factory Method: crea el tipo correcto de notificación según el parámetro
    public Notificacion crear(String tipo) {
        return switch (tipo.toUpperCase()) {
            case "EMAIL" -> new NotificacionEmail();
            case "SMS"   -> new NotificacionSMS();
            default -> throw new IllegalArgumentException("Tipo desconocido: " + tipo);
        };
    }
}
// Uso en cualquier servicio:
// Notificacion n = factory.crear("EMAIL");
// n.enviar("paciente@mail.com", "Su cita fue confirmada");