/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


/**
 *
 * @author Milena Avellan
 */
public class Vuelo {
    private int id;
    private Aeropuerto origen;
    private Aeropuerto destino;
    private double distancia; 
    private double costo;     
    private double tiempo;    

    public Vuelo(int id,Aeropuerto origen, Aeropuerto destino, double distancia, double costo, double tiempo) {
        this.id=id;
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
        this.costo = costo;
        this.tiempo = tiempo;
    }
    public int getId(){
        return id;
    }

    public Aeropuerto getOrigen() { 
        return origen; 
    }
    public Aeropuerto getDestino() { 
        return destino; 
    }
    public double getDistancia() { 
        return distancia; 
    }
    public double getCosto() { 
        return costo; 
    }
    public double getTiempo() { 
        return tiempo; 
    }

    @Override
    public String toString() {
        return origen.getCodigo() + " -> " + destino.getCodigo() + 
               " | Distancia: " + distancia + " km" + 
               " | Costo: $" + costo + 
               " | Tiempo: " + tiempo + " h";
    }
}



