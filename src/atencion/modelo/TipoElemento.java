package atencion.modelo;

/**
 * Nivel que ocupa un elemento dentro del catálogo de solicitudes.
 * El número de nivel es la profundidad esperada en el árbol (la raíz es 0).
 */
public enum TipoElemento {
    CENTRO(0, "Centro de atención"),
    CATEGORIA(1, "Categoría"),
    SUBCATEGORIA(2, "Subcategoría"),
    TIPO_ATENCION(3, "Tipo de atención");

    private final int nivel;
    private final String etiqueta;

    TipoElemento(int nivel, String etiqueta) {
        this.nivel = nivel;
        this.etiqueta = etiqueta;
    }

    public int getNivel() {
        return nivel;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Tipo que deben tener los hijos de un elemento de este tipo (null si no admite hijos). */
    public TipoElemento tipoDeHijo() {
        return switch (this) {
            case CENTRO -> CATEGORIA;
            case CATEGORIA -> SUBCATEGORIA;
            case SUBCATEGORIA -> TIPO_ATENCION;
            case TIPO_ATENCION -> null;
        };
    }
}
