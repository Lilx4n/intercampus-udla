package ec.udla.intercampus.algoritmos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ordenamiento por mezcla, genérico con Comparator.
 * Se eligió sobre QuickSort porque es estable (conserva el orden horario
 * de los registros empatados) y su peor caso es O(n log n).
 * Memoria adicional: O(n).
 */
public final class MergeSort {

    private MergeSort() { }

    /** Devuelve una lista nueva ordenada; no modifica la original. */
    public static <T> List<T> ordenar(List<T> lista, Comparator<T> cmp) {
        if (lista.size() <= 1) return new ArrayList<>(lista);
        int medio = lista.size() / 2;
        List<T> izq = ordenar(lista.subList(0, medio), cmp);
        List<T> der = ordenar(lista.subList(medio, lista.size()), cmp);
        return mezclar(izq, der, cmp);
    }

    private static <T> List<T> mezclar(List<T> a, List<T> b, Comparator<T> cmp) {
        List<T> resultado = new ArrayList<>(a.size() + b.size());
        int i = 0, j = 0;
        while (i < a.size() && j < b.size()) {
            // "<=" mantiene la estabilidad: ante empate gana el de la izquierda.
            if (cmp.compare(a.get(i), b.get(j)) <= 0) resultado.add(a.get(i++));
            else resultado.add(b.get(j++));
        }
        while (i < a.size()) resultado.add(a.get(i++));
        while (j < b.size()) resultado.add(b.get(j++));
        return resultado;
    }
}
