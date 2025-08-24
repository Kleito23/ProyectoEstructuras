package com.mycompany.proyecto;
import java.util.Objects;
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Milena Avellan
 */


public class Aeropuerto {
    private String codigo;   
    private String nombre;   
    private String pais;     
    private double latitud; 
    private double longitud; 

    public Aeropuerto(String codigo, String nombre, String pais, double latitud, double longitud) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.pais = pais;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    
    public String getCodigo() { 
        return codigo; 
    }
    public String getNombre() { 
        return nombre; 
    }
    public String getPais() { 
        return pais; 
    }
    public double getLatitud() { 
        return latitud; 
    }
    public double getLongitud() { 
        return longitud; 
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre + " (" + pais + ") [" + latitud + ", " + longitud + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Aeropuerto)) return false;
        Aeropuerto otro = (Aeropuerto) obj;
        return Objects.equals(this.codigo, otro.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigo);
    }
}

