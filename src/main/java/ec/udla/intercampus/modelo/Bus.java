package ec.udla.intercampus.modelo;

import ec.udla.intercampus.estructuras.BufferCircular;
import ec.udla.intercampus.estructuras.Grafo;
import ec.udla.intercampus.estructuras.ListaCircularDoble.Nodo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Bus que recorre una línea. Su posición es un nodo de la lista circular:
 * o está detenido en ese nodo, o va en camino hacia el siguiente.
 */
public class Bus {

    private static final int MEDICIONES_TRAFICO = 5;

    private final String id;
    private final Linea linea;
    private final int capacidad;
    private final List<Estudiante> pasajeros = new ArrayList<>();
    private final BufferCircular historialTrafico = new BufferCircular(MEDICIONES_TRAFICO);

    private Nodo<Parada> actual;          // última parada alcanzada
    private boolean enParada = true;
    private double minutosDetenido;       // restantes de detención (si enParada)
    private double minutosAlSiguiente;    // restantes de viaje (si no enParada)
    private double planTramoActual;       // minutos planificados del tramo en curso
    private double realTramoActual;       // minutos reales del tramo en curso

    public Bus(String id, Linea linea, int capacidad, String paradaInicial, double minutosDetenidoInicial) {
        this.id = id;
        this.linea = linea;
        this.capacidad = capacidad;
        this.actual = linea.getRecorrido().buscar(p -> p.getCodigo().equals(paradaInicial));
        if (actual == null) throw new IllegalArgumentException("La línea no pasa por " + paradaInicial);
        this.minutosDetenido = minutosDetenidoInicial;
    }

    /**
     * Avanza el bus un minuto.
     * @param factorTrafico multiplicador del tiempo del tramo si el bus sale ahora
     * @return true si en este minuto llegó a una parada (para que suban y bajen pasajeros)
     */
    public boolean avanzarUnMinuto(Grafo grafo, double factorTrafico) {
        if (enParada) {
            minutosDetenido -= 1;
            if (minutosDetenido <= 0) {                         // sale hacia la siguiente
                Parada destino = actual.getSiguiente().getDato();
                planTramoActual = grafo.minutosTramo(actual.getDato().getCodigo(), destino.getCodigo());
                realTramoActual = planTramoActual * factorTrafico;
                minutosAlSiguiente = realTramoActual;
                enParada = false;
            }
            return false;
        }
        minutosAlSiguiente -= 1;
        if (minutosAlSiguiente <= 0) {                          // llegó
            historialTrafico.agregar(realTramoActual / planTramoActual);
            actual = actual.getSiguiente();
            enParada = true;
            minutosDetenido = actual.getDato().minutosDetencion();
            return true;
        }
        return false;
    }

    /**
     * Minutos estimados hasta que el bus esté en la parada indicada.
     * Recorre la lista circular desde la posición actual (máximo una vuelta)
     * y escala los tramos futuros con el promedio móvil del tráfico medido.
     * @return 0 si ya está detenido ahí; -1 si la línea no pasa por la parada.
     */
    public double minutosHasta(String codigoParada, Grafo grafo) {
        double factor = historialTrafico.promedio(1.0);
        Nodo<Parada> nodo;
        double t;
        if (enParada) {
            if (actual.getDato().getCodigo().equals(codigoParada)) return 0;
            t = minutosDetenido;
            nodo = actual;
        } else {
            t = minutosAlSiguiente;
            nodo = actual.getSiguiente();
            if (nodo.getDato().getCodigo().equals(codigoParada)) return t;
            t += nodo.getDato().minutosDetencion();
        }
        int pasos = linea.getRecorrido().tamanio();
        for (int i = 0; i < pasos; i++) {
            Nodo<Parada> sig = nodo.getSiguiente();
            t += grafo.minutosTramo(nodo.getDato().getCodigo(), sig.getDato().getCodigo()) * factor;
            if (sig.getDato().getCodigo().equals(codigoParada)) return t;
            t += sig.getDato().minutosDetencion();
            nodo = sig;
        }
        return -1;
    }

    /** Baja a los pasajeros cuyo destino es la parada actual. Devuelve cuántos bajaron. */
    public int bajarPasajeros() {
        int bajaron = 0;
        String aqui = actual.getDato().getCodigo();
        for (Iterator<Estudiante> it = pasajeros.iterator(); it.hasNext(); ) {
            if (it.next().destino().equals(aqui)) { it.remove(); bajaron++; }
        }
        return bajaron;
    }

    public boolean subir(Estudiante e) {
        if (pasajeros.size() >= capacidad) return false;
        pasajeros.add(e);
        return true;
    }

    public String getId() { return id; }
    public Linea getLinea() { return linea; }
    public int getCapacidad() { return capacidad; }
    public int getOcupacion() { return pasajeros.size(); }
    public boolean hayCupo() { return pasajeros.size() < capacidad; }
    public boolean isEnParada() { return enParada; }
    public double getMinutosDetenido() { return minutosDetenido; }
    public Parada getParadaActual() { return actual.getDato(); }
    public Parada getProximaParada() { return actual.getSiguiente().getDato(); }
    public double getMinutosAlSiguiente() { return minutosAlSiguiente; }
    public double factorTraficoPromedio() { return historialTrafico.promedio(1.0); }

    /** Nivel de ocupación para mostrar al estudiante. */
    public String nivelOcupacion() {
        double r = (double) pasajeros.size() / capacidad;
        if (r >= 1.0) return "Lleno";
        if (r >= 0.75) return "Casi lleno";
        return "Hay cupo";
    }
}
