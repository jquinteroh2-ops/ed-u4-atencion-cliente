package atencion.pruebas;

import atencion.grafo.Edge;
import atencion.grafo.Ruta;
import atencion.modelo.Area;
import atencion.modelo.AtencionException;
import atencion.modelo.ElementoCatalogo;
import atencion.servicio.DatosPrueba;
import atencion.servicio.SistemaAtencion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Pruebas automáticas sin librerías externas. Cada prueba compara el resultado esperado
 * con el obtenido y muestra [PASA] o [FALLA]. Ejecutar:
 *     java -cp out atencion.pruebas.PruebasSistema
 */
public final class PruebasSistema {

    private static int pasan;
    private static int fallan;

    private PruebasSistema() {
    }

    public static void main(String[] args) {
        pruebasArbol();
        pruebasGrafo();
        pruebasIntegracion();
        System.out.println();
        System.out.println("Resultado: " + pasan + " pasan, " + fallan + " fallan.");
        if (fallan > 0) {
            System.exit(1);
        }
    }

    // ================================================================== árbol

    private static void pruebasArbol() {
        titulo("ÁRBOL: catálogo de solicitudes");
        SistemaAtencion s = DatosPrueba.crear();

        verificar("A1 El catálogo inicial tiene 26 elementos", 26, s.totalElementos());
        verificar("A1 Altura del catálogo", 3, s.altura());

        s.registrarElemento("REC", "REC-DIG", "Reclamos por canales digitales", null, 0);
        s.registrarElemento("REC-DIG", "REC-DIG-01", "Chat sin respuesta", "CDIG", 12);
        verificar("A2 Nueva subcategoría queda en el nivel 2", 2, s.nivel("REC-DIG"));
        verificar("A2 Nuevo tipo de atención queda en el nivel 3", 3, s.nivel("REC-DIG-01"));
        verificar("A2 Padre del nuevo tipo de atención", "REC-DIG", s.padre("REC-DIG-01").getCodigo());

        verificar("A3 Buscar existente SOP-INT-01", true, s.buscarElemento("SOP-INT-01") != null);
        verificar("A3 Buscar inexistente XYZ-99", true, s.buscarElemento("XYZ-99") == null);

        verificar("A4 Hijos de SOP", List.of("SOP-INT", "SOP-EQU", "SOP-APP"), codigos(s.hijos("SOP")));

        verificar("A5 Eliminar SOP (tiene hijos) se impide", true,
                lanza(() -> s.eliminarElemento("SOP")));
        s.eliminarElemento("VEN-PLA-01");
        verificar("A5 Eliminar la hoja VEN-PLA-01", true, s.buscarElemento("VEN-PLA-01") == null);
        verificar("A5 Eliminar la raíz se impide", true, lanza(() -> s.eliminarElemento("CAC")));

        SistemaAtencion r = DatosPrueba.crear();
        verificar("A6 Preorden empieza por la primera rama completa",
                List.of("CAC", "FAC", "FAC-COB", "FAC-COB-01", "FAC-COB-02"), codigos(r.preorden()).subList(0, 5));
        List<String> post = codigos(r.postorden());
        verificar("A6 Postorden termina en la raíz", "CAC", post.get(post.size() - 1));
        verificar("A6 Postorden empieza en la primera hoja", "FAC-COB-01", post.get(0));
        List<Integer> tamanos = new ArrayList<>();
        for (List<ElementoCatalogo> nivel : r.porNiveles()) {
            tamanos.add(nivel.size());
        }
        verificar("A6 Por niveles: elementos en cada nivel", List.of(1, 4, 9, 12), tamanos);

        verificar("A7 Nivel de CAC (raíz)", 0, r.nivel("CAC"));
        verificar("A7 Nivel de FAC-PAG", 2, r.nivel("FAC-PAG"));
        verificar("A7 Altura del subárbol SOP", 2, r.alturaSubarbol("SOP"));
        verificar("A7 Altura de una hoja", 0, r.alturaSubarbol("FAC-COB-01"));

        r.reubicarElemento("SOP-EQU-01", "SOP-INT");
        verificar("A8 Reubicar SOP-EQU-01 bajo SOP-INT", "SOP-INT", r.padre("SOP-EQU-01").getCodigo());
        verificar("A8 Reubicar una categoría bajo una subcategoría se impide", true,
                lanza(() -> r.reubicarElemento("VEN", "SOP-INT")));

        r.modificarElemento("FAC-COB", "Cobros y cargos", null, null);
        verificar("A9 Modificar nombre conserva padre", "FAC", r.padre("FAC-COB").getCodigo());
        verificar("A9 Modificar nombre conserva hijos", 2, r.hijos("FAC-COB").size());
    }

    // ================================================================== grafo

    private static void pruebasGrafo() {
        titulo("GRAFO: red de escalamiento");
        SistemaAtencion s = DatosPrueba.crear();

        verificar("G1 Áreas y escalamientos iniciales", "15/20", s.totalAreas() + "/" + s.totalEscalamientos());
        List<String> vecinos = new ArrayList<>();
        for (Edge<Area> e : s.escalamientosDe("MDA")) {
            vecinos.add(e.getDestination().getCodigo());
        }
        verificar("G2 Vecinos de MDA", List.of("ST2", "FACT", "VENT", "CAL"), vecinos);
        verificar("G2 Grado de salida / entrada de CAL", "2/3", s.gradoSalida("CAL") + "/" + s.gradoEntrada("CAL"));

        verificar("G3 Conexión directa MDA → CAL existe", true, s.conexionDirecta("MDA", "CAL") != null);
        verificar("G3 Conexión directa CAL → MDA no existe (dirigido)", true, s.conexionDirecta("CAL", "MDA") == null);

        List<String> bfsPlano = new ArrayList<>();
        for (List<Area> nivel : s.bfs("MDA")) {
            bfsPlano.addAll(codigosAreas(nivel));
        }
        List<String> dfs = codigosAreas(s.dfs("MDA"));
        verificar("G4 BFS desde MDA",
                List.of("MDA", "ST2", "FACT", "VENT", "CAL", "ST3", "LOG", "COBR", "SEGF", "RETE", "GER", "JUR", "DEF"),
                bfsPlano);
        verificar("G4 DFS desde MDA",
                List.of("MDA", "ST2", "ST3", "GER", "DEF", "LOG", "CAL", "JUR", "FACT", "COBR", "SEGF", "VENT", "RETE"),
                dfs);
        verificar("G4 BFS y DFS visitan las mismas áreas en distinto orden", true,
                !bfsPlano.equals(dfs) && bfsPlano.containsAll(dfs) && dfs.containsAll(bfsPlano));

        Ruta<Area> saltos = s.rutaMenosTransferencias("MDA", "DEF");
        Ruta<Area> rapida = s.rutaMasRapida("MDA", "DEF");
        verificar("G5 Menos transferencias MDA → DEF", "MDA → CAL → GER → DEF (100)",
                saltos + " (" + (long) saltos.costo() + ")");
        verificar("G5 Más rápida MDA → DEF (Dijkstra)", "MDA → VENT → RETE → CAL → GER → DEF (83)",
                rapida + " (" + (long) rapida.costo() + ")");
        verificar("G5 Sin ruta desde el área aislada AUD", false, s.rutaMasRapida("AUD", "DEF").existe());

        verificar("G6 Ciclo detectado", "[[FACT, COBR, FACT]]", s.transferenciasCirculares().toString());
        s.eliminarEscalamiento("COBR", "FACT");
        verificar("G6 Sin ciclos tras eliminar COBR → FACT", 0, s.transferenciasCirculares().size());

        s.modificarTiempoEscalamiento("RETE", "CAL", 40);
        verificar("G7 Cambiar RETE → CAL a 40 min cambia la ruta más rápida",
                "MDA → CAL → GER → DEF (100)",
                s.rutaMasRapida("MDA", "DEF") + " (" + (long) s.rutaMasRapida("MDA", "DEF").costo() + ")");

        verificar("G8 Eliminar FACT se impide (es responsable de tipos de atención)", true,
                lanza(() -> s.eliminarArea("FACT")));
        verificar("G8 Eliminar LOG borra sus 2 escalamientos", 2, s.eliminarArea("LOG"));
        verificar("G8 ST2 ya no escala a LOG", 1, s.gradoSalida("ST2"));

        verificar("G9 Escalamiento duplicado se rechaza", true, lanza(() -> s.crearEscalamiento("MDA", "CAL", 5)));
        s.registrarArea("TEL", "Telefonía");
        s.crearEscalamiento("TEL", "MDA", 7);
        verificar("G9 TEL → MDA no crea MDA → TEL", true, s.conexionDirecta("MDA", "TEL") == null);
    }

    // ================================================================== integración

    private static void pruebasIntegracion() {
        titulo("INTEGRACIÓN árbol + grafo");
        SistemaAtencion s = DatosPrueba.crear();
        Ruta<Area> ruta = s.escalarSolicitud("SOP-INT-02", "DEF");
        verificar("I1 SOP-INT-02 inicia en MDA y llega a DEF en 83 min",
                "MDA → VENT → RETE → CAL → GER → DEF (83)", ruta + " (" + (long) ruta.costo() + ")");
        verificar("I2 Escalar una categoría (no es tipo de atención) se impide", true,
                lanza(() -> s.escalarSolicitud("FAC", "DEF")));
        verificar("I3 Registrar tipo de atención con área inexistente se impide", true,
                lanza(() -> s.registrarElemento("SOP-INT", "SOP-INT-09", "Prueba", "XXX", 4)));
        verificar("I4 Tipos atendidos por FACT", List.of("FAC-COB-01", "FAC-PAG-01"),
                codigos(s.tiposAtendidosPor("FACT")));
    }

    // ================================================================== utilidades

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("== " + texto + " ==");
    }

    private static void verificar(String nombre, Object esperado, Object obtenido) {
        boolean ok = Objects.equals(esperado, obtenido);
        if (ok) {
            pasan++;
        } else {
            fallan++;
        }
        System.out.println((ok ? "[PASA]  " : "[FALLA] ") + nombre + "  →  " + obtenido
                + (ok ? "" : "   (esperado: " + esperado + ")"));
    }

    private static boolean lanza(Runnable accion) {
        try {
            accion.run();
            return false;
        } catch (AtencionException e) {
            return true;
        }
    }

    private static List<String> codigos(List<ElementoCatalogo> lista) {
        List<String> r = new ArrayList<>();
        for (ElementoCatalogo e : lista) {
            r.add(e.getCodigo());
        }
        return r;
    }

    private static List<String> codigosAreas(List<Area> lista) {
        List<String> r = new ArrayList<>();
        for (Area a : lista) {
            r.add(a.getCodigo());
        }
        return r;
    }
}
