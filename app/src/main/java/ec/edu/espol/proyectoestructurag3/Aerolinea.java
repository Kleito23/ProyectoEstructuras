package ec.edu.espol.proyectoestructurag3;

public class Aerolinea {
    private String nombre;
    private String codigo; // ejemplo: "AA"
    private double costoPromedio;
    private double tiempoPromedio; // en horas

    public Aerolinea(String nombre, String codigo, double costoPromedio, double tiempoPromedio) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.costoPromedio = costoPromedio;
        this.tiempoPromedio = tiempoPromedio;
    }

    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }
    public double getCostoPromedio() { return costoPromedio; }
    public double getTiempoPromedio() { return tiempoPromedio; }

    @Override
    public String toString() {
        return nombre + " (" + codigo + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Aerolinea other = (Aerolinea) obj;
        return codigo.equals(other.codigo);
    }

    @Override
    public int hashCode() {
        return codigo.hashCode();
    }
}


