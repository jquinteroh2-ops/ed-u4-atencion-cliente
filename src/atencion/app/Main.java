package atencion.app;

import atencion.servicio.DatosPrueba;
import atencion.servicio.SistemaAtencion;

/** Punto de entrada: crea el sistema (con o sin datos de prueba) y abre el menú. */
public class Main {
    public static void main(String[] args) {
        SistemaAtencion sistema;
        if (args.length > 0 && args[0].equalsIgnoreCase("--vacio")) {
            sistema = new SistemaAtencion("CAC", "Centro de Atención al Cliente");
        } else {
            sistema = DatosPrueba.crear();
            System.out.println("Datos de prueba cargados: " + sistema.totalElementos() + " elementos en el catálogo, "
                    + sistema.totalAreas() + " áreas y " + sistema.totalEscalamientos() + " escalamientos.");
        }
        new MenuConsola(sistema).iniciar();
    }
}
