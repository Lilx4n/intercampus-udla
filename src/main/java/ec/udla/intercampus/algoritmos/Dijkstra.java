package ec.udla.intercampus.algoritmos;

import ec.udla.intercampus.estructuras.Grafo;
import ec.udla.intercampus.estructuras.MinHeap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Camino de menor tiempo entre dos paradas (Dijkstra con montículo).
 * Complejidad: O((V + E) log V). Válido porque todos los pesos son positivos.
 *
 * Variante "lazy": en vez de disminuir la prioridad dentro del montículo,
 * se inserta una entrada nueva y las obsoletas se descartan al extraerlas.
 */
public final class Dijkstra {

    private Dijkstra() { }

    /** Resultado: paradas en orden y minutos totales. camino vacío = no hay ruta. */
    public record Resultado(List<String> camino, int minutosTotales) {
        public boolean existe() { return !camino.isEmpty(); }
    }

    /** Entrada del montículo: parada con su distancia tentativa. */
    private record Entrada(String parada, int distancia) implements Comparable<Entrada> {
        @Override public int compareTo(Entrada o) { return Integer.compare(distancia, o.distancia); }
    }

    /**
     * @param factorPeso multiplica cada tramo (1.0 = tiempos planificados;
     *                   >1 para simular tráfico general en la red)
     */
    public static Resultado rutaMasRapida(Grafo grafo, String origen, String destino, double factorPeso) {
        if (!grafo.existeVertice(origen) || !grafo.existeVertice(destino))
            throw new IllegalArgumentException("Origen o destino inexistente");

        Map<String, Integer> distancia = new HashMap<>();
        Map<String, String> previo = new HashMap<>();
        MinHeap<Entrada> frontera = new MinHeap<>();

        for (String v : grafo.vertices()) distancia.put(v, Integer.MAX_VALUE);
        distancia.put(origen, 0);
        frontera.insertar(new Entrada(origen, 0));

        while (!frontera.estaVacio()) {
            Entrada actual = frontera.extraerMin();
            if (actual.distancia() > distancia.get(actual.parada())) continue; // entrada obsoleta
            if (actual.parada().equals(destino)) break;                         // ya es óptima

            for (Grafo.Arista arista : grafo.vecinos(actual.parada())) {
                int peso = (int) Math.round(arista.minutos() * factorPeso);
                int candidata = actual.distancia() + peso;
                if (candidata < distancia.get(arista.destino())) {             // relajación
                    distancia.put(arista.destino(), candidata);
                    previo.put(arista.destino(), actual.parada());
                    frontera.insertar(new Entrada(arista.destino(), candidata));
                }
            }
        }

        if (distancia.get(destino) == Integer.MAX_VALUE) return new Resultado(List.of(), -1);

        // Reconstrucción del camino desde el destino hacia atrás.
        List<String> camino = new ArrayList<>();
        for (String p = destino; p != null; p = previo.get(p)) camino.add(p);
        Collections.reverse(camino);
        return new Resultado(camino, distancia.get(destino));
    }
}
