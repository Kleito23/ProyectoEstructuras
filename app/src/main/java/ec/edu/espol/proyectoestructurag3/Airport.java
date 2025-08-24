package ec.edu.espol.proyectoestructurag3;

public class Airport {
    private String name;
    private String code;
    private String city;
    private String country;
    private double latitude;
    private double longitude;

    public Airport(String name, String code, String city, String country, double latitude, double longitude) {
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
}
