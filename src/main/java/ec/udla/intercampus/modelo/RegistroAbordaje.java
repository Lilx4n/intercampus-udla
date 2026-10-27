package ec.udla.intercampus.modelo;

/** Lo que pasó cuando un bus se detuvo en una parada: quiénes subieron y quiénes se quedaron sin cupo. */
public record RegistroAbordaje(String codigoParada, String idBus, int minuto, int subieron, int sinCupo) { }
