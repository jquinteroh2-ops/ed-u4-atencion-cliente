package atencion.modelo;

/** Error de una regla del sistema; el menú lo captura y muestra el mensaje. */
public class AtencionException extends RuntimeException {
    public AtencionException(String mensaje) {
        super(mensaje);
    }
}
