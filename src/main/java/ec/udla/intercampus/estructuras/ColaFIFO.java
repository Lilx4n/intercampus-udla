package ec.udla.intercampus.estructuras;

/**
 * Cola FIFO enlazada simple. Modela la fila de estudiantes en una parada:
 * el primero en llegar es el primero en subir.
 *
 * Complejidad: encolar O(1), desencolar O(1).
 */
public class ColaFIFO<T> {

    private static class Nodo<T> {
        final T dato;
        Nodo<T> siguiente;
        Nodo(T dato) { this.dato = dato; }
    }

    private Nodo<T> frente, fin;
    private int tamanio;

    public void encolar(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (fin == null) frente = nuevo;
        else fin.siguiente = nuevo;
        fin = nuevo;
        tamanio++;
    }

    public T desencolar() {
        if (frente == null) throw new IllegalStateException("La cola está vacía");
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) fin = null;
        tamanio--;
        return dato;
    }

    public T verFrente() {
        if (frente == null) throw new IllegalStateException("La cola está vacía");
        return frente.dato;
    }

    public boolean estaVacia() { return tamanio == 0; }
    public int tamanio() { return tamanio; }
}
