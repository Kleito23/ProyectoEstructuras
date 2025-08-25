package ec.edu.espol.proyectoestructurag3;

import java.util.ArrayList;
import java.util.List;

public class ArbolAerolineas {
    private NodoAerolinea raiz;

    private static class NodoAerolinea {
        Aerolinea aerolinea;
        NodoAerolinea izquierdo, derecho;

        NodoAerolinea(Aerolinea aerolinea) {
            this.aerolinea = aerolinea;
        }
    }

    public void insertar(Aerolinea aerolinea) {
        raiz = insertarRec(raiz, aerolinea);
    }

    private NodoAerolinea insertarRec(NodoAerolinea raiz, Aerolinea aerolinea) {
        if (raiz == null) {
            return new NodoAerolinea(aerolinea);
        }

        int comparacion = aerolinea.getNombre().compareToIgnoreCase(raiz.aerolinea.getNombre());
        if (comparacion < 0) {
            raiz.izquierdo = insertarRec(raiz.izquierdo, aerolinea);
        } else if (comparacion > 0) {
            raiz.derecho = insertarRec(raiz.derecho, aerolinea);
        }

        return raiz;
    }

    public Aerolinea buscar(String nombre) {
        return buscarRec(raiz, nombre);
    }

    private Aerolinea buscarRec(NodoAerolinea raiz, String nombre) {
        if (raiz == null) return null;

        int comparacion = nombre.compareToIgnoreCase(raiz.aerolinea.getNombre());
        if (comparacion == 0) {
            return raiz.aerolinea;
        } else if (comparacion < 0) {
            return buscarRec(raiz.izquierdo, nombre);
        } else {
            return buscarRec(raiz.derecho, nombre);
        }
    }

    public List<Aerolinea> obtenerOrdenadoPorNombre() {
        List<Aerolinea> lista = new ArrayList<>();
        inOrderRec(raiz, lista);
        return lista;
    }

    private void inOrderRec(NodoAerolinea raiz, List<Aerolinea> lista) {
        if (raiz != null) {
            inOrderRec(raiz.izquierdo, lista);
            lista.add(raiz.aerolinea);
            inOrderRec(raiz.derecho, lista);
        }
    }

    public List<Aerolinea> obtenerOrdenadoPorCosto() {
        List<Aerolinea> lista = obtenerOrdenadoPorNombre();
        lista.sort((a1, a2) -> Double.compare(a1.getCostoPromedio(), a2.getCostoPromedio()));
        return lista;
    }

    public List<Aerolinea> obtenerOrdenadoPorTiempo() {
        List<Aerolinea> lista = obtenerOrdenadoPorNombre();
        lista.sort((a1, a2) -> Double.compare(a1.getTiempoPromedio(), a2.getTiempoPromedio()));
        return lista;
    }
}
