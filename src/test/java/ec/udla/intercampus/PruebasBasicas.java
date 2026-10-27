package ec.udla.intercampus;

import ec.udla.intercampus.algoritmos.Dijkstra;
import ec.udla.intercampus.algoritmos.MergeSort;
import ec.udla.intercampus.estructuras.BufferCircular;
import ec.udla.intercampus.estructuras.ColaFIFO;
import ec.udla.intercampus.estructuras.Grafo;
import ec.udla.intercampus.estructuras.ListaCircularDoble;
import ec.udla.intercampus.estructuras.MinHeap;
import ec.udla.intercampus.servicios.RedIntercampus;
import ec.udla.intercampus.servicios.ServicioEta;

import java.util.Comparator;
import java.util.List;

/**
 * Pruebas sin dependencias externas (no requieren JUnit).
 * Ejecutar: java -cp out ec.udla.intercampus.PruebasBasicas
 * En el Progreso 2 se pueden migrar a JUnit 5.
 */
public class PruebasBasicas {

    private static int ok = 0, fallas = 0;

    public static void main(String[] args) {
        minHeapOrdena();
        colaRespetaOrden();
        listaCircularDaLaVuelta();
        bufferSobrescribeElMasAntiguo();
        dijkstraEligeElCaminoCorto();
        mergeSortEsEstable();
        etaEsCeroSiElBusEstaEnLaParada();
        System.out.printf("%nResultado: %d correctas, %d fallidas%n", ok, fallas);
        if (fallas > 0) System.exit(1);
    }

    static void minHeapOrdena() {
        MinHeap<Integer> h = new MinHeap<>();
        for (int x : new int[]{9, 3, 7, 1, 8, 2, 30, 5, 4, 6, 11, 12, 13, 14, 15, 16, 17}) h.insertar(x);
        StringBuilder sb = new StringBuilder();
        while (!h.estaVacio()) sb.append(h.extraerMin()).append(',');
        verificar("MinHeap extrae en orden", sb.toString().equals("1,2,3,4,5,6,7,8,9,11,12,13,14,15,16,17,30,"));
    }

    static void colaRespetaOrden() {
        ColaFIFO<String> c = new ColaFIFO<>();
        c.encolar("a"); c.encolar("b"); c.encolar("c");
        verificar("ColaFIFO respeta el orden de llegada",
                c.desencolar().equals("a") && c.desencolar().equals("b") && c.tamanio() == 1);
    }

    static void listaCircularDaLaVuelta() {
        ListaCircularDoble<String> l = new ListaCircularDoble<>();
        l.agregar("GRA"); l.agregar("P1"); l.agregar("PRK");
        ListaCircularDoble.Nodo<String> n = l.buscar(s -> s.equals("PRK"));
        verificar("Lista circular: después de la última vuelve a la primera",
                n.getSiguiente().getDato().equals("GRA") && l.getCabeza().getAnterior().getDato().equals("PRK"));
    }

    static void bufferSobrescribeElMasAntiguo() {
        BufferCircular b = new BufferCircular(3);
        b.agregar(1); b.agregar(2); b.agregar(3); b.agregar(10);   // sale el 1
        verificar("Buffer circular promedia las últimas 3", Math.abs(b.promedio(0) - 5.0) < 1e-9);
    }

    static void dijkstraEligeElCaminoCorto() {
        Grafo g = new Grafo();
        for (String v : new String[]{"A", "B", "C", "D"}) g.agregarVertice(v);
        g.agregarTramoDoble("A", "B", 1);
        g.agregarTramoDoble("B", "D", 1);
        g.agregarTramoDoble("A", "C", 1);
        g.agregarTramoDoble("C", "D", 5);
        Dijkstra.Resultado r = Dijkstra.rutaMasRapida(g, "A", "D", 1.0);
        verificar("Dijkstra encuentra A→B→D (2 min)", r.camino().equals(List.of("A", "B", "D")) && r.minutosTotales() == 2);

        RedIntercampus red = RedIntercampus.crearRedEjemplo();
        Dijkstra.Resultado r2 = Dijkstra.rutaMasRapida(red.getGrafo(), "PRK", "COL", 1.0);
        verificar("Red de ejemplo: PRK→COL por P2 y P4 (18 min)", r2.minutosTotales() == 18);
    }

    static void mergeSortEsEstable() {
        record R(String nombre, int valor) { }
        List<R> datos = List.of(new R("a", 2), new R("b", 1), new R("c", 2), new R("d", 0));
        List<R> ord = MergeSort.ordenar(datos, Comparator.comparingInt(R::valor));
        verificar("MergeSort ordena y es estable (a antes que c)",
                ord.get(0).nombre().equals("d") && ord.get(2).nombre().equals("a") && ord.get(3).nombre().equals("c"));
    }

    static void etaEsCeroSiElBusEstaEnLaParada() {
        RedIntercampus red = RedIntercampus.crearRedEjemplo();
        ServicioEta eta = new ServicioEta(red);
        verificar("ETA = 0 si el bus está detenido en la parada", eta.proxima("GRA").minutos() == 0);
    }

    private static void verificar(String nombre, boolean condicion) {
        System.out.println((condicion ? "  [OK]    " : "  [FALLA] ") + nombre);
        if (condicion) ok++; else fallas++;
    }
}
