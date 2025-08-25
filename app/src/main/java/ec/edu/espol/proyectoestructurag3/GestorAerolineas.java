package ec.edu.espol.proyectoestructurag3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestorAerolineas {
    private static GestorAerolineas instance;
    private ArbolAerolineas arbolAerolineas;
    private List<Aerolinea> aerolineasDisponibles;

    private GestorAerolineas() {
        arbolAerolineas = new ArbolAerolineas();
        aerolineasDisponibles = new ArrayList<>();
        inicializarAerolineas();
    }

    public static GestorAerolineas getInstance() {
        if (instance == null) {
            instance = new GestorAerolineas();
        }
        return instance;
    }

    private void inicializarAerolineas() {
        // Aerolíneas con diferentes costos y tiempos promedio
        List<Aerolinea> aerolineas = List.of(
                new Aerolinea("American Airlines", "AA", 450.0, 8.5),
                new Aerolinea("Delta Air Lines", "DL", 420.0, 9.0),
                new Aerolinea("United Airlines", "UA", 480.0, 8.0),
                new Aerolinea("Southwest Airlines", "WN", 320.0, 10.5),
                new Aerolinea("JetBlue Airways", "B6", 380.0, 9.5),
                new Aerolinea("Alaska Airlines", "AS", 400.0, 8.8),
                new Aerolinea("Spirit Airlines", "NK", 250.0, 11.0),
                new Aerolinea("Frontier Airlines", "F9", 280.0, 10.8),
                new Aerolinea("Air China", "CA", 520.0, 12.0),
                new Aerolinea("China Eastern", "MU", 480.0, 11.5),
                new Aerolinea("Emirates", "EK", 650.0, 7.5),
                new Aerolinea("Qatar Airways", "QR", 620.0, 8.0),
                new Aerolinea("Singapore Airlines", "SQ", 580.0, 7.8),
                new Aerolinea("Lufthansa", "LH", 550.0, 9.2),
                new Aerolinea("British Airways", "BA", 590.0, 8.5)
        );

        for (Aerolinea aerolinea : aerolineas) {
            arbolAerolineas.insertar(aerolinea);
            aerolineasDisponibles.add(aerolinea);
        }
    }

    public List<Aerolinea> obtenerAerolineasOrdenadas(String criterio) {
        switch (criterio.toLowerCase()) {
            case "costo":
                return arbolAerolineas.obtenerOrdenadoPorCosto();
            case "tiempo":
                return arbolAerolineas.obtenerOrdenadoPorTiempo();
            case "nombre":
            default:
                return arbolAerolineas.obtenerOrdenadoPorNombre();
        }
    }

    public Aerolinea obtenerAerolineaAleatoria() {
        Random random = new Random();
        return aerolineasDisponibles.get(random.nextInt(aerolineasDisponibles.size()));
    }

    public List<Aerolinea> obtenerVariasAerolineasAleatorias(int cantidad) {
        List<Aerolinea> seleccionadas = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < Math.min(cantidad, aerolineasDisponibles.size()); i++) {
            Aerolinea aerolinea;
            do {
                aerolinea = aerolineasDisponibles.get(random.nextInt(aerolineasDisponibles.size()));
            } while (seleccionadas.contains(aerolinea));
            seleccionadas.add(aerolinea);
        }

        return seleccionadas;
    }

    public Aerolinea buscarAerolinea(String nombre) {
        return arbolAerolineas.buscar(nombre);
    }

    public List<Aerolinea> todasLasAerolineas() {
        return new ArrayList<>(aerolineasDisponibles);
    }
}
