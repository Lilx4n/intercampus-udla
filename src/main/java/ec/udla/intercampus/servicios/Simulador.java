package ec.udla.intercampus.servicios;

import ec.udla.intercampus.estructuras.ColaFIFO;
import ec.udla.intercampus.modelo.Bus;
import ec.udla.intercampus.modelo.Estudiante;
import ec.udla.intercampus.modelo.Linea;
import ec.udla.intercampus.modelo.Parada;
import ec.udla.intercampus.modelo.RegistroAbordaje;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simulación por pasos de un minuto. En el sistema real, la posición del bus
 * llegaría por GPS del celular del conductor; aquí se genera con un modelo
 * de tráfico aleatorio (semilla fija para que la demo sea reproducible).
 */
public class Simulador {

    public static final int HORA_INICIO = 7 * 60;   // el reloj 0 corresponde a las 07:00

    private final RedIntercampus red;
    private final ServicioEta eta;
    private final ServicioAlertas alertas;
    private final ServicioReportes reportes;
    private final Random azar;
    private int reloj;              // minutos desde las 07:00
    private int siguienteEstudiante = 1;

    public Simulador(RedIntercampus red, long semilla) {
        this.red = red;
        this.eta = new ServicioEta(red);
        this.alertas = new ServicioAlertas(eta);
        this.reportes = new ServicioReportes();
        this.azar = new Random(semilla);
    }

    /** Avanza n minutos y devuelve los eventos relevantes (alertas y personas sin cupo). */
    public List<String> avanzar(int minutos) {
        List<String> eventos = new ArrayList<>();
        for (int i = 0; i < minutos; i++) {
            reloj++;
            generarEstudiantes();
            for (Bus bus : red.getBuses()) {
                if (bus.avanzarUnMinuto(red.getGrafo(), factorTrafico())) {
                    String evento = atenderParada(bus);
                    if (evento != null) eventos.add(evento);
                }
            }
            for (String m : alertas.procesar(reloj)) eventos.add(formatear(reloj) + "  " + m);
        }
        return eventos;
    }

    /** Hora pico (07:00–08:00 y 12:30–13:30) multiplica el tiempo de viaje. */
    private double factorTrafico() {
        int hora = HORA_INICIO + reloj;
        boolean pico = (hora >= 7 * 60 && hora < 8 * 60) || (hora >= 12 * 60 + 30 && hora < 13 * 60 + 30);
        double base = pico ? 1.3 : 1.0;
        return base * (0.85 + azar.nextDouble() * 0.4);      // ruido entre 0.85 y 1.25
    }

    /** Llegan estudiantes a las paradas; más a los campus y más en hora pico. */
    private void generarEstudiantes() {
        int hora = HORA_INICIO + reloj;
        boolean pico = hora < 8 * 60 + 15 || (hora >= 12 * 60 + 15 && hora < 13 * 60 + 15);
        for (Parada p : red.getParadas()) {
            // Probabilidad de que llegue un estudiante en este minuto (valores ilustrativos).
            double prob = p.esCampus() ? (pico ? 0.9 : 0.4) : (pico ? 0.3 : 0.1);
            int llegan = azar.nextDouble() < prob ? 1 : 0;
            List<String> destinos = destinosAlcanzables(p.getCodigo());
            for (int k = 0; k < llegan && !destinos.isEmpty(); k++) {
                String destino = destinos.get(azar.nextInt(destinos.size()));
                p.getFila().encolar(new Estudiante(siguienteEstudiante++, p.getCodigo(), destino, reloj));
            }
        }
    }

    /** Campus a los que se puede ir sin transbordo desde la parada. */
    private List<String> destinosAlcanzables(String origen) {
        List<String> r = new ArrayList<>();
        for (Parada p : red.getParadas()) {
            if (!p.esCampus() || p.getCodigo().equals(origen)) continue;
            for (Linea l : red.getLineas()) {
                if (l.pasaPor(origen) && l.pasaPor(p.getCodigo())) { r.add(p.getCodigo()); break; }
            }
        }
        return r;
    }

    /**
     * El bus llega a una parada: bajan los que llegaron a su destino y sube la
     * fila en orden FIFO. Quien va a un destino que esta línea no cubre vuelve
     * al final de la fila (rotación: se recorre la cola una vez, O(n)).
     */
    private String atenderParada(Bus bus) {
        bus.bajarPasajeros();
        Parada parada = bus.getParadaActual();
        ColaFIFO<Estudiante> fila = parada.getFila();
        int subieron = 0, sinCupo = 0, n = fila.tamanio();
        for (int i = 0; i < n; i++) {
            Estudiante e = fila.desencolar();
            if (!bus.getLinea().pasaPor(e.destino())) { fila.encolar(e); continue; }
            if (bus.subir(e)) subieron++;
            else { sinCupo++; fila.encolar(e); }
        }
        reportes.registrar(new RegistroAbordaje(parada.getCodigo(), bus.getId(), reloj, subieron, sinCupo));
        if (sinCupo > 0)
            return String.format("%s  %s se llenó en %s: %d estudiante(s) se quedaron esperando",
                    formatear(reloj), bus.getId(), parada.getNombre(), sinCupo);
        return null;
    }

    public static String formatear(double minutoSimulado) {
        int total = (int) Math.floor(HORA_INICIO + minutoSimulado) % (24 * 60);
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    /** Convierte "HH:MM" a minutos de simulación. */
    public static int aMinutoSimulado(String hhmm) {
        String[] p = hhmm.trim().split(":");
        if (p.length != 2) throw new IllegalArgumentException("Formato esperado HH:MM");
        return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]) - HORA_INICIO;
    }

    public int getReloj() { return reloj; }
    public RedIntercampus getRed() { return red; }
    public ServicioEta getEta() { return eta; }
    public ServicioAlertas getAlertas() { return alertas; }
    public ServicioReportes getReportes() { return reportes; }
}
