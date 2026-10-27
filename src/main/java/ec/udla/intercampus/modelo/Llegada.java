package ec.udla.intercampus.modelo;

/** Llegada estimada de un bus a una parada; se ordena por minutos (para el MinHeap). */
public record Llegada(Bus bus, String codigoParada, double minutos) implements Comparable<Llegada> {
    @Override public int compareTo(Llegada o) { return Double.compare(minutos, o.minutos); }

    /** true si el bus ya está detenido en la parada. */
    public boolean estaEnParada() { return minutos == 0; }
}
