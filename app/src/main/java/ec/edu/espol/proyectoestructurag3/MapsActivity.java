package ec.edu.espol.proyectoestructurag3;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.maplibre.android.MapLibre;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.annotations.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private FloatingActionButton fabAirports;
    private List<Airport> selectedAirports;
    private List<Airport> availableAirports;

    // Ubicación inicial (Australia Occidental)
    private static final double LAT_ANTIGUA = -39.511944;
    private static final double LNG_ANTIGUA = 116.410556;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Inicializar MapLibre ANTES de setContentView
            MapLibre.getInstance(this);

            setContentView(R.layout.activity_maps);

            // Inicializar listas
            selectedAirports = new ArrayList<>();
            availableAirports = createAirportsList();

            // Inicializar vistas
            mapView = findViewById(R.id.mapView);
            fabAirports = findViewById(R.id.fabAirports);

            if (mapView != null) {
                mapView.onCreate(savedInstanceState);
                mapView.getMapAsync(this);
            } else {
                Log.e(TAG, "MapView no encontrado en el layout");
            }

            // Configurar botón flotante
            fabAirports.setOnClickListener(v -> showAirportsDialog());

        } catch (Exception e) {
            Log.e(TAG, "Error en onCreate: ", e);
        }
    }

    @Override
    public void onMapReady(MapLibreMap map) {
        try {
            Log.d(TAG, "Mapa listo, configurando estilo...");
            this.mapLibreMap = map;

            // Establecer estilo con callback para manejar errores
            map.setStyle(new Style.Builder().fromUri("https://demotiles.maplibre.org/style.json"),
                    new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(Style style) {
                            Log.d(TAG, "Estilo cargado exitosamente");

                            // Centrar cámara en ubicación inicial
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(LAT_ANTIGUA, LNG_ANTIGUA))
                                    .zoom(8.0)
                                    .build();
                            map.setCameraPosition(cameraPosition);

                            // Agregar marcador inicial
                            map.addMarker(new MarkerOptions()
                                    .position(new LatLng(LAT_ANTIGUA, LNG_ANTIGUA))
                                    .title("Ubicación inicial")
                                    .snippet("Punto de partida"));

                            Log.d(TAG, "Configuración inicial completada");
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error en onMapReady: ", e);
        }
    }

    // Crear lista de aeropuertos disponibles
    private List<Airport> createAirportsList() {
        List<Airport> airports = new ArrayList<>();

        // Aeropuertos principales de Australia
        airports.add(new Airport("Sydney Kingsford Smith Airport", "SYD", "Sydney", "Australia", -33.9399, 151.1753));
        airports.add(new Airport("Melbourne Airport", "MEL", "Melbourne", "Australia", -37.6690, 144.8410));
        airports.add(new Airport("Brisbane Airport", "BNE", "Brisbane", "Australia", -27.3942, 153.1218));
        airports.add(new Airport("Perth Airport", "PER", "Perth", "Australia", -31.9385, 115.9672));
        airports.add(new Airport("Adelaide Airport", "ADL", "Adelaide", "Australia", -34.9285, 138.5304));
        airports.add(new Airport("Gold Coast Airport", "OOL", "Gold Coast", "Australia", -28.1644, 153.5067));
        airports.add(new Airport("Canberra Airport", "CBR", "Canberra", "Australia", -35.3069, 149.1953));
        airports.add(new Airport("Darwin International Airport", "DRW", "Darwin", "Australia", -12.4146, 130.8770));
        airports.add(new Airport("Hobart Airport", "HBA", "Hobart", "Australia", -42.8361, 147.5100));
        airports.add(new Airport("Cairns Airport", "CNS", "Cairns", "Australia", -16.8858, 145.7781));

        // Aeropuertos internacionales cercanos
        airports.add(new Airport("Singapore Changi Airport", "SIN", "Singapore", "Singapore", 1.3644, 103.9915));
        airports.add(new Airport("Auckland Airport", "AKL", "Auckland", "New Zealand", -37.0082, 174.7850));
        airports.add(new Airport("Christchurch Airport", "CHC", "Christchurch", "New Zealand", -43.4866, 172.5320));
        airports.add(new Airport("Jakarta Soekarno-Hatta", "CGK", "Jakarta", "Indonesia", -6.1256, 106.6559));
        airports.add(new Airport("Kuala Lumpur International", "KUL", "Kuala Lumpur", "Malaysia", 2.7456, 101.7099));

        return airports;
    }

    // Mostrar diálogo de selección de aeropuertos
    private void showAirportsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_airports, null);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewAirports);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Crear adaptador con referencia al diálogo para cerrarlo
        AirportAdapter adapter = new AirportAdapter(availableAirports, airport -> {
            onAirportSelected(airport);
            dialog.dismiss(); // Cerrar diálogo inmediatamente después de seleccionar
        });
        recyclerView.setAdapter(adapter);

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Callback cuando se selecciona un aeropuerto
    private void onAirportSelected(Airport airport) {
        if (!selectedAirports.contains(airport)) {
            selectedAirports.add(airport);
            addAirportMarker(airport);
            updateMapBounds();
            Log.d(TAG, "Aeropuerto agregado: " + airport.getName());
        }
    }

    // Agregar marcador de aeropuerto al mapa
    private void addAirportMarker(Airport airport) {
        if (mapLibreMap != null) {
            mapLibreMap.addMarker(new MarkerOptions()
                    .position(new LatLng(airport.getLatitude(), airport.getLongitude()))
                    .title(airport.getName())
                    .snippet(airport.getCode() + " - " + airport.getLocation()));
        }
    }

    // Actualizar límites del mapa para mostrar todos los puntos
    private void updateMapBounds() {
        if (mapLibreMap == null || selectedAirports.isEmpty()) {
            return;
        }

        try {
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

            // Incluir ubicación inicial
            boundsBuilder.include(new LatLng(LAT_ANTIGUA, LNG_ANTIGUA));

            // Incluir todos los aeropuertos seleccionados
            for (Airport airport : selectedAirports) {
                boundsBuilder.include(new LatLng(airport.getLatitude(), airport.getLongitude()));
            }

            LatLngBounds bounds = boundsBuilder.build();

            // Animar la cámara para mostrar todos los puntos con padding
            int padding = 100; // padding en pixels
            mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding), 1000);

            Log.d(TAG, "Mapa actualizado para mostrar " + (selectedAirports.size() + 1) + " puntos");

        } catch (Exception e) {
            Log.e(TAG, "Error al actualizar límites del mapa: ", e);
        }
    }

    // Métodos del ciclo de vida con manejo de errores
    @Override
    protected void onStart() {
        super.onStart();
        if (mapView != null) {
            mapView.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }
}
