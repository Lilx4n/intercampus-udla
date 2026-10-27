package ec.udla.intercampus.modelo;

/**
 * Alerta "avísame cuando el bus esté a N minutos de mi parada".
 * Se ordena por el minuto en que conviene revisarla (cola de eventos).
 */
public class Alerta implements Comparable<Alerta> {

    private final int id;
    private final String codigoParada;
    private final int minutosAntes;
    private double minutoRevision;

    public Alerta(int id, String codigoParada, int minutosAntes, double minutoRevision) {
        this.id = id;
        this.codigoParada = codigoParada;
        this.minutosAntes = minutosAntes;
        this.minutoRevision = minutoRevision;
    }

    @Override public int compareTo(Alerta o) { return Double.compare(minutoRevision, o.minutoRevision); }

    public int getId() { return id; }
    public String getCodigoParada() { return codigoParada; }
    public int getMinutosAntes() { return minutosAntes; }
    public double getMinutoRevision() { return minutoRevision; }
    public void reprogramar(double minuto) { this.minutoRevision = minuto; }
}
