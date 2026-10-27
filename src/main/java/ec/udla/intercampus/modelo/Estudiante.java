package ec.udla.intercampus.modelo;

/** Estudiante que espera el Intercampus en una parada para ir a otra. */
public record Estudiante(int id, String origen, String destino, int minutoLlegada) { }
