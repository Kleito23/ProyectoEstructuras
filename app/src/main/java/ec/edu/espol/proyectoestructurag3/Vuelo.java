package ec.edu.espol.proyectoestructurag3;/*
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
    private double distancia;      // distancia en km
    Aerolinea aerolinea;

    public Vuelo(int id, Aeropuerto origen, Aeropuerto destino, double distancia, Aerolinea aerolinea) {
        this.id = id;
        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
        this.aerolinea = aerolinea;
    }

    public int getId() { return id; }
    public Aeropuerto getOrigen() { return origen; }
    public Aeropuerto getDestino() { return destino; }
    public Aerolinea getAerolinea() { return aerolinea; }
    public double getDistancia() { return distancia; }

    public double getCosto() { return aerolinea.getCostoPromedio(); }
    public double getTiempo() { return aerolinea.getTiempoPromedio(); }

    @Override
    public String toString() {
        return origen.getCode() + " → " + destino.getCode() + " (" + aerolinea + ")";
    }
}



