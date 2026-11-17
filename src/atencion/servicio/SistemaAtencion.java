package atencion.servicio;

import atencion.arbol.Tree;
import atencion.arbol.TreeNode;
import atencion.grafo.Edge;
import atencion.grafo.Graph;
import atencion.grafo.Ruta;
import atencion.modelo.Area;
import atencion.modelo.AtencionException;
import atencion.modelo.ElementoCatalogo;
import atencion.modelo.TipoElemento;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lógica del sistema de atención al cliente. Une las dos estructuras:
 *
 *  - ÁRBOL (catálogo): Centro → Categoría → Subcategoría → Tipo de atención.
 *  - GRAFO (red de escalamiento): Área → Área, dirigido y con minutos como peso.
 *
 * El punto de unión es el área responsable de cada tipo de atención: la hoja del
 * árbol dice en qué vértice del grafo empieza la atención de esa solicitud.
 */
public class SistemaAtencion {

    private final Tree<ElementoCatalogo> catalogo;
    private final Graph<Area> red = new Graph<>();
    private final Map<String, Area> areas = new LinkedHashMap<>();   // código → área (vértice)

    public SistemaAtencion(String codigoCentro, String nombreCentro) {
        this.catalogo = new Tree<>(new ElementoCatalogo(
                normalizar(codigoCentro), nombreCentro, TipoElemento.CENTRO));
    }

    // ================================================================== ÁRBOL: catálogo de solicitudes

    /**
     * Registra un elemento debajo de su padre. El tipo lo decide el padre:
     * debajo del centro van categorías, debajo de una categoría subcategorías
     * y debajo de una subcategoría tipos de atención.
     */
    public ElementoCatalogo registrarElemento(String codigoPadre, String codigo, String nombre,
                                              String codigoArea, int horasRespuesta) {
        ElementoCatalogo padre = elemento(codigoPadre);
        TipoElemento tipoHijo = padre.getTipo().tipoDeHijo();
        if (tipoHijo == null) {
            throw new AtencionException(padre.getCodigo() + " es un tipo de atención y no puede tener elementos debajo.");
        }
        String cod = normalizar(codigo);
        String nom = obligatorio(nombre, "El nombre es obligatorio.");
        ElementoCatalogo nuevo;
        if (tipoHijo == TipoElemento.TIPO_ATENCION) {
            Area area = area(codigoArea);            // el área debe existir en el grafo
            validarHoras(horasRespuesta);
            nuevo = ElementoCatalogo.tipoAtencion(cod, nom, area.getCodigo(), horasRespuesta);
        } else {
            nuevo = new ElementoCatalogo(cod, nom, tipoHijo);
        }
        catalogo.insert(padre.getCodigo(), nuevo);
        return nuevo;
    }

    public TreeNode<ElementoCatalogo> buscarElemento(String codigo) {
        return catalogo.find(normalizar(codigo));
    }

    public String dibujarCatalogo(String desdeCodigo) {
        return catalogo.draw(desdeCodigo == null ? null : normalizar(desdeCodigo));
    }

    public List<ElementoCatalogo> preorden() {
        return catalogo.preOrder();
    }

    public List<ElementoCatalogo> postorden() {
        return catalogo.postOrder();
    }

    public List<List<ElementoCatalogo>> porNiveles() {
        return catalogo.levelOrder();
    }

    public List<ElementoCatalogo> hijos(String codigo) {
        return catalogo.children(normalizar(codigo));
    }

    public ElementoCatalogo padre(String codigo) {
        return catalogo.parent(normalizar(codigo));
    }

    public int nivel(String codigo) {
        return catalogo.level(normalizar(codigo));
    }

    public int altura() {
        return catalogo.height();
    }

    public int alturaSubarbol(String codigo) {
        return catalogo.height(normalizar(codigo));
    }

    public List<ElementoCatalogo> rutaJerarquica(String codigo) {
        return catalogo.pathFromRoot(normalizar(codigo));
    }

    /** Modifica el dato del nodo; el nodo sigue en el mismo lugar, con el mismo padre e hijos. */
    public ElementoCatalogo modificarElemento(String codigo, String nuevoNombre,
                                              String nuevaArea, Integer nuevasHoras) {
        ElementoCatalogo e = elemento(codigo);
        if (nuevoNombre != null && !nuevoNombre.isBlank()) {
            e.setNombre(nuevoNombre.trim());
        }
        if (e.esTipoAtencion()) {
            if (nuevaArea != null && !nuevaArea.isBlank()) {
                e.setCodigoAreaResponsable(area(nuevaArea).getCodigo());
            }
            if (nuevasHoras != null) {
                validarHoras(nuevasHoras);
                e.setHorasRespuesta(nuevasHoras);
            }
        }
        return e;
    }

    /** Reubica un elemento (con todo su subárbol) bajo otro padre del nivel correcto. */
    public void reubicarElemento(String codigo, String codigoNuevoPadre) {
        ElementoCatalogo e = elemento(codigo);
        ElementoCatalogo nuevoPadre = elemento(codigoNuevoPadre);
        if (nuevoPadre.getTipo().tipoDeHijo() != e.getTipo()) {
            throw new AtencionException("Un elemento de tipo " + e.getTipo().getEtiqueta()
                    + " solo puede quedar debajo de un elemento de tipo "
                    + TipoElemento.values()[e.getTipo().getNivel() - 1].getEtiqueta() + ".");
        }
        catalogo.move(e.getCodigo(), nuevoPadre.getCodigo());
    }

    /** Política: solo se eliminan elementos sin hijos (la comprueba el árbol). */
    public ElementoCatalogo eliminarElemento(String codigo) {
        return catalogo.remove(normalizar(codigo));
    }

    public int totalElementos() {
        return catalogo.size();
    }

    public List<ElementoCatalogo> tiposDeAtencion() {
        List<ElementoCatalogo> tipos = new ArrayList<>();
        for (ElementoCatalogo e : catalogo.leaves()) {
            if (e.esTipoAtencion()) {
                tipos.add(e);
            }
        }
        return tipos;
    }

    // ================================================================== GRAFO: red de escalamiento

    public Area registrarArea(String codigo, String nombre) {
        String cod = normalizar(codigo);
        Area area = new Area(cod, obligatorio(nombre, "El nombre del área es obligatorio."));
        if (!red.addVertex(area)) {
            throw new AtencionException("Ya existe el área " + cod + ".");
        }
        areas.put(cod, area);
        return area;
    }

    /**
     * Elimina un área. No se permite si algún tipo de atención del catálogo la tiene como
     * responsable (quedaría una solicitud sin quien la atienda). Si se puede, el grafo
     * borra también todas las conexiones que entraban y salían de ella.
     */
    public int eliminarArea(String codigo) {
        Area area = area(codigo);
        List<ElementoCatalogo> dependientes = tiposAtendidosPor(area.getCodigo());
        if (!dependientes.isEmpty()) {
            List<String> codigos = new ArrayList<>();
            for (ElementoCatalogo e : dependientes) {
                codigos.add(e.getCodigo());
            }
            throw new AtencionException("El área " + area.getCodigo() + " es responsable de " + codigos
                    + ". Asigne otra área a esos tipos de atención antes de eliminarla.");
        }
        int conexiones = red.removeVertex(area);
        areas.remove(area.getCodigo());
        return conexiones;
    }

    public void crearEscalamiento(String origen, String destino, double minutos) {
        red.addEdge(area(origen), area(destino), minutos);
    }

    public boolean eliminarEscalamiento(String origen, String destino) {
        return red.removeEdge(area(origen), area(destino));
    }

    public void modificarTiempoEscalamiento(String origen, String destino, double minutos) {
        red.setWeight(area(origen), area(destino), minutos);
    }

    public List<Edge<Area>> escalamientosDe(String codigo) {
        return red.neighbors(area(codigo));
    }

    public List<Area> recibeEscalamientosDe(String codigo) {
        return red.predecessors(area(codigo));
    }

    public int gradoSalida(String codigo) {
        return red.outDegree(area(codigo));
    }

    public int gradoEntrada(String codigo) {
        return red.inDegree(area(codigo));
    }

    public Edge<Area> conexionDirecta(String origen, String destino) {
        return red.edge(area(origen), area(destino));
    }

    public List<List<Area>> bfs(String inicio) {
        return red.bfs(area(inicio));
    }

    public List<Area> dfs(String inicio) {
        return red.dfs(area(inicio));
    }

    public Ruta<Area> rutaMenosTransferencias(String origen, String destino) {
        return red.fewestHopsPath(area(origen), area(destino));
    }

    public Ruta<Area> rutaMasRapida(String origen, String destino) {
        return red.dijkstra(area(origen), area(destino));
    }

    public List<List<Area>> transferenciasCirculares() {
        return red.findCycles();
    }

    public String dibujarRed() {
        Map<Area, String> nombres = new LinkedHashMap<>();
        for (Area a : areas.values()) {
            nombres.put(a, a.getNombre());
        }
        return red.draw(nombres);
    }

    public int totalAreas() {
        return red.vertexCount();
    }

    public int totalEscalamientos() {
        return red.edgeCount();
    }

    public Area area(String codigo) {
        Area a = areas.get(normalizar(codigo));
        if (a == null) {
            throw new AtencionException("No existe el área " + normalizar(codigo) + ".");
        }
        return a;
    }

    public List<Area> areas() {
        return new ArrayList<>(areas.values());
    }

    // ================================================================== INTEGRACIÓN árbol + grafo

    /** Tipos de atención (hojas del árbol) cuya área responsable es el vértice indicado. */
    public List<ElementoCatalogo> tiposAtendidosPor(String codigoArea) {
        String cod = normalizar(codigoArea);
        List<ElementoCatalogo> resultado = new ArrayList<>();
        for (ElementoCatalogo e : tiposDeAtencion()) {
            if (cod.equals(e.getCodigoAreaResponsable())) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    /**
     * Escala una solicitud: el árbol dice a qué área llega primero (área responsable del
     * tipo de atención) y el grafo calcula, con Dijkstra, la ruta más rápida hasta el área destino.
     */
    public Ruta<Area> escalarSolicitud(String codigoTipoAtencion, String codigoAreaDestino) {
        ElementoCatalogo tipo = elemento(codigoTipoAtencion);
        if (!tipo.esTipoAtencion()) {
            throw new AtencionException(tipo.getCodigo() + " es una " + tipo.getTipo().getEtiqueta().toLowerCase()
                    + "; elija un tipo de atención (una hoja del catálogo).");
        }
        return red.dijkstra(area(tipo.getCodigoAreaResponsable()), area(codigoAreaDestino));
    }

    // ================================================================== utilidades

    public ElementoCatalogo elemento(String codigo) {
        TreeNode<ElementoCatalogo> nodo = buscarElemento(codigo);
        if (nodo == null) {
            throw new AtencionException("No existe el elemento " + normalizar(codigo) + " en el catálogo.");
        }
        return nodo.getData();
    }

    private static void validarHoras(int horas) {
        if (horas <= 0) {
            throw new AtencionException("El tiempo de respuesta debe ser mayor que cero horas.");
        }
    }

    private static String obligatorio(String texto, String mensaje) {
        if (texto == null || texto.isBlank()) {
            throw new AtencionException(mensaje);
        }
        return texto.trim();
    }

    static String normalizar(String codigo) {
        return obligatorio(codigo, "El código es obligatorio.").toUpperCase();
    }
}
