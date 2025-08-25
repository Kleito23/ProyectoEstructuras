package ec.edu.espol.proyectoestructurag3;

public class Aeropuerto {
    private String name;
    private String code;
    private String city;
    private String country;
    private double latitude;
    private double longitude;

    public Aeropuerto(String name, String code, String city, String country, double latitude, double longitude) {
        this.name = name;
        this.code = code;
        this.city = city;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() { return name; }
    public String getCode() { return code; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public String getLocation() {
        return city + ", " + country;
    }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Aeropuerto other = (Aeropuerto) obj;
        return code.equals(other.code);
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

    // Método para calcular distancia usando fórmula de Haversine
    public double calcularDistancia(Aeropuerto destino) {
        final int R = 6371; // Radio de la Tierra en km

        double latDistance = Math.toRadians(destino.latitude - this.latitude);
        double lonDistance = Math.toRadians(destino.longitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(destino.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distancia en kilómetros
    }
}
