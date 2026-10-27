package ec.udla.intercampus.servicios;

import ec.udla.intercampus.estructuras.Grafo;
import ec.udla.intercampus.modelo.Bus;
import ec.udla.intercampus.modelo.Linea;
import ec.udla.intercampus.modelo.Parada;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Red completa: grafo de tramos, paradas indexadas por código (búsqueda O(1)),
 * líneas y buses.
 */
public class RedIntercampus {

    private final Grafo grafo = new Grafo();
    private final Map<String, Parada> paradas = new LinkedHashMap<>();   // conserva el orden de registro
    private final List<Linea> lineas = new ArrayList<>();
    private final Map<String, Bus> buses = new LinkedHashMap<>();

    public Parada agregarParada(String codigo, String nombre, boolean esCampus) {
        Parada p = new Parada(codigo, nombre, esCampus);
        paradas.put(codigo, p);
        grafo.agregarVertice(codigo);
        return p;
    }

    public void agregarTramo(String a, String b, int minutos) { grafo.agregarTramoDoble(a, b, minutos); }

    public Linea agregarLinea(String codigo, String nombre, String... codigosParadas) {
        List<Parada> recorrido = new ArrayList<>();
        for (String c : codigosParadas) recorrido.add(parada(c));
        Linea l = new Linea(codigo, nombre, recorrido, grafo);
        lineas.add(l);
        return l;
    }

    public Bus agregarBus(String id, Linea linea, int capacidad, String paradaInicial, double detenido) {
        Bus b = new Bus(id, linea, capacidad, paradaInicial, detenido);
        buses.put(id, b);
        return b;
    }

    public Parada parada(String codigo) {
        Parada p = paradas.get(codigo == null ? null : codigo.toUpperCase());
        if (p == null) throw new IllegalArgumentException("No existe la parada " + codigo);
        return p;
    }

    public boolean existeParada(String codigo) { return codigo != null && paradas.containsKey(codigo.toUpperCase()); }
    public Grafo getGrafo() { return grafo; }
    public Collection<Parada> getParadas() { return paradas.values(); }
    public List<Linea> getLineas() { return lineas; }
    public Collection<Bus> getBuses() { return buses.values(); }

    /**
     * Red de ejemplo para el prototipo. Los nombres de campus son de la UDLA;
     * las paradas intermedias, tiempos y capacidades son ILUSTRATIVOS
     * y deben reemplazarse por los datos reales del servicio.
     */
    public static RedIntercampus crearRedEjemplo() {
        RedIntercampus red = new RedIntercampus();
        red.agregarParada("GRA", "Campus Granados", true);
        red.agregarParada("PRK", "Campus UDLAPark", true);
        red.agregarParada("COL", "Campus Colón", true);
        red.agregarParada("P1", "Parada intermedia 1", false);
        red.agregarParada("P2", "Parada intermedia 2", false);
        red.agregarParada("P3", "Parada intermedia 3", false);
        red.agregarParada("P4", "Parada intermedia 4", false);

        red.agregarTramo("GRA", "P1", 5);
        red.agregarTramo("P1", "P2", 6);
        red.agregarTramo("P2", "PRK", 4);
        red.agregarTramo("GRA", "P3", 7);
        red.agregarTramo("P3", "COL", 8);
        red.agregarTramo("P2", "P4", 5);
        red.agregarTramo("P4", "COL", 9);

        Linea a = red.agregarLinea("L1", "Granados ↔ UDLAPark", "GRA", "P1", "P2", "PRK", "P2", "P1");
        Linea b = red.agregarLinea("L2", "Granados ↔ Colón", "GRA", "P3", "COL", "P3");
        Linea c = red.agregarLinea("L3", "UDLAPark ↔ Colón", "PRK", "P2", "P4", "COL", "P4", "P2");

        red.agregarBus("B1", a, 18, "GRA", 2);
        red.agregarBus("B2", a, 18, "PRK", 1);
        red.agregarBus("B3", b, 18, "COL", 3);
        red.agregarBus("B4", c, 18, "PRK", 4);
        return red;
    }
}
