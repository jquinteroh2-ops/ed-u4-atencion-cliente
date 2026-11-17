package atencion.modelo;

import java.util.Objects;

/**
 * Elemento del catálogo de solicitudes: el centro, una categoría, una subcategoría
 * o un tipo de atención. Es el dato que guarda cada nodo del árbol.
 *
 * Solo los tipos de atención (las hojas) tienen un área responsable y un tiempo
 * de respuesta; el área responsable es el punto donde el árbol se une con el grafo.
 */
public class ElementoCatalogo implements Identificable {

    private final String codigo;
    private String nombre;
    private final TipoElemento tipo;
    private String codigoAreaResponsable;   // solo TIPO_ATENCION
    private int horasRespuesta;             // solo TIPO_ATENCION (acuerdo de servicio)

    public ElementoCatalogo(String codigo, String nombre, TipoElemento tipo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public static ElementoCatalogo tipoAtencion(String codigo, String nombre,
                                                String codigoArea, int horasRespuesta) {
        ElementoCatalogo e = new ElementoCatalogo(codigo, nombre, TipoElemento.TIPO_ATENCION);
        e.codigoAreaResponsable = codigoArea;
        e.horasRespuesta = horasRespuesta;
        return e;
    }

    @Override
    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoElemento getTipo() {
        return tipo;
    }

    public boolean esTipoAtencion() {
        return tipo == TipoElemento.TIPO_ATENCION;
    }

    public String getCodigoAreaResponsable() {
        return codigoAreaResponsable;
    }

    public void setCodigoAreaResponsable(String codigoAreaResponsable) {
        this.codigoAreaResponsable = codigoAreaResponsable;
    }

    public int getHorasRespuesta() {
        return horasRespuesta;
    }

    public void setHorasRespuesta(int horasRespuesta) {
        this.horasRespuesta = horasRespuesta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ElementoCatalogo otro)) {
            return false;
        }
        return codigo.equals(otro.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        String base = codigo + " " + nombre;
        if (esTipoAtencion()) {
            base += "  [área: " + codigoAreaResponsable + ", " + horasRespuesta + " h]";
        }
        return base;
    }
}
