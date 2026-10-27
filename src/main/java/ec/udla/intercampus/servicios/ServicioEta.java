package ec.udla.intercampus.servicios;

import ec.udla.intercampus.estructuras.MinHeap;
import ec.udla.intercampus.modelo.Bus;
import ec.udla.intercampus.modelo.Llegada;

import java.util.ArrayList;
import java.util.List;

/**
 * Tiempo estimado de llegada (ETA) de los buses a una parada.
 * Calcula la ETA de cada bus que pasa por la parada, los mete en un MinHeap
 * y extrae los k menores: O(B·L + k log B), con B buses y L paradas por línea.
 */
public class ServicioEta {

    private final RedIntercampus red;

    public ServicioEta(RedIntercampus red) { this.red = red; }

    public List<Llegada> proximasLlegadas(String codigoParada, int k) {
        String codigo = red.parada(codigoParada).getCodigo();
        MinHeap<Llegada> heap = new MinHeap<>();
        for (Bus bus : red.getBuses()) {
            double minutos = bus.minutosHasta(codigo, red.getGrafo());
            if (minutos >= 0) heap.insertar(new Llegada(bus, codigo, minutos));
        }
        List<Llegada> resultado = new ArrayList<>();
        while (!heap.estaVacio() && resultado.size() < k) resultado.add(heap.extraerMin());
        return resultado;
    }

    /** La llegada más próxima a la parada, o null si ningún bus pasa por ahí. */
    public Llegada proxima(String codigoParada) {
        List<Llegada> l = proximasLlegadas(codigoParada, 1);
        return l.isEmpty() ? null : l.get(0);
    }
}
