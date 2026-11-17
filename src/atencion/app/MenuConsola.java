package atencion.app;

import atencion.arbol.TreeNode;
import atencion.grafo.Edge;
import atencion.grafo.Ruta;
import atencion.modelo.Area;
import atencion.modelo.AtencionException;
import atencion.modelo.ElementoCatalogo;
import atencion.modelo.TipoElemento;
import atencion.servicio.SistemaAtencion;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Interfaz de consola. Solo pide datos y muestra resultados;
 * las reglas están en SistemaAtencion y las estructuras en Tree y Graph.
 */
public class MenuConsola {

    private final SistemaAtencion sistema;
    private final Scanner entrada = new Scanner(System.in);

    public MenuConsola(SistemaAtencion sistema) {
        this.sistema = sistema;
    }

    public void iniciar() {
        System.out.println("================================================================");
        System.out.println("   SISTEMA DE ATENCIÓN AL CLIENTE - Catálogo y escalamiento (caso 25)");
        System.out.println("================================================================");
        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Opción: ");
            try {
                ejecutar(opcion);
            } catch (AtencionException e) {
                System.out.println("  [!] " + e.getMessage());
            }
        } while (opcion != 0);
        System.out.println("Hasta luego.");
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("---------- CATÁLOGO DE SOLICITUDES (árbol) ----------");
        System.out.println(" 1. Registrar categoría, subcategoría o tipo de atención");
        System.out.println(" 2. Ver catálogo de solicitudes (jerarquía)");
        System.out.println(" 3. Buscar en el catálogo por código");
        System.out.println(" 4. Recorridos del catálogo (preorden, postorden, por niveles)");
        System.out.println(" 5. Consultar nivel, padre, hijos y altura");
        System.out.println(" 6. Modificar elemento del catálogo");
        System.out.println(" 7. Reubicar elemento del catálogo");
        System.out.println(" 8. Eliminar elemento del catálogo");
        System.out.println("---------- RED DE ESCALAMIENTO ENTRE ÁREAS (grafo) ----------");
        System.out.println(" 9. Registrar área responsable");
        System.out.println("10. Eliminar área responsable");
        System.out.println("11. Crear escalamiento entre áreas");
        System.out.println("12. Eliminar escalamiento");
        System.out.println("13. Modificar tiempo de un escalamiento");
        System.out.println("14. Ver escalamientos de un área (vecinos y grado)");
        System.out.println("15. Verificar escalamiento directo entre dos áreas");
        System.out.println("16. Recorrido BFS de escalamiento (por niveles)");
        System.out.println("17. Recorrido DFS de escalamiento (en profundidad)");
        System.out.println("18. Ruta con menos transferencias (BFS)");
        System.out.println("19. Ruta de escalamiento más rápida (Dijkstra)");
        System.out.println("20. Detectar transferencias circulares (ciclos)");
        System.out.println("---------- INTEGRACIÓN ----------");
        System.out.println("21. Escalar una solicitud del catálogo hasta un área");
        System.out.println("22. Información completa del sistema");
        System.out.println(" 0. Salir");
    }

    private void ejecutar(int opcion) {
        switch (opcion) {
            case 1 -> registrarElemento();
            case 2 -> verCatalogo();
            case 3 -> buscarElemento();
            case 4 -> recorridosCatalogo();
            case 5 -> consultarElemento();
            case 6 -> modificarElemento();
            case 7 -> reubicarElemento();
            case 8 -> eliminarElemento();
            case 9 -> registrarArea();
            case 10 -> eliminarArea();
            case 11 -> crearEscalamiento();
            case 12 -> eliminarEscalamiento();
            case 13 -> modificarTiempo();
            case 14 -> verEscalamientos();
            case 15 -> verificarConexion();
            case 16 -> bfs();
            case 17 -> dfs();
            case 18 -> rutaMenosTransferencias();
            case 19 -> rutaMasRapida();
            case 20 -> transferenciasCirculares();
            case 21 -> escalarSolicitud();
            case 22 -> informacionCompleta();
            case 0 -> { }
            default -> System.out.println("  [!] Opción no válida.");
        }
    }

    // ================================================================== árbol

    private void registrarElemento() {
        String padre = leer("Código del elemento padre (ej. CAC, FAC, SOP-INT): ");
        ElementoCatalogo p = sistema.elemento(padre);
        TipoElemento tipoHijo = p.getTipo().tipoDeHijo();
        if (tipoHijo == null) {
            throw new AtencionException(p.getCodigo() + " es un tipo de atención y no puede tener elementos debajo.");
        }
        System.out.println("  Nuevo elemento de tipo \"" + tipoHijo.getEtiqueta() + "\" dentro de " + p);
        String codigo = leer("Código nuevo: ");
        String nombre = leer("Nombre: ");
        String area = null;
        int horas = 0;
        if (tipoHijo == TipoElemento.TIPO_ATENCION) {
            System.out.println("  Áreas disponibles: " + sistema.areas());
            area = sistema.area(leer("Área responsable: ")).getCodigo();   // se valida antes de seguir
            horas = leerEntero("Tiempo máximo de respuesta (horas): ");
        }
        ElementoCatalogo nuevo = sistema.registrarElemento(padre, codigo, nombre, area, horas);
        System.out.println("  [OK] Registrado " + nuevo + " en el nivel " + sistema.nivel(nuevo.getCodigo()));
        System.out.println("       Ruta: " + textoRuta(sistema.rutaJerarquica(nuevo.getCodigo())));
    }

    private void verCatalogo() {
        String desde = leer("Código desde donde dibujar (Enter = todo el catálogo): ");
        System.out.println();
        System.out.print(sistema.dibujarCatalogo(desde.isBlank() ? null : desde));
        System.out.println("  Elementos: " + sistema.totalElementos() + " | Altura: " + sistema.altura());
    }

    private void buscarElemento() {
        String codigo = leer("Código a buscar: ");
        TreeNode<ElementoCatalogo> nodo = sistema.buscarElemento(codigo);
        if (nodo == null) {
            System.out.println("  [X] No existe el elemento " + codigo.trim().toUpperCase()
                    + " en el catálogo (se recorrió todo el árbol sin encontrarlo).");
            return;
        }
        ElementoCatalogo e = nodo.getData();
        System.out.println("  [OK] " + e.getTipo().getEtiqueta() + ": " + e);
        System.out.println("       Ruta: " + textoRuta(sistema.rutaJerarquica(e.getCodigo())));
        System.out.println("       Nivel " + sistema.nivel(e.getCodigo()) + " | Hijos: " + nodo.getChildren().size());
        if (e.esTipoAtencion()) {
            Area a = sistema.area(e.getCodigoAreaResponsable());
            System.out.println("       Lo atiende: " + a.descripcion() + " → vértice de la red de escalamiento");
        }
    }

    private void recorridosCatalogo() {
        System.out.println();
        System.out.println("PREORDEN (profundidad: primero el nodo y luego cada rama completa):");
        imprimirLista(sistema.preorden());
        System.out.println();
        System.out.println("POSTORDEN (profundidad: primero los hijos, el padre al final; la raíz sale de última):");
        imprimirLista(sistema.postorden());
        System.out.println();
        System.out.println("POR NIVELES (anchura con una cola: la raíz, luego todo el nivel 1, luego el 2...):");
        List<List<ElementoCatalogo>> niveles = sistema.porNiveles();
        for (int i = 0; i < niveles.size(); i++) {
            System.out.println("  Nivel " + i + ": " + codigos(niveles.get(i)));
        }
    }

    private void consultarElemento() {
        String codigo = leer("Código del elemento: ");
        ElementoCatalogo e = sistema.elemento(codigo);
        ElementoCatalogo padre = sistema.padre(codigo);
        List<ElementoCatalogo> hijos = sistema.hijos(codigo);
        System.out.println("  Elemento: " + e + " (" + e.getTipo().getEtiqueta() + ")");
        System.out.println("  Padre:    " + (padre == null ? "ninguno, es la raíz" : padre));
        System.out.println("  Nivel:    " + sistema.nivel(codigo) + "   (aristas desde la raíz)");
        System.out.println("  Hijos directos (" + hijos.size() + "):");
        for (ElementoCatalogo h : hijos) {
            System.out.println("     - " + h);
        }
        System.out.println("  Altura de su subárbol: " + sistema.alturaSubarbol(codigo));
        System.out.println("  Altura de todo el catálogo: " + sistema.altura());
    }

    private void modificarElemento() {
        String codigo = leer("Código del elemento a modificar: ");
        ElementoCatalogo e = sistema.elemento(codigo);
        System.out.println("  Actual: " + e);
        String nombre = leer("Nuevo nombre (Enter = no cambiar): ");
        String area = null;
        Integer horas = null;
        if (e.esTipoAtencion()) {
            area = leer("Nueva área responsable " + sistema.areas() + " (Enter = no cambiar): ");
            String h = leer("Nuevo tiempo de respuesta en horas (Enter = no cambiar): ");
            horas = h.isBlank() ? null : Integer.valueOf(h.trim());
        }
        sistema.modificarElemento(codigo, nombre, area, horas);
        System.out.println("  [OK] Modificado: " + e);
        System.out.println("       Sigue en: " + textoRuta(sistema.rutaJerarquica(e.getCodigo()))
                + " con " + sistema.hijos(e.getCodigo()).size() + " hijo(s)");
    }

    private void reubicarElemento() {
        String codigo = leer("Código del elemento a reubicar: ");
        String nuevoPadre = leer("Código del nuevo padre: ");
        System.out.println("  Antes:   " + textoRuta(sistema.rutaJerarquica(codigo)));
        sistema.reubicarElemento(codigo, nuevoPadre);
        System.out.println("  [OK] Ahora: " + textoRuta(sistema.rutaJerarquica(codigo)));
    }

    private void eliminarElemento() {
        String codigo = leer("Código del elemento a eliminar: ");
        ElementoCatalogo eliminado = sistema.eliminarElemento(codigo);
        System.out.println("  [OK] Eliminado " + eliminado + ". Elementos en el catálogo: " + sistema.totalElementos());
    }

    // ================================================================== grafo

    private void registrarArea() {
        String codigo = leer("Código del área (ej. TEL): ");
        String nombre = leer("Nombre del área: ");
        Area a = sistema.registrarArea(codigo, nombre);
        System.out.println("  [OK] Área registrada: " + a.descripcion() + ". Aún no tiene escalamientos.");
    }

    private void eliminarArea() {
        String codigo = leer("Código del área a eliminar: ");
        int conexiones = sistema.eliminarArea(codigo);
        System.out.println("  [OK] Área " + codigo.trim().toUpperCase() + " eliminada junto con "
                + conexiones + " escalamiento(s) que entraban o salían de ella.");
    }

    private void crearEscalamiento() {
        String origen = leer("Área que escala (origen): ");
        String destino = leer("Área que recibe (destino): ");
        double minutos = leerDecimal("Minutos que tarda la transferencia: ");
        sistema.crearEscalamiento(origen, destino, minutos);
        System.out.println("  [OK] Escalamiento " + origen.trim().toUpperCase() + " → "
                + destino.trim().toUpperCase() + " creado. Es dirigido: no crea el regreso.");
    }

    private void eliminarEscalamiento() {
        String origen = leer("Área origen: ");
        String destino = leer("Área destino: ");
        if (sistema.eliminarEscalamiento(origen, destino)) {
            System.out.println("  [OK] Escalamiento eliminado.");
        } else {
            System.out.println("  [X] No existía un escalamiento " + origen.trim().toUpperCase()
                    + " → " + destino.trim().toUpperCase() + ".");
        }
    }

    private void modificarTiempo() {
        String origen = leer("Área origen: ");
        String destino = leer("Área destino: ");
        double minutos = leerDecimal("Nuevo tiempo en minutos: ");
        sistema.modificarTiempoEscalamiento(origen, destino, minutos);
        System.out.println("  [OK] " + origen.trim().toUpperCase() + " " + sistema.conexionDirecta(origen, destino));
    }

    private void verEscalamientos() {
        String codigo = leer("Código del área: ");
        Area a = sistema.area(codigo);
        List<Edge<Area>> salida = sistema.escalamientosDe(codigo);
        List<Area> entrada = sistema.recibeEscalamientosDe(codigo);
        System.out.println("  " + a.descripcion());
        System.out.println("  Puede escalar a (salida):   " + (salida.isEmpty() ? "ninguna" : salida));
        System.out.println("  Recibe escalamientos de (entrada): " + (entrada.isEmpty() ? "ninguna" : entrada));
        System.out.println("  Grado de salida: " + sistema.gradoSalida(codigo)
                + " | Grado de entrada: " + sistema.gradoEntrada(codigo));
        System.out.println("  Tipos de atención que inician aquí: " + codigos(sistema.tiposAtendidosPor(codigo)));
    }

    private void verificarConexion() {
        String origen = leer("Área origen: ");
        String destino = leer("Área destino: ");
        Edge<Area> arista = sistema.conexionDirecta(origen, destino);
        sistema.area(destino);
        String o = origen.trim().toUpperCase();
        String d = destino.trim().toUpperCase();
        if (arista != null) {
            System.out.println("  [OK] Sí existe el escalamiento directo " + o + " " + arista);
        } else {
            System.out.println("  [X] No existe escalamiento directo " + o + " → " + d
                    + (sistema.conexionDirecta(destino, origen) != null
                    ? " (sí existe en sentido contrario " + d + " → " + o + ")" : "") + ".");
        }
    }

    private void bfs() {
        String inicio = leer("Área de inicio: ");
        List<List<Area>> niveles = sistema.bfs(inicio);
        System.out.println("  BFS usa una COLA: primero todas las áreas a 1 transferencia, luego a 2...");
        int total = 0;
        for (int i = 0; i < niveles.size(); i++) {
            System.out.println("  Nivel " + i + " (" + i + " transferencia(s)): " + niveles.get(i));
            total += niveles.get(i).size();
        }
        System.out.println("  Áreas alcanzables desde " + inicio.trim().toUpperCase() + ": " + total
                + " de " + sistema.totalAreas());
    }

    private void dfs() {
        String inicio = leer("Área de inicio: ");
        List<Area> orden = sistema.dfs(inicio);
        System.out.println("  DFS sigue una rama hasta el fondo y luego regresa (recursión = pila):");
        System.out.println("  " + unir(orden));
        System.out.println("  Áreas alcanzables: " + orden.size() + " de " + sistema.totalAreas());
    }

    private void rutaMenosTransferencias() {
        String origen = leer("Área origen: ");
        String destino = leer("Área destino: ");
        Ruta<Area> ruta = sistema.rutaMenosTransferencias(origen, destino);
        imprimirRuta(ruta, "menos transferencias (BFS)", origen, destino);
    }

    private void rutaMasRapida() {
        String origen = leer("Área origen: ");
        String destino = leer("Área destino: ");
        Ruta<Area> ruta = sistema.rutaMasRapida(origen, destino);
        imprimirRuta(ruta, "más rápida (Dijkstra)", origen, destino);
    }

    private void transferenciasCirculares() {
        List<List<Area>> ciclos = sistema.transferenciasCirculares();
        if (ciclos.isEmpty()) {
            System.out.println("  [OK] No hay transferencias circulares: ninguna solicitud puede quedar rebotando.");
            return;
        }
        System.out.println("  [!] Se encontraron " + ciclos.size() + " transferencia(s) circular(es):");
        for (List<Area> ciclo : ciclos) {
            System.out.println("      " + unir(ciclo));
        }
        System.out.println("  Una solicitud podría quedar rebotando entre esas áreas sin resolverse.");
    }

    // ================================================================== integración

    private void escalarSolicitud() {
        String tipo = leer("Código del tipo de atención (ej. SOP-INT-01): ");
        String destino = leer("Área a la que debe llegar (ej. GER, DEF): ");
        ElementoCatalogo e = sistema.elemento(tipo);
        Ruta<Area> ruta = sistema.escalarSolicitud(tipo, destino);
        System.out.println("  Catálogo (árbol): " + textoRuta(sistema.rutaJerarquica(e.getCodigo())));
        if (e.esTipoAtencion()) {
            System.out.println("  Área responsable (inicio en el grafo): "
                    + sistema.area(e.getCodigoAreaResponsable()).descripcion());
        }
        imprimirRuta(ruta, "de escalamiento más rápida", e.getCodigoAreaResponsable(), destino);
        if (ruta.existe()) {
            System.out.println("  Acuerdo de servicio: " + e.getHorasRespuesta() + " h de respuesta.");
        }
    }

    private void informacionCompleta() {
        System.out.println();
        System.out.println("================ CATÁLOGO DE SOLICITUDES (árbol general) ================");
        System.out.print(sistema.dibujarCatalogo(null));
        System.out.println("Elementos: " + sistema.totalElementos() + " | Altura: " + sistema.altura()
                + " | Tipos de atención (hojas): " + sistema.tiposDeAtencion().size());
        System.out.println();
        System.out.println("================ RED DE ESCALAMIENTO (grafo dirigido y ponderado) ================");
        System.out.print(sistema.dibujarRed());
        System.out.println("Áreas (vértices): " + sistema.totalAreas()
                + " | Escalamientos (aristas): " + sistema.totalEscalamientos());
        System.out.println();
        System.out.println("================ INTEGRACIÓN: área responsable de cada tipo de atención ================");
        for (Area a : sistema.areas()) {
            List<ElementoCatalogo> tipos = sistema.tiposAtendidosPor(a.getCodigo());
            if (!tipos.isEmpty()) {
                System.out.println(String.format("  %-5s ← %s", a, codigos(tipos)));
            }
        }
    }

    // ================================================================== utilidades de presentación

    private void imprimirRuta(Ruta<Area> ruta, String tipo, String origen, String destino) {
        if (!ruta.existe()) {
            System.out.println("  [X] No hay ruta de " + origen.trim().toUpperCase() + " a "
                    + destino.trim().toUpperCase() + ": no hay escalamientos que lleven hasta allá.");
            return;
        }
        System.out.println("  [OK] Ruta " + tipo + ": " + ruta);
        System.out.println("       Transferencias: " + ruta.transferencias()
                + " | Tiempo total: " + formatear(ruta.costo()) + " min");
    }

    private static void imprimirLista(List<ElementoCatalogo> lista) {
        StringBuilder linea = new StringBuilder("  ");
        for (int i = 0; i < lista.size(); i++) {
            String texto = (i + 1) + "." + lista.get(i).getCodigo() + "  ";
            if (linea.length() + texto.length() > 96) {
                System.out.println(linea);
                linea = new StringBuilder("  ");
            }
            linea.append(texto);
        }
        System.out.println(linea);
    }

    private static String textoRuta(List<ElementoCatalogo> camino) {
        StringBuilder sb = new StringBuilder();
        for (ElementoCatalogo e : camino) {
            sb.append(sb.length() == 0 ? "" : " > ").append(e.getNombre());
        }
        return sb.toString();
    }

    private static String codigos(List<ElementoCatalogo> lista) {
        StringBuilder sb = new StringBuilder("[");
        for (ElementoCatalogo e : lista) {
            sb.append(sb.length() == 1 ? "" : ", ").append(e.getCodigo());
        }
        return sb.append("]").toString();
    }

    private static String unir(List<Area> areas) {
        StringBuilder sb = new StringBuilder();
        for (Area a : areas) {
            sb.append(sb.length() == 0 ? "" : " → ").append(a);
        }
        return sb.toString();
    }

    private static String formatear(double v) {
        return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
    }

    private String leer(String mensaje) {
        System.out.print(mensaje);
        try {
            return entrada.nextLine();
        } catch (NoSuchElementException e) {
            System.out.println();
            System.exit(0);
            return "";
        }
    }

    private int leerEntero(String mensaje) {
        while (true) {
            String texto = leer(mensaje).trim();
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Escriba un número entero.");
            }
        }
    }

    private double leerDecimal(String mensaje) {
        while (true) {
            String texto = leer(mensaje).trim().replace(',', '.');
            try {
                return Double.parseDouble(texto);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Escriba un número.");
            }
        }
    }
}
