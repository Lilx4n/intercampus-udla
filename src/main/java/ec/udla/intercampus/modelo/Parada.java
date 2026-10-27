package ec.udla.intercampus.modelo;

import ec.udla.intercampus.estructuras.ColaFIFO;

/**
 * Parada de la red. Cada parada mantiene su propia fila (FIFO) de estudiantes.
 * Un campus es una parada terminal con tiempo de detención mayor.
 */
public class Parada {

    private final String codigo;
    private final String nombre;
    private final boolean esCampus;
    private final ColaFIFO<Estudiante> fila = new ColaFIFO<>();

    public Parada(String codigo, String nombre, boolean esCampus) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.esCampus = esCampus;
    }

    /** Minutos que el bus se detiene: más en campus (suben y bajan más personas). */
    public int minutosDetencion() { return esCampus ? 3 : 1; }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public boolean esCampus() { return esCampus; }
    public ColaFIFO<Estudiante> getFila() { return fila; }

    @Override public String toString() { return nombre + " (" + codigo + ")"; }
}
