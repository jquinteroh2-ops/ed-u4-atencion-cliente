package atencion.modelo;

import java.util.Objects;

/**
 * Área responsable de atender solicitudes (Mesa de Ayuda, Facturación, Jurídica...).
 * Cada área es un vértice del grafo de escalamiento.
 */
public class Area {

    private final String codigo;
    private String nombre;

    public Area(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** Dos áreas son la misma si tienen el mismo código: así el grafo no repite vértices. */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Area otra)) {
            return false;
        }
        return codigo.equals(otra.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }

    @Override
    public String toString() {
        return codigo;
    }

    public String descripcion() {
        return codigo + " (" + nombre + ")";
    }
}
