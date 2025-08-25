package ec.edu.espol.proyectoestructurag3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class EstadisticaActivity extends AppCompatActivity {

    private static final String TAG = "EstadisticaActivity";
    private Grafo grafo;
    private GestorAerolineas gestorAerolineas;

    private TextView tvTotalAeropuertos;
    private TextView tvTotalVuelos;
    private TextView tvTotalAerolineas;
    private TextView tvRutaMasDemandada;
    private RecyclerView rvAerolineasStats;
    private RecyclerView rvRutasStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Inicializar referencias
        grafo = Grafo.getInstance();
        gestorAerolineas = GestorAerolineas.getInstance();

        // Inicializar vistas
        initViews();

        // Configurar navegación inferior
        setupBottomNavigation();

        // Cargar estadísticas
        loadStatistics();
    }

    private void initViews() {
        tvTotalAeropuertos = findViewById(R.id.tvTotalAeropuertos);
        tvTotalVuelos = findViewById(R.id.tvTotalVuelos);
        tvTotalAerolineas = findViewById(R.id.tvTotalAerolineas);
        tvRutaMasDemandada = findViewById(R.id.tvRutaMasDemandada);
        rvAerolineasStats = findViewById(R.id.rvAerolineasStats);
        rvRutasStats = findViewById(R.id.rvRutasStats);

        // Configurar RecyclerViews
        rvAerolineasStats.setLayoutManager(new LinearLayoutManager(this));
        rvRutasStats.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_statistics);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_flights) {
                startActivity(new Intent(this, MapsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_statistics) {
                return true; // Ya estamos aquí
            }
            return false;
        });
    }

    private void loadStatistics() {
        // Estadísticas básicas
        List<Aeropuerto> aeropuertos = grafo.listarAeropuertos();
        List<Vuelo> vuelos = grafo.listarVuelos();
        List<Aerolinea> aerolineas = gestorAerolineas.todasLasAerolineas();

        tvTotalAeropuertos.setText("Total Aeropuertos: " + aeropuertos.size());
        tvTotalVuelos.setText("Total Vuelos: " + vuelos.size());
        tvTotalAerolineas.setText("Total Aerolíneas: " + aerolineas.size());

        // Encontrar ruta más demandada
        String rutaMasDemandada = encontrarRutaMasDemandada(vuelos);
        tvRutaMasDemandada.setText("Ruta más demandada: " + rutaMasDemandada);

        // Estadísticas de aerolíneas ordenadas por costo
        List<Aerolinea> aerolineasOrdenadas = gestorAerolineas.obtenerAerolineasOrdenadas("costo");
        AerolineaStatsAdapter aerolineaAdapter = new AerolineaStatsAdapter(aerolineasOrdenadas, vuelos);
        rvAerolineasStats.setAdapter(aerolineaAdapter);

        // Estadísticas de rutas
        List<RutaStats> rutasStats = calcularEstadisticasRutas(vuelos);
        RutaStatsAdapter rutaAdapter = new RutaStatsAdapter(rutasStats);
        rvRutasStats.setAdapter(rutaAdapter);
    }

    private String encontrarRutaMasDemandada(List<Vuelo> vuelos) {
        Map<String, Integer> rutaCount = new HashMap<>();

        for (Vuelo vuelo : vuelos) {
            String ruta = vuelo.getOrigen().getCode() + " → " + vuelo.getDestino().getCode();
            rutaCount.put(ruta, rutaCount.getOrDefault(ruta, 0) + 1);
        }

        String rutaMasDemandada = "Ninguna";
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : rutaCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                rutaMasDemandada = entry.getKey() + " (" + maxCount + " vuelos)";
            }
        }

        return rutaMasDemandada;
    }

    private List<RutaStats> calcularEstadisticasRutas(List<Vuelo> vuelos) {
        Map<String, RutaStats> rutasMap = new HashMap<>();

        for (Vuelo vuelo : vuelos) {
            String rutaKey = vuelo.getOrigen().getCode() + " → " + vuelo.getDestino().getCode();

            if (!rutasMap.containsKey(rutaKey)) {
                rutasMap.put(rutaKey, new RutaStats(rutaKey, vuelo.getDistancia()));
            }

            RutaStats stats = rutasMap.get(rutaKey);
            stats.agregarVuelo(vuelo);
        }

        List<RutaStats> rutasList = new ArrayList<>(rutasMap.values());
        rutasList.sort((r1, r2) -> Integer.compare(r2.getCantidadVuelos(), r1.getCantidadVuelos()));

        return rutasList;
    }

    // Clase para estadísticas de rutas
    public static class RutaStats {
        private String ruta;
        private double distancia;
        private int cantidadVuelos;
        private double costoPromedio;
        private double tiempoPromedio;
        private List<String> aerolineas;

        public RutaStats(String ruta, double distancia) {
            this.ruta = ruta;
            this.distancia = distancia;
            this.cantidadVuelos = 0;
            this.costoPromedio = 0;
            this.tiempoPromedio = 0;
            this.aerolineas = new ArrayList<>();
        }

        public void agregarVuelo(Vuelo vuelo) {
            cantidadVuelos++;
            costoPromedio = ((costoPromedio * (cantidadVuelos - 1)) + vuelo.getCosto()) / cantidadVuelos;
            tiempoPromedio = ((tiempoPromedio * (cantidadVuelos - 1)) + vuelo.getTiempo()) / cantidadVuelos;

            String aerolinea = vuelo.getAerolinea().getCodigo();
            if (!aerolineas.contains(aerolinea)) {
                aerolineas.add(aerolinea);
            }
        }

        // Getters
        public String getRuta() { return ruta; }
        public double getDistancia() { return distancia; }
        public int getCantidadVuelos() { return cantidadVuelos; }
        public double getCostoPromedio() { return costoPromedio; }
        public double getTiempoPromedio() { return tiempoPromedio; }
        public List<String> getAerolineas() { return aerolineas; }
    }
}