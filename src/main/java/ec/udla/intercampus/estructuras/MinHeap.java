package ec.udla.intercampus.estructuras;

import java.util.Arrays;

/**
 * Montículo binario de mínimos (cola de prioridad) sobre un arreglo dinámico.
 * Se usa para: próximas llegadas a una parada, cola de eventos de alertas
 * y la frontera de Dijkstra.
 *
 * Complejidad: insertar O(log n), extraerMin O(log n), verMin O(1).
 * Hijos del índice i: 2i+1 y 2i+2; padre: (i-1)/2.
 */
public class MinHeap<T extends Comparable<T>> {

    private Object[] datos = new Object[16];
    private int tamanio;

    public void insertar(T elemento) {
        if (tamanio == datos.length) datos = Arrays.copyOf(datos, tamanio * 2);
        datos[tamanio] = elemento;
        subir(tamanio);
        tamanio++;
    }

    public T extraerMin() {
        if (tamanio == 0) throw new IllegalStateException("El montículo está vacío");
        T min = get(0);
        tamanio--;
        datos[0] = datos[tamanio];
        datos[tamanio] = null;          // evita retener referencias (fuga de memoria)
        if (tamanio > 0) bajar(0);
        return min;
    }

    public T verMin() {
        if (tamanio == 0) throw new IllegalStateException("El montículo está vacío");
        return get(0);
    }

    public boolean estaVacio() { return tamanio == 0; }
    public int tamanio() { return tamanio; }

    // Reubica hacia arriba mientras el elemento sea menor que su padre.
    private void subir(int i) {
        while (i > 0) {
            int padre = (i - 1) / 2;
            if (get(i).compareTo(get(padre)) >= 0) break;
            intercambiar(i, padre);
            i = padre;
        }
    }

    // Reubica hacia abajo intercambiando con el hijo menor.
    private void bajar(int i) {
        while (true) {
            int izq = 2 * i + 1, der = izq + 1, menor = i;
            if (izq < tamanio && get(izq).compareTo(get(menor)) < 0) menor = izq;
            if (der < tamanio && get(der).compareTo(get(menor)) < 0) menor = der;
            if (menor == i) return;
            intercambiar(i, menor);
            i = menor;
        }
    }

    @SuppressWarnings("unchecked")
    private T get(int i) { return (T) datos[i]; }

    private void intercambiar(int a, int b) {
        Object tmp = datos[a];
        datos[a] = datos[b];
        datos[b] = tmp;
    }
}
