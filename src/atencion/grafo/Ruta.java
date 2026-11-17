package atencion.grafo;

import java.util.List;

/** Resultado de una búsqueda de camino: los vértices en orden y el costo total. */
public record Ruta<T>(List<T> vertices, double costo) {

    public boolean existe() {
        return !vertices.isEmpty();
    }

    public int transferencias() {
        return Math.max(0, vertices.size() - 1);
    }

    @Override
    public String toString() {
        if (!existe()) {
            return "(sin ruta)";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vertices.size(); i++) {
            sb.append(i == 0 ? "" : " → ").append(vertices.get(i));
        }
        return sb.toString();
    }
}
