package atencion.arbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nodo de un árbol general (n-ario).
 * Cada nodo guarda su dato, una referencia a su padre y la lista de sus hijos directos.
 * La lista permite cualquier cantidad de hijos: una categoría puede tener 1, 2 o 10 subcategorías.
 */
public class TreeNode<T> {

    private T data;
    private TreeNode<T> parent;                         // null solo en la raíz
    private final List<TreeNode<T>> children = new ArrayList<>();

    public TreeNode(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public TreeNode<T> getParent() {
        return parent;
    }

    /** Solo lectura: los hijos se agregan y quitan con addChild/removeChild para mantener el padre. */
    public List<TreeNode<T>> getChildren() {
        return Collections.unmodifiableList(children);
    }

    void addChild(TreeNode<T> child) {
        child.parent = this;
        children.add(child);
    }

    boolean removeChild(TreeNode<T> child) {
        boolean quitado = children.remove(child);
        if (quitado) {
            child.parent = null;
        }
        return quitado;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }
}
