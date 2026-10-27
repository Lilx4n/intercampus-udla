package ec.udla.intercampus.app;

import ec.udla.intercampus.algoritmos.Dijkstra;
import ec.udla.intercampus.modelo.Bus;
import ec.udla.intercampus.modelo.Linea;
import ec.udla.intercampus.modelo.Llegada;
import ec.udla.intercampus.modelo.Parada;
import ec.udla.intercampus.servicios.RedIntercampus;
import ec.udla.intercampus.servicios.ServicioReportes.ResumenParada;
import ec.udla.intercampus.servicios.Simulador;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Intercampus en vivo — prototipo de consola.
 * Uso:  java -cp out ec.udla.intercampus.app.Main          (menú interactivo)
 *       java -cp out ec.udla.intercampus.app.Main --demo   (recorrido guiado sin teclado)
 */
public class Main {

    private static final long SEMILLA = 2026L;

    public static void main(String[] args) {
        // Fuerza UTF-8 para que tildes y flechas se vean bien en cualquier consola.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        Simulador sim = new Simulador(RedIntercampus.crearRedEjemplo(), SEMILLA);
        if (args.length > 0 && args[0].equals("--demo")) demo(sim);
        else menu(sim);
    }

    // ---------------------------------------------------------------- menú
    private static void menu(Simulador sim) {
        Scanner in = new Scanner(System.in);
        imprimirRed(sim.getRed());
        while (true) {
            System.out.println();
            System.out.println("=== Intercampus en vivo · " + Simulador.formatear(sim.getReloj()) + " ===");
            System.out.println("1. Próximas llegadas a una parada");
            System.out.println("2. Ruta más rápida entre dos paradas");
            System.out.println("3. Avísame cuando el bus esté a N minutos");
            System.out.println("4. Avanzar la simulación N minutos");
            System.out.println("5. Estado de los buses");
            System.out.println("6. Reporte: paradas con más estudiantes sin cupo");
            System.out.println("7. Ver la red (paradas y líneas)");
            System.out.println("0. Salir");
            String op = leer(in, "Opción: ");
            try {
                switch (op) {
                    case "1" -> mostrarLlegadas(sim, leer(in, "Código de parada (ej. GRA): "));
                    case "2" -> mostrarRuta(sim, leer(in, "Origen: "), leer(in, "Destino: "));
                    case "3" -> {
                        String p = leer(in, "Código de parada: ");
                        int n = Integer.parseInt(leer(in, "¿Cuántos minutos antes?: "));
                        int id = sim.getAlertas().programar(p.toUpperCase(), n, sim.getReloj());
                        System.out.println("Alerta #" + id + " programada. Avanza la simulación para verla dispararse.");
                    }
                    case "4" -> avanzar(sim, Integer.parseInt(leer(in, "Minutos a avanzar: ")));
                    case "5" -> mostrarBuses(sim);
                    case "6" -> mostrarReporte(sim, leer(in, "Desde (HH:MM): "), leer(in, "Hasta (HH:MM): "));
                    case "7" -> imprimirRed(sim.getRed());
                    case "0" -> { System.out.println("Hasta luego."); return; }
                    default -> System.out.println("Opción no válida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un número válido.");
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static String leer(Scanner in, String msg) {
        System.out.print(msg);
        if (!in.hasNextLine()) System.exit(0);       // fin de entrada (ej. stdin redirigido)
        return in.nextLine().trim();
    }

    // ---------------------------------------------------------------- demo guiada
    private static void demo(Simulador sim) {
        imprimirRed(sim.getRed());
        titulo("1) Estudiante en Granados a las 07:00: ¿cuándo pasa el bus?");
        mostrarLlegadas(sim, "GRA");

        titulo("2) Ruta más rápida de UDLAPark a Colón (Dijkstra)");
        mostrarRuta(sim, "PRK", "COL");

        titulo("3) Alerta: avísame cuando el bus esté a 5 min de Granados");
        int id = sim.getAlertas().programar("GRA", 5, sim.getReloj());
        System.out.println("Alerta #" + id + " programada.");

        titulo("4) Pasan 45 minutos de hora pico");
        avanzar(sim, 45);

        titulo("5) Estado de los buses a las " + Simulador.formatear(sim.getReloj()));
        mostrarBuses(sim);

        titulo("6) Reporte 07:00–07:45: dónde faltó capacidad (MergeSort)");
        mostrarReporte(sim, "07:00", "07:45");
    }

    private static void titulo(String t) { System.out.println("\n--- " + t + " ---"); }

    // ---------------------------------------------------------------- vistas
    private static void mostrarLlegadas(Simulador sim, String codigo) {
        Parada p = sim.getRed().parada(codigo);
        List<Llegada> llegadas = sim.getEta().proximasLlegadas(p.getCodigo(), 3);
        if (llegadas.isEmpty()) { System.out.println("Ningún bus pasa por " + p.getNombre()); return; }
        System.out.println("Próximas llegadas a " + p.getNombre() + ":");
        for (Llegada l : llegadas) {
            Bus b = l.bus();
            String cuando = l.estaEnParada()
                    ? "está en la parada, sale en " + (int) Math.ceil(b.getMinutosDetenido()) + " min"
                    : String.format("%d min (%s)", (int) Math.ceil(l.minutos()),
                            Simulador.formatear(sim.getReloj() + l.minutos()));
            System.out.printf("  %-3s %-22s %-38s %s (%d/%d)%n", b.getId(), b.getLinea().getNombre(),
                    cuando, b.nivelOcupacion(), b.getOcupacion(), b.getCapacidad());
        }
        System.out.println("  Estudiantes esperando en la parada: " + p.getFila().tamanio());
    }

    private static void mostrarRuta(Simulador sim, String origen, String destino) {
        RedIntercampus red = sim.getRed();
        Parada o = red.parada(origen), d = red.parada(destino);
        Dijkstra.Resultado r = Dijkstra.rutaMasRapida(red.getGrafo(), o.getCodigo(), d.getCodigo(), 1.0);
        if (!r.existe()) { System.out.println("No hay ruta entre esas paradas."); return; }
        StringBuilder sb = new StringBuilder();
        for (String c : r.camino()) sb.append(sb.isEmpty() ? "" : " → ").append(c);
        System.out.println(sb + "   (" + r.minutosTotales() + " min de viaje, sin contar esperas)");
    }

    private static void avanzar(Simulador sim, int minutos) {
        if (minutos <= 0 || minutos > 600) throw new IllegalArgumentException("Usa entre 1 y 600 minutos");
        List<String> eventos = sim.avanzar(minutos);
        if (eventos.isEmpty()) System.out.println("Sin eventos destacados.");
        else eventos.forEach(e -> System.out.println("  " + e));
        System.out.println("Hora actual: " + Simulador.formatear(sim.getReloj()));
    }

    private static void mostrarBuses(Simulador sim) {
        for (Bus b : sim.getRed().getBuses()) {
            String pos = b.isEnParada()
                    ? "detenido en " + b.getParadaActual().getCodigo()
                    : String.format("%s → %s (%d min)", b.getParadaActual().getCodigo(),
                            b.getProximaParada().getCodigo(), (int) Math.ceil(b.getMinutosAlSiguiente()));
            System.out.printf("  %-3s %-22s %-24s %2d/%d  tráfico x%.2f%n", b.getId(), b.getLinea().getNombre(),
                    pos, b.getOcupacion(), b.getCapacidad(), b.factorTraficoPromedio());
        }
    }

    private static void mostrarReporte(Simulador sim, String desde, String hasta) {
        List<ResumenParada> lista = sim.getReportes().paradasCriticas(
                Simulador.aMinutoSimulado(desde), Simulador.aMinutoSimulado(hasta));
        if (lista.isEmpty()) { System.out.println("Sin registros en esa franja."); return; }
        System.out.printf("  %-6s %9s %9s %12s%n", "Parada", "Subieron", "Sin cupo*", "Detenciones");
        for (ResumenParada r : lista)
            System.out.printf("  %-6s %9d %9d %12d%n", r.codigoParada(), r.subieron(), r.sinCupo(), r.detenciones());
        System.out.println("  * Veces que un estudiante no pudo subir (uno mismo puede contarse en varios buses).");
    }

    private static void imprimirRed(RedIntercampus red) {
        System.out.println("Red Intercampus (datos de ejemplo):");
        for (Parada p : red.getParadas()) System.out.println("  " + p);
        for (Linea l : red.getLineas()) System.out.println("  Línea " + l);
    }
}
