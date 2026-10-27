# Intercampus en vivo — UDLA

Proyecto de **ISWZ2102 Programación III** (Ingeniería de Software, UDLA).
Prototipo en Java del motor de una app que informa a los estudiantes **cuándo llega el bus Intercampus** a su parada, al estilo de la app Transit.

> Estado: **Progreso 1**. Diseño de la solución con estructuras de datos y algoritmos implementados y probados en consola. La red de paradas, los tiempos y las capacidades son **datos de ejemplo**; deben reemplazarse por los del servicio real.

## Problema

Los estudiantes esperan el Intercampus sin saber cuándo llegará el bus, si ya pasó o si tendrá cupo. Hoy preguntan al guardia o al conductor, escriben en grupos de WhatsApp o terminan pagando un taxi. El sistema calcula en tiempo real cuántos minutos faltan, avisa antes de que llegue el bus y registra dónde falta capacidad.

## Funcionalidades (más allá de un CRUD)

| Funcionalidad | Operación algorítmica |
|---|---|
| Próximas llegadas a una parada | Recorrido de lista circular + cola de prioridad (top-k) |
| Ruta más rápida entre dos paradas | Dijkstra sobre grafo ponderado |
| "Avísame cuando falten N minutos" | Cola de eventos (min-heap) con reprogramación |
| Abordaje y cupo | Cola FIFO por parada, rotación para destinos no cubiertos |
| Estimación con tráfico | Promedio móvil en buffer circular |
| Reporte de paradas críticas | Filtrado por franja + agregación (HashMap) + MergeSort |

## Estructuras de datos

Las estructuras del paquete `estructuras` están **implementadas a mano** (no se usan las de `java.util` para ellas).

| Estructura | Clase | Uso en el dominio | Operaciones clave |
|---|---|---|---|
| Grafo ponderado (lista de adyacencia) | `Grafo` | Paradas y tramos con minutos | vecinos O(grado) · memoria O(V+E) |
| Lista circular doblemente enlazada | `ListaCircularDoble` | Recorrido cíclico de cada línea | siguiente O(1) · buscar O(n) |
| Montículo de mínimos | `MinHeap` | Próximas llegadas, alertas, frontera de Dijkstra | insertar / extraer O(log n) |
| Cola FIFO enlazada | `ColaFIFO` | Fila de estudiantes en cada parada | encolar / desencolar O(1) |
| Buffer circular | `BufferCircular` | Últimas 5 mediciones de tráfico por bus | agregar / promedio O(1) |
| Tabla hash (`java.util.HashMap`) | `RedIntercampus` | Buscar parada o bus por código | O(1) promedio |

## Alternativas evaluadas y selección

| Decisión | Alternativas | Seleccionada | Justificación |
|---|---|---|---|
| Representar la red | Matriz de adyacencia O(V²) · Lista de adyacencia O(V+E) | **Lista de adyacencia** | La red es dispersa: cada parada conecta con 2 o 3 vecinas |
| Ruta más rápida | BFS · Dijkstra · Floyd-Warshall | **Dijkstra con heap** O((V+E) log V) | BFS ignora los pesos; Floyd-Warshall es O(V³) y hay que recalcularlo cuando cambia el tráfico |
| Próximas llegadas | Lista ordenada (inserción O(n)) · Árbol AVL · Min-heap | **Min-heap** | Solo interesa el mínimo y las ETA se recalculan a cada minuto |
| Recorrido de la línea | Arreglo con índice módulo n · Lista circular | **Lista circular doble** | El ciclo es natural, sin casos borde; permite recorrer hacia atrás en el Progreso 2 |
| Ordenar reportes | QuickSort · MergeSort | **MergeSort** | Estable y O(n log n) garantizado en el peor caso |

## Estructura del proyecto

```
src/main/java/ec/udla/intercampus/
├── estructuras/   Grafo, ListaCircularDoble, MinHeap, ColaFIFO, BufferCircular
├── algoritmos/    Dijkstra, MergeSort
├── modelo/        Parada, Linea, Bus, Estudiante, Llegada, Alerta, RegistroAbordaje
├── servicios/     RedIntercampus, ServicioEta, ServicioAlertas, ServicioReportes, Simulador
└── app/           Main (menú de consola y modo --demo)
src/test/java/ec/udla/intercampus/
└── PruebasBasicas (pruebas sin dependencias)
```

## Cómo ejecutarlo

Requiere **JDK 17 o superior**.

```bash
# compilar
javac -encoding UTF-8 -d out $(find src -name "*.java")

# demo guiada (sin teclado), ideal para la presentación
java -cp out ec.udla.intercampus.app.Main --demo

# menú interactivo
java -cp out ec.udla.intercampus.app.Main

# pruebas
java -cp out ec.udla.intercampus.PruebasBasicas
```

En Windows (PowerShell), para compilar:

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse -Filter *.java src).FullName
```

En IntelliJ IDEA o NetBeans: abrir la carpeta como proyecto Maven (incluye `pom.xml`) y ejecutar `Main`.

## Red de ejemplo

- Campus: Granados (GRA), UDLAPark (PRK), Colón (COL). [Verificar qué campus cubre el Intercampus real].
- Paradas intermedias P1–P4, tramos de 4 a 9 minutos, buses de 18 plazas (ilustrativo).
- Líneas: L1 Granados ↔ UDLAPark, L2 Granados ↔ Colón, L3 UDLAPark ↔ Colón.
- Hora pico 07:00–08:00 y 12:30–13:30 multiplica el tiempo de viaje ×1.3 con ruido aleatorio (semilla fija para que la demo sea reproducible).

## Limitaciones conocidas (pendientes para el Progreso 2)

- Dijkstra calcula tiempo de viaje, no espera ni transbordos entre líneas.
- La posición del bus es simulada; en producción llegaría del GPS del celular del conductor.
- Los estudiantes solo viajan a campus alcanzables sin transbordo.
- Persistencia de datos e interfaz gráfica o móvil.

## Integrantes

- [COMPLETAR]
