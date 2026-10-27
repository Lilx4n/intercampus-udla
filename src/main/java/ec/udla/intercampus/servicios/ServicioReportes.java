package ec.udla.intercampus.servicios;

import ec.udla.intercampus.algoritmos.MergeSort;
import ec.udla.intercampus.modelo.RegistroAbordaje;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reportes de demanda para quien administra el servicio:
 * filtrado por franja horaria, agregación por parada (HashMap) y
 * ordenamiento con MergeSort para priorizar dónde falta capacidad.
 */
public class ServicioReportes {

    /** Resumen de una parada en la franja consultada. */
    public record ResumenParada(String codigoParada, int subieron, int sinCupo, int detenciones) { }

    private final List<RegistroAbordaje> registros = new ArrayList<>();

    public void registrar(RegistroAbordaje r) { registros.add(r); }

    /** Filtra registros con minuto en [desde, hasta]. O(n). */
    public List<RegistroAbordaje> filtrarPorFranja(int desde, int hasta) {
        List<RegistroAbordaje> r = new ArrayList<>();
        for (RegistroAbordaje x : registros) if (x.minuto() >= desde && x.minuto() <= hasta) r.add(x);
        return r;
    }

    /**
     * Paradas ordenadas de mayor a menor número de estudiantes sin cupo
     * (desempate: más estudiantes que subieron). Agregación O(n) + MergeSort O(p log p).
     */
    public List<ResumenParada> paradasCriticas(int desde, int hasta) {
        Map<String, int[]> acumulado = new LinkedHashMap<>();   // [subieron, sinCupo, detenciones]
        for (RegistroAbordaje r : filtrarPorFranja(desde, hasta)) {
            int[] a = acumulado.computeIfAbsent(r.codigoParada(), k -> new int[3]);
            a[0] += r.subieron();
            a[1] += r.sinCupo();
            a[2]++;
        }
        List<ResumenParada> lista = new ArrayList<>();
        acumulado.forEach((k, v) -> lista.add(new ResumenParada(k, v[0], v[1], v[2])));
        Comparator<ResumenParada> cmp = Comparator.comparingInt(ResumenParada::sinCupo).reversed()
                .thenComparing(Comparator.comparingInt(ResumenParada::subieron).reversed());
        return MergeSort.ordenar(lista, cmp);
    }

    public int totalRegistros() { return registros.size(); }
}
