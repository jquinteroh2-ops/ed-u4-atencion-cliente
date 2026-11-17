package atencion.arbol;

import atencion.modelo.AtencionException;
import atencion.modelo.Identificable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Árbol general: cada nodo puede tener cualquier número de hijos.
 * Los nodos se identifican por el código de su dato (Identificable).
 *
 * Política de eliminación: NO se elimina un nodo que tenga hijos. Primero hay que
 * eliminar o reubicar sus descendientes. Así nunca se pierden subárboles por accidente.
 */
public class Tree<T extends Identificable> {

    private TreeNode<T> root;

    public Tree(T rootData) {
        this.root = new TreeNode<>(rootData);
    }

    public TreeNode<T> getRoot() {
        return root;
    }

    // ------------------------------------------------------------------ inserción

    /** Agrega un nuevo nodo como hijo del nodo cuyo código es codigoPadre. */
    public TreeNode<T> insert(String codigoPadre, T data) {
        TreeNode<T> padre = find(codigoPadre);
        if (padre == null) {
            throw new AtencionException("No existe el elemento padre " + codigoPadre + ".");
        }
        if (find(data.getCodigo()) != null) {
            throw new AtencionException("Ya existe un elemento con el código " + data.getCodigo() + ".");
        }
        TreeNode<T> nuevo = new TreeNode<>(data);
        padre.addChild(nuevo);
        return nuevo;
    }

    // ------------------------------------------------------------------ búsqueda

    /** Búsqueda en profundidad desde la raíz. Devuelve null si el código no existe. */
    public TreeNode<T> find(String codigo) {
        return find(root, codigo);
    }

    private TreeNode<T> find(TreeNode<T> actual, String codigo) {
        if (actual.getData().getCodigo().equalsIgnoreCase(codigo)) {
            return actual;
        }
        for (TreeNode<T> hijo : actual.getChildren()) {
            TreeNode<T> encontrado = find(hijo, codigo);
            if (encontrado != null) {
                return encontrado;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------ eliminación y reubicación

    /** Elimina un nodo hoja. Si tiene hijos se impide la eliminación (política del sistema). */
    public T remove(String codigo) {
        TreeNode<T> nodo = obligatorio(codigo);
        if (nodo.isRoot()) {
            throw new AtencionException("No se puede eliminar la raíz del catálogo.");
        }
        if (!nodo.isLeaf()) {
            throw new AtencionException(codigo + " tiene " + nodo.getChildren().size()
                    + " elemento(s) dependiente(s). Elimínelos o reubíquelos primero.");
        }
        nodo.getParent().removeChild(nodo);
        return nodo.getData();
    }

    /** Mueve un nodo (con todo su subárbol) para que quede debajo de otro padre. */
    public void move(String codigo, String codigoNuevoPadre) {
        TreeNode<T> nodo = obligatorio(codigo);
        TreeNode<T> nuevoPadre = obligatorio(codigoNuevoPadre);
        if (nodo.isRoot()) {
            throw new AtencionException("La raíz no se puede reubicar.");
        }
        if (esDescendiente(nuevoPadre, nodo)) {
            throw new AtencionException("No se puede mover " + codigo + " debajo de sí mismo o de uno de sus descendientes.");
        }
        nodo.getParent().removeChild(nodo);
        nuevoPadre.addChild(nodo);
    }

    /** true si 'posible' es 'ancestro' o está dentro de su subárbol (sube por los padres). */
    private boolean esDescendiente(TreeNode<T> posible, TreeNode<T> ancestro) {
        for (TreeNode<T> n = posible; n != null; n = n.getParent()) {
            if (n == ancestro) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ consultas

    public List<T> children(String codigo) {
        List<T> resultado = new ArrayList<>();
        for (TreeNode<T> hijo : obligatorio(codigo).getChildren()) {
            resultado.add(hijo.getData());
        }
        return resultado;
    }

    /** Padre directo, o null si el nodo es la raíz. */
    public T parent(String codigo) {
        TreeNode<T> padre = obligatorio(codigo).getParent();
        return padre == null ? null : padre.getData();
    }

    /** Nivel = número de aristas desde la raíz. Se cuenta subiendo por las referencias al padre. */
    public int level(String codigo) {
        int nivel = 0;
        for (TreeNode<T> n = obligatorio(codigo); n.getParent() != null; n = n.getParent()) {
            nivel++;
        }
        return nivel;
    }

    /** Altura de todo el árbol. */
    public int height() {
        return height(root);
    }

    /** Altura del subárbol de un nodo. */
    public int height(String codigo) {
        return height(obligatorio(codigo));
    }

    /** Una hoja tiene altura 0; un nodo interno tiene 1 + la mayor altura de sus hijos. */
    private int height(TreeNode<T> nodo) {
        int mayor = -1;
        for (TreeNode<T> hijo : nodo.getChildren()) {
            mayor = Math.max(mayor, height(hijo));
        }
        return mayor + 1;
    }

    /** Camino desde la raíz hasta el nodo (ruta jerárquica). */
    public List<T> pathFromRoot(String codigo) {
        List<T> camino = new ArrayList<>();
        for (TreeNode<T> n = obligatorio(codigo); n != null; n = n.getParent()) {
            camino.add(0, n.getData());
        }
        return camino;
    }

    public int size() {
        return preOrder().size();
    }

    public List<T> leaves() {
        List<T> hojas = new ArrayList<>();
        for (TreeNode<T> n : preOrderNodes()) {
            if (n.isLeaf()) {
                hojas.add(n.getData());
            }
        }
        return hojas;
    }

    // ------------------------------------------------------------------ recorridos

    /** Preorden (profundidad): primero el nodo, después cada hijo completo de izquierda a derecha. */
    public List<T> preOrder() {
        List<T> resultado = new ArrayList<>();
        for (TreeNode<T> n : preOrderNodes()) {
            resultado.add(n.getData());
        }
        return resultado;
    }

    private List<TreeNode<T>> preOrderNodes() {
        List<TreeNode<T>> resultado = new ArrayList<>();
        preOrder(root, resultado);
        return resultado;
    }

    private void preOrder(TreeNode<T> nodo, List<TreeNode<T>> resultado) {
        resultado.add(nodo);                         // 1. visitar el nodo
        for (TreeNode<T> hijo : nodo.getChildren()) {
            preOrder(hijo, resultado);               // 2. recorrer cada subárbol
        }
    }

    /** Postorden (profundidad): primero todos los hijos, al final el nodo. La raíz sale de última. */
    public List<T> postOrder() {
        List<T> resultado = new ArrayList<>();
        postOrder(root, resultado);
        return resultado;
    }

    private void postOrder(TreeNode<T> nodo, List<T> resultado) {
        for (TreeNode<T> hijo : nodo.getChildren()) {
            postOrder(hijo, resultado);              // 1. recorrer cada subárbol
        }
        resultado.add(nodo.getData());               // 2. visitar el nodo
    }

    /**
     * Recorrido por niveles (anchura) con una cola: primero la raíz, luego todos los de nivel 1,
     * luego los de nivel 2... Devuelve una lista por nivel.
     */
    public List<List<T>> levelOrder() {
        List<List<T>> niveles = new ArrayList<>();
        Queue<TreeNode<T>> cola = new ArrayDeque<>();
        cola.offer(root);
        while (!cola.isEmpty()) {
            int enEsteNivel = cola.size();           // los que están en la cola son de un mismo nivel
            List<T> nivel = new ArrayList<>();
            for (int i = 0; i < enEsteNivel; i++) {
                TreeNode<T> actual = cola.poll();
                nivel.add(actual.getData());
                for (TreeNode<T> hijo : actual.getChildren()) {
                    cola.offer(hijo);                // los hijos se atienden en la siguiente vuelta
                }
            }
            niveles.add(nivel);
        }
        return niveles;
    }

    // ------------------------------------------------------------------ representación

    /** Dibuja el árbol con ramas ├── └── (o solo el subárbol del código indicado). */
    public String draw(String codigoInicio) {
        TreeNode<T> inicio = codigoInicio == null ? root : obligatorio(codigoInicio);
        StringBuilder sb = new StringBuilder();
        sb.append(inicio.getData()).append('\n');
        List<TreeNode<T>> hijos = inicio.getChildren();
        for (int i = 0; i < hijos.size(); i++) {
            draw(hijos.get(i), "", i == hijos.size() - 1, sb);
        }
        return sb.toString();
    }

    private void draw(TreeNode<T> nodo, String prefijo, boolean ultimo, StringBuilder sb) {
        sb.append(prefijo).append(ultimo ? "└── " : "├── ").append(nodo.getData()).append('\n');
        String prefijoHijos = prefijo + (ultimo ? "    " : "│   ");
        List<TreeNode<T>> hijos = nodo.getChildren();
        for (int i = 0; i < hijos.size(); i++) {
            draw(hijos.get(i), prefijoHijos, i == hijos.size() - 1, sb);
        }
    }

    private TreeNode<T> obligatorio(String codigo) {
        TreeNode<T> nodo = find(codigo);
        if (nodo == null) {
            throw new AtencionException("No existe el elemento " + codigo + " en el catálogo.");
        }
        return nodo;
    }
}
