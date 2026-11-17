package atencion.grafo;

/**
 * Arista dirigida y ponderada: va desde el vértice dueño de la lista de adyacencia
 * hasta 'destination' y tiene un peso (en este sistema, minutos que tarda la transferencia).
 */
public class Edge<T> {

    private final T destination;
    private double weight;

    public Edge(T destination, double weight) {
        this.destination = destination;
        this.weight = weight;
    }

    public T getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }

    void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "→ " + destination + " (" + formatear(weight) + " min)";
    }

    static String formatear(double valor) {
        return valor == Math.rint(valor) ? String.valueOf((long) valor) : String.valueOf(valor);
    }
}
