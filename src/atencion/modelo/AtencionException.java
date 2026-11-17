package atencion.modelo;

/** Error de una regla del sistema; el menú lo captura y muestra el mensaje. */
public class AtencionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AtencionException(String mensaje) {
        super(mensaje);
    }
}
