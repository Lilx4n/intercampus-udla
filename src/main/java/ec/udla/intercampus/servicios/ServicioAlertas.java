package ec.udla.intercampus.servicios;

import ec.udla.intercampus.estructuras.MinHeap;
import ec.udla.intercampus.modelo.Alerta;
import ec.udla.intercampus.modelo.Llegada;

import java.util.ArrayList;
import java.util.List;

/**
 * Alertas "avísame a N minutos". Cola de eventos con MinHeap ordenado por
 * el minuto de revisión: en cada minuto solo se procesan las alertas vencidas
 * (las del tope), no todas. Como la ETA cambia con el tráfico, una alerta
 * que todavía no corresponde se reprograma y vuelve al montículo.
 */
public class ServicioAlertas {

    private final ServicioEta eta;
    private final MinHeap<Alerta> pendientes = new MinHeap<>();
    private int siguienteId = 1;

    public ServicioAlertas(ServicioEta eta) { this.eta = eta; }

    /** Programa la alerta y devuelve su id. */
    public int programar(String codigoParada, int minutosAntes, double reloj) {
        if (minutosAntes <= 0) throw new IllegalArgumentException("Los minutos deben ser positivos");
        Llegada prox = eta.proxima(codigoParada);
        if (prox == null) throw new IllegalArgumentException("Ningún bus pasa por " + codigoParada);
        double revision = reloj + Math.max(0, prox.minutos() - minutosAntes);
        Alerta a = new Alerta(siguienteId++, prox.codigoParada(), minutosAntes, revision);
        pendientes.insertar(a);
        return a.getId();
    }

    /** Procesa las alertas vencidas y devuelve los mensajes de las que se disparan. */
    public List<String> procesar(double reloj) {
        List<String> mensajes = new ArrayList<>();
        List<Alerta> reprogramar = new ArrayList<>();
        while (!pendientes.estaVacio() && pendientes.verMin().getMinutoRevision() <= reloj) {
            Alerta a = pendientes.extraerMin();
            Llegada prox = eta.proxima(a.getCodigoParada());
            if (prox != null && prox.minutos() > 0 && prox.minutos() <= a.getMinutosAntes()) {
                mensajes.add(String.format("ALERTA #%d: el bus %s llega a %s en %d min. ¡Sal ahora!",
                        a.getId(), prox.bus().getId(), a.getCodigoParada(), (int) Math.ceil(prox.minutos())));
            } else {
                // Aún no corresponde (o el bus ya está ahí y salió): se revisa más tarde.
                double faltan = prox == null ? 1 : Math.max(1, prox.minutos() - a.getMinutosAntes());
                a.reprogramar(reloj + faltan);
                reprogramar.add(a);
            }
        }
        reprogramar.forEach(pendientes::insertar);
        return mensajes;
    }

    public int pendientes() { return pendientes.tamanio(); }
}
