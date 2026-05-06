package cl.duoc.rednorte.listaespera.model;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotificacionEmail implements Notificacion {
    @Override
    public void enviar(String dest, String msg) {
        log.info("[EMAIL] → {} : {}", dest, msg);
    }
    @Override
    public String getTipo() { return "EMAIL"; }
}