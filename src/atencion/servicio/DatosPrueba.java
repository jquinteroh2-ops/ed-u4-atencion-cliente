package atencion.servicio;

/**
 * Datos de prueba: 25 elementos en el catálogo (4 niveles, altura 3)
 * y 15 áreas con 20 escalamientos en la red.
 *
 * La red incluye a propósito:
 *  - un ciclo FACT → COBR → FACT (transferencia circular entre Facturación y Cobranzas);
 *  - un área aislada (AUD) sin conexiones;
 *  - una ruta corta en saltos pero lenta (MDA → CAL directo, 50 min) y otra con más
 *    saltos pero más rápida (MDA → VENT → RETE → CAL, 33 min), para comparar BFS y Dijkstra.
 */
public final class DatosPrueba {

    private DatosPrueba() {
    }

    public static SistemaAtencion crear() {
        SistemaAtencion s = new SistemaAtencion("CAC", "Centro de Atención al Cliente");
        cargarRed(s);
        cargarCatalogo(s);
        return s;
    }

    /** Primero la red, porque cada tipo de atención necesita que su área ya exista. */
    private static void cargarRed(SistemaAtencion s) {
        s.registrarArea("CDIG", "Canales Digitales (chat y app)");
        s.registrarArea("MDA", "Mesa de Ayuda - Nivel 1");
        s.registrarArea("ST2", "Soporte Técnico - Nivel 2");
        s.registrarArea("ST3", "Ingeniería de Red - Nivel 3");
        s.registrarArea("LOG", "Logística y Envíos");
        s.registrarArea("FACT", "Facturación");
        s.registrarArea("COBR", "Cobranzas");
        s.registrarArea("SEGF", "Seguridad y Fraude");
        s.registrarArea("VENT", "Ventas");
        s.registrarArea("RETE", "Retención de Clientes");
        s.registrarArea("CAL", "Calidad y Reclamos");
        s.registrarArea("JUR", "Jurídica");
        s.registrarArea("GER", "Gerencia de Servicio");
        s.registrarArea("DEF", "Defensoría del Cliente");
        s.registrarArea("AUD", "Auditoría Interna");

        // origen → destino (minutos que tarda la transferencia)
        s.crearEscalamiento("CDIG", "MDA", 5);
        s.crearEscalamiento("CDIG", "VENT", 6);
        s.crearEscalamiento("MDA", "ST2", 15);
        s.crearEscalamiento("MDA", "FACT", 10);
        s.crearEscalamiento("MDA", "VENT", 8);
        s.crearEscalamiento("MDA", "CAL", 50);
        s.crearEscalamiento("ST2", "ST3", 30);
        s.crearEscalamiento("ST2", "LOG", 25);
        s.crearEscalamiento("ST3", "GER", 60);
        s.crearEscalamiento("LOG", "CAL", 20);
        s.crearEscalamiento("FACT", "COBR", 12);
        s.crearEscalamiento("FACT", "SEGF", 18);
        s.crearEscalamiento("COBR", "FACT", 12);
        s.crearEscalamiento("SEGF", "JUR", 40);
        s.crearEscalamiento("VENT", "RETE", 10);
        s.crearEscalamiento("RETE", "CAL", 15);
        s.crearEscalamiento("CAL", "GER", 30);
        s.crearEscalamiento("CAL", "JUR", 35);
        s.crearEscalamiento("JUR", "DEF", 45);
        s.crearEscalamiento("GER", "DEF", 20);
    }

    private static void cargarCatalogo(SistemaAtencion s) {
        // Nivel 1: categorías
        s.registrarElemento("CAC", "FAC", "Facturación y pagos", null, 0);
        s.registrarElemento("CAC", "SOP", "Soporte técnico", null, 0);
        s.registrarElemento("CAC", "VEN", "Ventas y planes", null, 0);
        s.registrarElemento("CAC", "REC", "Reclamos y PQR", null, 0);

        // Nivel 2: subcategorías
        s.registrarElemento("FAC", "FAC-COB", "Cobros", null, 0);
        s.registrarElemento("FAC", "FAC-PAG", "Pagos", null, 0);
        s.registrarElemento("SOP", "SOP-INT", "Internet y conectividad", null, 0);
        s.registrarElemento("SOP", "SOP-EQU", "Equipos", null, 0);
        s.registrarElemento("SOP", "SOP-APP", "Aplicación móvil", null, 0);
        s.registrarElemento("VEN", "VEN-PLA", "Cambio de plan", null, 0);
        s.registrarElemento("VEN", "VEN-CAN", "Cancelación del servicio", null, 0);
        s.registrarElemento("REC", "REC-QUE", "Quejas por atención", null, 0);
        s.registrarElemento("REC", "REC-LEG", "Reclamos formales", null, 0);

        // Nivel 3: tipos de atención (hojas) con su área responsable en la red
        s.registrarElemento("FAC-COB", "FAC-COB-01", "Cobro duplicado", "FACT", 24);
        s.registrarElemento("FAC-COB", "FAC-COB-02", "Cargo no reconocido", "SEGF", 12);
        s.registrarElemento("FAC-PAG", "FAC-PAG-01", "Pago no aplicado", "FACT", 48);
        s.registrarElemento("FAC-PAG", "FAC-PAG-02", "Acuerdo de pago", "COBR", 72);
        s.registrarElemento("SOP-INT", "SOP-INT-01", "Sin servicio de internet", "MDA", 4);
        s.registrarElemento("SOP-INT", "SOP-INT-02", "Internet lento o intermitente", "MDA", 24);
        s.registrarElemento("SOP-EQU", "SOP-EQU-01", "Cambio de módem dañado", "ST2", 48);
        s.registrarElemento("SOP-APP", "SOP-APP-01", "No puede ingresar a la app", "CDIG", 8);
        s.registrarElemento("VEN-PLA", "VEN-PLA-01", "Mejorar el plan contratado", "VENT", 24);
        s.registrarElemento("VEN-CAN", "VEN-CAN-01", "Solicitud de cancelación", "RETE", 72);
        s.registrarElemento("REC-QUE", "REC-QUE-01", "Mala atención de un asesor", "CAL", 72);
        s.registrarElemento("REC-LEG", "REC-LEG-01", "Recurso de reposición", "JUR", 360);
    }
}
