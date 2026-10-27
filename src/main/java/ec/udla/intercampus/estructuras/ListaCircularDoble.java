package ec.udla.intercampus.estructuras;

import java.util.function.Predicate;

/**
 * Lista circular doblemente enlazada.
 * Modela el recorrido cíclico de una línea de bus: después de la última parada
 * vuelve a la primera, sin casos especiales en los extremos.
 *
 * Complejidad: agregar O(1) (se mantiene referencia a la cabeza y su anterior es la cola),
 * buscar O(n), avanzar al siguiente O(1).
 */
public class ListaCircularDoble<T> {

    /** Nodo público para que un bus pueda "estar" en un nodo y avanzar con siguiente(). */
    public static class Nodo<T> {
        private final T dato;
        private Nodo<T> siguiente;
        private Nodo<T> anterior;

        Nodo(T dato) { this.dato = dato; }

        public T getDato() { return dato; }
        public Nodo<T> getSiguiente() { return siguiente; }
        public Nodo<T> getAnterior() { return anterior; }
    }

    private Nodo<T> cabeza;
    private int tamanio;

    /** Inserta al final del ciclo (antes de la cabeza). */
    public void agregar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (cabeza == null) {
            nuevo.siguiente = nuevo;
            nuevo.anterior = nuevo;
            cabeza = nuevo;
        } else {
            Nodo<T> cola = cabeza.anterior;
            cola.siguiente = nuevo;
            nuevo.anterior = cola;
            nuevo.siguiente = cabeza;
            cabeza.anterior = nuevo;
        }
        tamanio++;
    }

    /** Primer nodo que cumple la condición, recorriendo una sola vuelta. */
    public Nodo<T> buscar(Predicate<T> condicion) {
        if (cabeza == null) return null;
        Nodo<T> actual = cabeza;
        for (int i = 0; i < tamanio; i++) {
            if (condicion.test(actual.dato)) return actual;
            actual = actual.siguiente;
        }
        return null;
    }

    /** true si algún elemento cumple la condición. */
    public boolean contiene(Predicate<T> condicion) { return buscar(condicion) != null; }

    public Nodo<T> getCabeza() { return cabeza; }
    public int tamanio() { return tamanio; }
    public boolean estaVacia() { return tamanio == 0; }
}
