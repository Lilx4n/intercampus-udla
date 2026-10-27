package ec.udla.intercampus.estructuras;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Grafo dirigido ponderado con lista de adyacencia.
 * Vértices = paradas (por código); aristas = tramos con su tiempo en minutos.
 *
 * Se eligió lista de adyacencia y no matriz porque la red es dispersa:
 * cada parada conecta con 2 o 3 vecinas. Memoria O(V + E) frente a O(V²).
 */
public class Grafo {

    /** Arista hacia un vecino con su peso (minutos planificados). */
    public record Arista(String destino, int minutos) { }

    private final Map<String, List<Arista>> adyacencia = new HashMap<>();

    public void agregarVertice(String codigo) {
        adyacencia.putIfAbsent(codigo, new ArrayList<>());
    }

    /** Agrega un tramo en ambos sentidos (las calles de la ruta son de doble vía). */
    public void agregarTramoDoble(String a, String b, int minutos) {
        agregarTramo(a, b, minutos);
        agregarTramo(b, a, minutos);
    }

    public void agregarTramo(String origen, String destino, int minutos) {
        if (!adyacencia.containsKey(origen) || !adyacencia.containsKey(destino))
            throw new IllegalArgumentException("Parada inexistente: " + origen + " o " + destino);
        if (minutos <= 0) throw new IllegalArgumentException("El tiempo debe ser positivo");
        adyacencia.get(origen).add(new Arista(destino, minutos));
    }

    public List<Arista> vecinos(String codigo) {
        List<Arista> lista = adyacencia.get(codigo);
        if (lista == null) throw new IllegalArgumentException("Parada inexistente: " + codigo);
        return lista;
    }

    /** Minutos del tramo directo origen→destino, o -1 si no existe. */
    public int minutosTramo(String origen, String destino) {
        for (Arista a : vecinos(origen)) if (a.destino().equals(destino)) return a.minutos();
        return -1;
    }

    public boolean existeVertice(String codigo) { return adyacencia.containsKey(codigo); }
    public Collection<String> vertices() { return adyacencia.keySet(); }
    public int cantidadVertices() { return adyacencia.size(); }
}
