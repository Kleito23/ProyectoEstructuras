package ec.edu.espol.proyectoestructurag3;/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.util.*;


/**
 *
 * @author Milena Avellan
 */


public class Grafo {
    private static Grafo instance;   // Singleton
    private Map<Aeropuerto, List<Vuelo>> adyacencia;

    public Grafo() {
        adyacencia = new HashMap<>();
    }

    public Map<Aeropuerto, List<Vuelo>> getAdyacencia() {
        return adyacencia;
    }


    // AEROPUERTOS
    public void agregarAeropuerto(Aeropuerto a) {
        if (!adyacencia.containsKey(a)) {
            adyacencia.put(a, new ArrayList<>());
        }
    }

    public boolean existeAeropuerto(String codigo) {
        for (Aeropuerto a : adyacencia.keySet()) {
            if (a.getCode().equals(codigo)) return true;
        }
        return false;
    }

    public static Grafo getInstance() {
        if (instance == null) {
            instance = new Grafo();
        }
        return instance;
    }

    public Aeropuerto buscarAeropuerto(String codigo) {
        for (Aeropuerto a : adyacencia.keySet()) {
            if (a.getCode().equals(codigo)) return a;
        }
        return null;
    }

    public void eliminarAeropuerto(Aeropuerto a) {
    if (!adyacencia.containsKey(a)) return;
    adyacencia.remove(a);
    for (List<Vuelo> lista : adyacencia.values()) {

        List<Vuelo> vuelosValidos = new ArrayList<>();
        for (Vuelo v : lista) {
            if (!v.getDestino().equals(a)) {
                vuelosValidos.add(v); 
            }
        }
        lista.clear();
        lista.addAll(vuelosValidos);
    }
}


    public List<Aeropuerto> listarAeropuertos() {
        return new ArrayList<>(adyacencia.keySet());
    }

    // VUELOS

    public boolean agregarVuelo(Vuelo vuelo) {
        Aeropuerto origen = vuelo.getOrigen();
        Aeropuerto destino = vuelo.getDestino();

        // Verificar que ambos aeropuertos existen en el grafo
        if (!adyacencia.containsKey(origen) || !adyacencia.containsKey(destino)) {
            System.out.println("Error: uno o ambos aeropuertos no son válidos");
            return false;
        }

        // Verificar que no exista un vuelo con el mismo ID desde este origen
        for (Vuelo v : adyacencia.get(origen)) {
            if (v.getId() == vuelo.getId()) {
                System.out.println("Error: ya existe un vuelo con ese ID");
                return false;
            }
        }

        // Agregar el vuelo
        adyacencia.get(origen).add(vuelo);
        return true;
    }
    


    public boolean existeVuelo(Aeropuerto origen, Aeropuerto destino, int id) {
    if (!adyacencia.containsKey(origen)) return false;
    for (Vuelo v : adyacencia.get(origen)) {
        if (v.getDestino().equals(destino) && v.getId() == id) {
            return true;
        }
    }
    return false;
}


    public void eliminarVuelo(Aeropuerto origen, Aeropuerto destino, int id) {
    if (!adyacencia.containsKey(origen)) return;

    Iterator<Vuelo> it = adyacencia.get(origen).iterator();
    while (it.hasNext()) {
        Vuelo v = it.next();
        if (v.getDestino().equals(destino) && v.getId() == id) {
            it.remove();
        }
    }
}


    public List<Vuelo> listarVuelos() {
        List<Vuelo> todos = new ArrayList<>();
        for (List<Vuelo> lista : adyacencia.values()) {
            todos.addAll(lista);
        }
        return todos;
    }
    // GRAFO
   
    public List<Vuelo> obtenerAdyacentes(Aeropuerto a) {
    if (!adyacencia.containsKey(a)) {
        throw new IllegalArgumentException("El aeropuerto no existe");
    }

    List<Vuelo> vuelos = adyacencia.get(a);
    if (vuelos.isEmpty()) {
        System.out.println("No hay vuelos disponibles desde " + a.getName());
    }

    return vuelos;
}

    public List<Aeropuerto> getAeropuertosLista() {
        return new ArrayList<>(adyacencia.keySet());
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Aeropuerto a : adyacencia.keySet()) {
            sb.append(a.toString()).append(" -> ").append(adyacencia.get(a)).append("\n");
        }
        return sb.toString();
    }}

   