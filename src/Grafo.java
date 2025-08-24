/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.util.*;


/**
 *
 * @author Milena Avellan
 */


public class Grafo {
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
            if (a.getCodigo().equals(codigo)) return true;
        }
        return false;
    }

    public Aeropuerto buscarAeropuerto(String codigo) {
        for (Aeropuerto a : adyacencia.keySet()) {
            if (a.getCodigo().equals(codigo)) return a;
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

    public boolean agregarVuelo(int id, Aeropuerto origen, Aeropuerto destino, double distancia, double costo, double tiempo) {
    if (!adyacencia.containsKey(origen) || !adyacencia.containsKey(destino)) {
        System.out.println("Error: uno o ambos aeropuertos no son válidos");
        return false;
    }
    for (Vuelo v : adyacencia.get(origen)) {
        if (v.getId() == id) {
            System.out.println("Error: ya existe un vuelo con ese ID");
            return false;
        }
    }

    Vuelo vuelo = new Vuelo(id, origen, destino, distancia, costo, tiempo);
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
        System.out.println("No hay vuelos disponibles desde " + a.getNombre());
    }

    return vuelos;
}


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Aeropuerto a : adyacencia.keySet()) {
            sb.append(a.toString()).append(" -> ").append(adyacencia.get(a)).append("\n");
        }
        return sb.toString();
    }}

   