# Sistema de Atención al Cliente — Árbol y Grafo (Caso 25)

Actividad Unidad 4 — Protocolo colaborativo — Estructuras de Datos — Universidad de Cartagena.

Aplicación de consola que organiza las solicitudes de un centro de atención al cliente en un
**catálogo jerárquico** (árbol) y modela el **escalamiento o transferencia** de solicitudes entre
las áreas responsables (grafo). Las dos estructuras están unidas: cada tipo de atención del árbol
indica el área (vértice del grafo) donde empieza su atención.

- **Java:** 17 o superior (probado con OpenJDK 21).
- **Estructuras propias:** `Tree`/`TreeNode` (árbol general) y `Graph`/`Edge` (lista de adyacencia).
  Las colecciones del JDK solo se usan como apoyo (`ArrayList` para los hijos, `LinkedHashMap` para
  las adyacencias, `ArrayDeque` como cola, `PriorityQueue` en Dijkstra).

## Las dos estructuras

| Parte del problema | Estructura | Por qué |
|---|---|---|
| Centro → Categoría → Subcategoría → Tipo de atención | **Árbol general** (n-ario) | Cada solicitud pertenece a una sola categoría; una categoría tiene cualquier número de subcategorías |
| Escalamiento/transferencia entre áreas | **Grafo dirigido y ponderado** | Un área escala a varias y recibe de varias; el sentido importa (Nivel 1 → Nivel 2, no al revés) y el peso son los minutos de la transferencia |

**Política de eliminación del árbol:** no se elimina un elemento con hijos; primero se eliminan o
reubican sus descendientes. Un área del grafo no se elimina si es responsable de algún tipo de atención.

## Algoritmos

- Árbol: inserción bajo un padre, búsqueda recursiva, nivel (subiendo por el padre), altura
  (recursiva), recorridos **preorden**, **postorden** y **por niveles** (cola), reubicación de subárboles.
- Grafo: **BFS** (cola, por niveles), **DFS** (recursivo), ruta con **menos transferencias** (BFS),
  ruta **más rápida** (**Dijkstra**) y **detección de transferencias circulares** (DFS con estados).

## Compilar y ejecutar

```bash
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out atencion.app.Main              # menú con datos de prueba
java -cp out atencion.pruebas.PruebasSistema # 46 pruebas automáticas
```

En Windows (PowerShell):

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out atencion.app.Main
```

`java -cp out atencion.app.Main --vacio` inicia sin datos de prueba.

Datos de prueba: 26 elementos en el catálogo (altura 3) y 15 áreas con 20 escalamientos,
incluido un ciclo (FACT → COBR → FACT) y un área aislada (AUD).

## Estructura

```
src/atencion/
├── modelo/     ElementoCatalogo, TipoElemento, Area, Identificable, AtencionException
├── arbol/      TreeNode, Tree            (árbol general)
├── grafo/      Edge, Graph, Ruta         (grafo con lista de adyacencia)
├── servicio/   SistemaAtencion (integra árbol y grafo), DatosPrueba
├── app/        Main, MenuConsola         (solo interacción con el usuario)
└── pruebas/    PruebasSistema            (pruebas automáticas)
```
