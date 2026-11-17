package atencion.grafo;

import atencion.modelo.AtencionException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * Grafo DIRIGIDO y PONDERADO implementado con lista de adyacencia.
 *
 *   adjacency:  vértice  →  lista de aristas que SALEN de ese vértice
 *
 * Se usa LinkedHashMap para que los vértices se recorran en el orden en que se registraron;
 * así BFS y DFS dan siempre el mismo resultado y se pueden explicar.
 */
public class Graph<T> {

    private final Map<T, List<Edge<T>>> adjacency = new LinkedHashMap<>();

    // ------------------------------------------------------------------ vértices

    public boolean addVertex(T v) {
        if (adjacency.containsKey(v)) {
            return false;
        }
        adjacency.put(v, new ArrayList<>());
        return true;
    }

    /**
     * Elimina el vértice y todas las aristas que dependen de él:
     * las que salen (su propia lista) y las que llegan (están en las listas de los demás).
     * Devuelve cuántas aristas se eliminaron en total.
     */
    public int removeVertex(T v) {
        List<Edge<T>> salientes = adjacency.remove(v);
        if (salientes == null) {
            throw new AtencionException("No existe el vértice " + v + ".");
        }
        int eliminadas = salientes.size();
        for (List<Edge<T>> lista : adjacency.values()) {
            int antes = lista.size();
            lista.removeIf(e -> e.getDestination().equals(v));
            eliminadas += antes - lista.size();
        }
        return eliminadas;
    }

    public boolean containsVertex(T v) {
        return adjacency.containsKey(v);
    }

    public Set<T> vertices() {
        return Collections.unmodifiableSet(adjacency.keySet());
    }

    public int vertexCount() {
        return adjacency.size();
    }

    public int edgeCount() {
        int total = 0;
        for (List<Edge<T>> lista : adjacency.values()) {
            total += lista.size();
        }
        return total;
    }

    // ------------------------------------------------------------------ aristas

    /** Crea la arista dirigida origen → destino. Al ser dirigido NO se crea destino → origen. */
    public void addEdge(T origen, T destino, double peso) {
        obligatorio(origen);
        obligatorio(destino);
        if (origen.equals(destino)) {
            throw new AtencionException("Un área no puede escalar hacia sí misma.");
        }
        if (peso <= 0) {
            throw new AtencionException("El tiempo de la transferencia debe ser mayor que cero.");
        }
        if (hasEdge(origen, destino)) {
            throw new AtencionException("Ya existe la conexión " + origen + " → " + destino + ".");
        }
        adjacency.get(origen).add(new Edge<>(destino, peso));
    }

    public boolean removeEdge(T origen, T destino) {
        return obligatorio(origen).removeIf(e -> e.getDestination().equals(destino));
    }

    public void setWeight(T origen, T destino, double peso) {
        if (peso <= 0) {
            throw new AtencionException("El tiempo de la transferencia debe ser mayor que cero.");
        }
        Edge<T> arista = edge(origen, destino);
        if (arista == null) {
            throw new AtencionException("No existe la conexión " + origen + " → " + destino + ".");
        }
        arista.setWeight(peso);
    }

    /** Conexión directa: ¿está 'destino' en la lista de adyacencia de 'origen'? */
    public boolean hasEdge(T origen, T destino) {
        return edge(origen, destino) != null;
    }

    public Edge<T> edge(T origen, T destino) {
        for (Edge<T> e : obligatorio(origen)) {
            if (e.getDestination().equals(destino)) {
                return e;
            }
        }
        return null;
    }

    /** Vecinos de salida: hacia dónde puede escalar este vértice. */
    public List<Edge<T>> neighbors(T v) {
        return Collections.unmodifiableList(obligatorio(v));
    }

    /** Vértices que tienen una arista hacia v (quién le escala a este vértice). */
    public List<T> predecessors(T v) {
        obligatorio(v);
        List<T> resultado = new ArrayList<>();
        for (Map.Entry<T, List<Edge<T>>> entrada : adjacency.entrySet()) {
            for (Edge<T> e : entrada.getValue()) {
                if (e.getDestination().equals(v)) {
                    resultado.add(entrada.getKey());
                }
            }
        }
        return resultado;
    }

    public int outDegree(T v) {
        return obligatorio(v).size();
    }

    public int inDegree(T v) {
        return predecessors(v).size();
    }

    // ------------------------------------------------------------------ BFS

    /**
     * Recorrido en anchura (BFS) con una COLA. Devuelve los vértices agrupados por nivel:
     * nivel 0 = inicio, nivel 1 = sus vecinos directos, nivel 2 = vecinos de los vecinos...
     */
    public List<List<T>> bfs(T inicio) {
        obligatorio(inicio);
        List<List<T>> niveles = new ArrayList<>();
        Set<T> visitados = new HashSet<>();
        Queue<T> cola = new ArrayDeque<>();
        visitados.add(inicio);
        cola.offer(inicio);
        while (!cola.isEmpty()) {
            int enEsteNivel = cola.size();
            List<T> nivel = new ArrayList<>();
            for (int i = 0; i < enEsteNivel; i++) {
                T actual = cola.poll();
                nivel.add(actual);
                for (Edge<T> e : adjacency.get(actual)) {
                    T vecino = e.getDestination();
                    if (visitados.add(vecino)) {     // add devuelve false si ya estaba
                        cola.offer(vecino);
                    }
                }
            }
            niveles.add(nivel);
        }
        return niveles;
    }

    // ------------------------------------------------------------------ DFS

    /**
     * Recorrido en profundidad (DFS) recursivo: se sigue una rama hasta el fondo y solo
     * al no tener más vecinos sin visitar se regresa (la pila es la pila de llamadas).
     */
    public List<T> dfs(T inicio) {
        obligatorio(inicio);
        List<T> orden = new ArrayList<>();
        dfs(inicio, new HashSet<>(), orden);
        return orden;
    }

    private void dfs(T actual, Set<T> visitados, List<T> orden) {
        visitados.add(actual);
        orden.add(actual);
        for (Edge<T> e : adjacency.get(actual)) {
            if (!visitados.contains(e.getDestination())) {
                dfs(e.getDestination(), visitados, orden);
            }
        }
    }

    // ------------------------------------------------------------------ caminos

    /**
     * Camino con MENOS conexiones usando BFS: como BFS avanza por niveles, la primera vez
     * que llega al destino lo hace con el menor número de saltos. Se guarda de dónde se
     * llegó a cada vértice (anterior) para reconstruir el camino al final.
     */
    public Ruta<T> fewestHopsPath(T origen, T destino) {
        obligatorio(origen);
        obligatorio(destino);
        Map<T, T> anterior = new HashMap<>();
        Set<T> visitados = new HashSet<>();
        Queue<T> cola = new ArrayDeque<>();
        visitados.add(origen);
        cola.offer(origen);
        while (!cola.isEmpty()) {
            T actual = cola.poll();
            if (actual.equals(destino)) {
                List<T> camino = reconstruir(anterior, origen, destino);
                return new Ruta<>(camino, costoDe(camino));
            }
            for (Edge<T> e : adjacency.get(actual)) {
                if (visitados.add(e.getDestination())) {
                    anterior.put(e.getDestination(), actual);
                    cola.offer(e.getDestination());
                }
            }
        }
        return new Ruta<>(List.of(), Double.POSITIVE_INFINITY);
    }

    /**
     * Dijkstra: ruta de MENOR COSTO (menos minutos) entre dos vértices.
     * Una PriorityQueue entrega siempre el vértice pendiente con la menor distancia conocida;
     * desde él se intenta mejorar ("relajar") la distancia de sus vecinos.
     */
    public Ruta<T> dijkstra(T origen, T destino) {
        obligatorio(origen);
        obligatorio(destino);
        Map<T, Double> distancia = new HashMap<>();
        Map<T, T> anterior = new HashMap<>();
        Set<T> cerrados = new HashSet<>();
        PriorityQueue<Map.Entry<T, Double>> pendientes =
                new PriorityQueue<>(Map.Entry.comparingByValue());

        distancia.put(origen, 0.0);
        pendientes.offer(Map.entry(origen, 0.0));
        while (!pendientes.isEmpty()) {
            T actual = pendientes.poll().getKey();
            if (!cerrados.add(actual)) {
                continue;                            // ya se procesó con una distancia menor
            }
            if (actual.equals(destino)) {
                break;
            }
            for (Edge<T> e : adjacency.get(actual)) {
                double nueva = distancia.get(actual) + e.getWeight();
                T vecino = e.getDestination();
                if (nueva < distancia.getOrDefault(vecino, Double.POSITIVE_INFINITY)) {
                    distancia.put(vecino, nueva);
                    anterior.put(vecino, actual);
                    pendientes.offer(Map.entry(vecino, nueva));
                }
            }
        }
        if (!distancia.containsKey(destino)) {
            return new Ruta<>(List.of(), Double.POSITIVE_INFINITY);
        }
        return new Ruta<>(reconstruir(anterior, origen, destino), distancia.get(destino));
    }

    private List<T> reconstruir(Map<T, T> anterior, T origen, T destino) {
        List<T> camino = new ArrayList<>();
        for (T v = destino; v != null; v = anterior.get(v)) {
            camino.add(0, v);
            if (v.equals(origen)) {
                break;
            }
        }
        return camino;
    }

    public double costoDe(List<T> camino) {
        double total = 0;
        for (int i = 0; i + 1 < camino.size(); i++) {
            total += edge(camino.get(i), camino.get(i + 1)).getWeight();
        }
        return total;
    }

    // ------------------------------------------------------------------ ciclos

    /**
     * Busca ciclos dirigidos con DFS y tres estados por vértice:
     *   sin visitar  →  EN_CAMINO (está en la rama actual)  →  TERMINADO.
     * Si desde un vértice se llega a otro que sigue EN_CAMINO, hay un ciclo:
     * se encontró una arista que regresa a la rama que se está explorando.
     * Devuelve todos los ciclos encontrados (cada uno empieza y termina en el mismo vértice).
     */
    public List<List<T>> findCycles() {
        Map<T, Integer> estado = new HashMap<>();   // 1 = en camino, 2 = terminado
        Deque<T> rama = new ArrayDeque<>();
        List<List<T>> ciclos = new ArrayList<>();
        for (T v : adjacency.keySet()) {
            if (!estado.containsKey(v)) {
                buscarCiclos(v, estado, rama, ciclos);
            }
        }
        return ciclos;
    }

    private void buscarCiclos(T actual, Map<T, Integer> estado, Deque<T> rama, List<List<T>> ciclos) {
        estado.put(actual, 1);
        rama.addLast(actual);
        for (Edge<T> e : adjacency.get(actual)) {
            T vecino = e.getDestination();
            Integer est = estado.get(vecino);
            if (est == null) {
                buscarCiclos(vecino, estado, rama, ciclos);
            } else if (est == 1) {                   // arista de retroceso: hay un ciclo
                List<T> ciclo = new ArrayList<>();
                boolean dentro = false;
                for (T r : rama) {
                    dentro = dentro || r.equals(vecino);
                    if (dentro) {
                        ciclo.add(r);
                    }
                }
                ciclo.add(vecino);
                ciclos.add(ciclo);
            }
        }
        rama.removeLast();
        estado.put(actual, 2);
    }

    // ------------------------------------------------------------------ representación

    public String draw(Map<T, String> nombres) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<T, List<Edge<T>>> entrada : adjacency.entrySet()) {
            T v = entrada.getKey();
            sb.append(String.format("%-5s", v));
            if (nombres != null && nombres.containsKey(v)) {
                sb.append(String.format("%-32s", nombres.get(v)));
            }
            sb.append(entrada.getValue().isEmpty() ? "(sin escalamientos de salida)" : entrada.getValue())
              .append('\n');
        }
        return sb.toString();
    }

    private List<Edge<T>> obligatorio(T v) {
        List<Edge<T>> lista = adjacency.get(v);
        if (lista == null) {
            throw new AtencionException("No existe el vértice " + v + ".");
        }
        return lista;
    }
}
