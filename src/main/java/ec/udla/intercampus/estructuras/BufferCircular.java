package ec.udla.intercampus.estructuras;

/**
 * Buffer circular de tamaño fijo para las últimas N mediciones de un bus
 * (factor de tráfico = minutos reales / minutos planificados de cada tramo).
 * Cuando se llena, el dato más antiguo se sobrescribe.
 *
 * Complejidad: agregar O(1), promedio O(1) (se mantiene la suma acumulada).
 */
public class BufferCircular {

    private final double[] datos;
    private int inicio;     // índice del dato más antiguo
    private int tamanio;
    private double suma;

    public BufferCircular(int capacidad) {
        if (capacidad <= 0) throw new IllegalArgumentException("Capacidad inválida");
        datos = new double[capacidad];
    }

    public void agregar(double valor) {
        if (tamanio < datos.length) {
            datos[(inicio + tamanio) % datos.length] = valor;
            tamanio++;
        } else {
            suma -= datos[inicio];          // sale el más antiguo
            datos[inicio] = valor;
            inicio = (inicio + 1) % datos.length;
        }
        suma += valor;
    }

    /** Promedio móvil; devuelve valorPorDefecto si aún no hay datos. */
    public double promedio(double valorPorDefecto) {
        return tamanio == 0 ? valorPorDefecto : suma / tamanio;
    }

    public int tamanio() { return tamanio; }
}
