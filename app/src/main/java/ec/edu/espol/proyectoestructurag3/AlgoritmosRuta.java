package ec.edu.espol.proyectoestructurag3;

import java.util.*;

public class AlgoritmosRuta {

    /**
     * Algoritmo de Dijkstra
     * Retorna la distancia mínima desde un aeropuerto origen a todos los demás.
     */
    public static Map<Aeropuerto, Double> dijkstra(Grafo grafo, Aeropuerto origen, Map<Aeropuerto, Aeropuerto> predecesor) {
        Map<Aeropuerto, Double> distancias = new HashMap<>();
        predecesor.clear(); // limpiar predecesores antes de ejecutar
        
        // Inicializar distancias con infinito
        for (Aeropuerto a : grafo.getAdyacencia().keySet()) {
            distancias.put(a, Double.MAX_VALUE);
        }
        distancias.put(origen, 0.0);

        // Cola de prioridad para escoger el nodo con menor distancia
        PriorityQueue<Aeropuerto> cola = new PriorityQueue<>(Comparator.comparingDouble(distancias::get));
        cola.add(origen);

        while (!cola.isEmpty()) {
            Aeropuerto actual = cola.poll();

            for (Vuelo vuelo : grafo.obtenerAdyacentes(actual)) {
                Aeropuerto vecino = vuelo.getDestino();
                double nuevaDist = distancias.get(actual) + vuelo.getDistancia();

                if (nuevaDist < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDist);
                    predecesor.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }
        return distancias;
    }

    /**
     * Reconstruir la ruta más corta desde el predecesor generado por Dijkstra
     */
    public static List<Aeropuerto> obtenerRuta(Map<Aeropuerto, Aeropuerto> predecesor, Aeropuerto destino) {
        LinkedList<Aeropuerto> ruta = new LinkedList<>();
        Aeropuerto actual = destino;

        while (actual != null) {
            ruta.addFirst(actual);
            actual = predecesor.get(actual);
        }
        return ruta;
    }

    /**
     * Búsqueda en anchura (BFS) – útil para grafos no ponderados
     */
    public static List<Aeropuerto> bfs(Grafo grafo, Aeropuerto origen) {
        List<Aeropuerto> visitados = new ArrayList<>();
        Queue<Aeropuerto> cola = new LinkedList<>();

        cola.add(origen);
        while (!cola.isEmpty()) {
            Aeropuerto actual = cola.poll();
            if (!visitados.contains(actual)) {
                visitados.add(actual);
                for (Vuelo vuelo : grafo.obtenerAdyacentes(actual)) {
                    cola.add(vuelo.getDestino());
                }
            }
        }
        return visitados;
    }

    /**
     * Búsqueda en profundidad (DFS)
     */
    public static void dfs(Grafo grafo, Aeropuerto origen, Set<Aeropuerto> visitados) {
        visitados.add(origen);
        for (Vuelo vuelo : grafo.obtenerAdyacentes(origen)) {
            if (!visitados.contains(vuelo.getDestino())) {
                dfs(grafo, vuelo.getDestino(), visitados);
            }
        }
    }

    /**
     * Estadísticas: número de conexiones por aeropuerto
     */
    public static Map<Aeropuerto, Integer> conexionesPorAeropuerto(Grafo grafo) {
        Map<Aeropuerto, Integer> conexiones = new HashMap<>();
        for (Aeropuerto a : grafo.getAdyacencia().keySet()) {
            conexiones.put(a, grafo.obtenerAdyacentes(a).size());
        }
        return conexiones;
    }

    /**
     * Aeropuerto más conectado
     */
    public static Aeropuerto masConectado(Grafo grafo) {
        Aeropuerto mejor = null;
        int max = -1;
        for (Aeropuerto a : grafo.getAdyacencia().keySet()) {
            int conexiones = grafo.obtenerAdyacentes(a).size();
            if (conexiones > max) {
                max = conexiones;
                mejor = a;
            }
        }
        return mejor;
    }

    /**
     * Aeropuerto menos conectado
     */
    public static Aeropuerto menosConectado(Grafo grafo) {
        Aeropuerto peor = null;
        int min = Integer.MAX_VALUE;
        for (Aeropuerto a : grafo.getAdyacencia().keySet()) {
            int conexiones = grafo.obtenerAdyacentes(a).size();
            if (conexiones < min) {
                min = conexiones;
                peor = a;
            }
        }
        return peor;
    }
}

