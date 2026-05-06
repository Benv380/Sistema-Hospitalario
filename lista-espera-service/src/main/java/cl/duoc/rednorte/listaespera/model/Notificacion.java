package cl.duoc.rednorte.listaespera.model;



public interface Notificacion {
    void enviar(String destinatario, String mensaje);
    String getTipo();
}