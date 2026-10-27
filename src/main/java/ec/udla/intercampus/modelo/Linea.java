package ec.udla.intercampus.modelo;

import ec.udla.intercampus.estructuras.Grafo;
import ec.udla.intercampus.estructuras.ListaCircularDoble;

import java.util.List;

/**
 * Línea del Intercampus: secuencia cíclica de paradas (ida y vuelta).
 * Al crearla se valida que cada par consecutivo, incluido el cierre
 * última→primera, sea un tramo existente en el grafo.
 */
public class Linea {

    private final String codigo;
    private final String nombre;
    private final ListaCircularDoble<Parada> recorrido = new ListaCircularDoble<>();

    public Linea(String codigo, String nombre, List<Parada> paradas, Grafo grafo) {
        if (paradas.size() < 2) throw new IllegalArgumentException("Una línea necesita al menos 2 paradas");
        for (int i = 0; i < paradas.size(); i++) {
            Parada a = paradas.get(i), b = paradas.get((i + 1) % paradas.size());
            if (grafo.minutosTramo(a.getCodigo(), b.getCodigo()) < 0)
                throw new IllegalArgumentException("La línea " + codigo + " usa un tramo inexistente: "
                        + a.getCodigo() + "→" + b.getCodigo());
            recorrido.agregar(a);
        }
        this.codigo = codigo;
        this.nombre = nombre;
    }

    /** true si la línea pasa por la parada indicada. */
    public boolean pasaPor(String codigoParada) {
        return recorrido.contiene(p -> p.getCodigo().equals(codigoParada));
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public ListaCircularDoble<Parada> getRecorrido() { return recorrido; }

    @Override public String toString() { return codigo + " · " + nombre; }
}
