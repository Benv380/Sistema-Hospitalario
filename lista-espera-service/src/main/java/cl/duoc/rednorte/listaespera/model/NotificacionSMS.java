package cl.duoc.rednorte.listaespera.model;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotificacionSMS implements Notificacion {
    @Override
    public void enviar(String dest, String msg) {
        log.info("[SMS] → {} : {}", dest, msg);
    }
    @Override
    public String getTipo() { return "SMS"; }
}