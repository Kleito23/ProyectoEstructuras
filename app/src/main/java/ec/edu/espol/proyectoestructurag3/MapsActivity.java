package ec.edu.espol.proyectoestructurag3;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.PolylineOptions;
import org.maplibre.android.annotations.Polyline;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private FloatingActionButton fabAirports;
    private List<Airport> selectedAirports;
    private List<Airport> availableAirports;

    // Mapas para relacionar marcadores con aeropuertos
    private Map<Marker, Airport> markerToAirport;
    private Map<Airport, Marker> airportToMarker;
    private List<Polyline> flightConnections;
    private Map<Polyline, Marker> polylineToSourceMarker;
    private Map<Polyline, Marker> polylineToDestMarker;
    private Marker initialMarker;
    private View currentInfoWindow;

    // Ubicación inicial (Beijing Daxing International Airport)
    private static final double LAT_INICIAL = 39.5098;
    private static final double LNG_INICIAL = 116.4109;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            // Inicializar MapLibre ANTES de setContentView
            MapLibre.getInstance(this);

            setContentView(R.layout.activity_maps);

            // Inicializar listas y mapas
            selectedAirports = new ArrayList<>();
            availableAirports = loadAirportsFromJson();
            markerToAirport = new HashMap<>();
            airportToMarker = new HashMap<>();
            flightConnections = new ArrayList<>();
            polylineToSourceMarker = new HashMap<>();
            polylineToDestMarker = new HashMap<>();

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

            // Configurar navegación inferior
            BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
            bottomNavigation.setSelectedItemId(R.id.nav_flights);

            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_flights) {
                    return true; // Ya estamos aquí
                } else if (itemId == R.id.nav_statistics) {
                    startActivity(new Intent(this, EstadisticaActivity.class));
                    finish();
                    return true;
                }
                return false;
            });

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
                                    .target(new LatLng(LAT_INICIAL, LNG_INICIAL))
                                    .zoom(8.0)
                                    .build();
                            map.setCameraPosition(cameraPosition);

                            // Agregar marcador inicial
                            initialMarker = map.addMarker(new MarkerOptions()
                                    .position(new LatLng(LAT_INICIAL, LNG_INICIAL))
                                    .title("Beijing Daxing International Airport")
                                    .snippet("PKX - Beijing, China"));

                            // Configurar InfoWindow personalizado
                            map.setInfoWindowAdapter(new MapLibreMap.InfoWindowAdapter() {
                                @Override
                                public View getInfoWindow(Marker marker) {
                                    return createInfoWindow(marker);
                                }
                            });

                            Log.d(TAG, "Configuración inicial completada");
                        }
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error en onMapReady: ", e);
        }
    }

    // Cargar aeropuertos desde el archivo JSON
    private List<Airport> loadAirportsFromJson() {
        List<Airport> airports = new ArrayList<>();
        try {
            InputStream inputStream = getAssets().open("aeropuertos.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String jsonString = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String codigo = jsonObject.getString("codigo");
                String nombre = jsonObject.getString("nombre");
                String ciudad = jsonObject.getString("ciudad");
                String pais = jsonObject.getString("pais");
                double latitud = jsonObject.getDouble("latitud");
                double longitud = jsonObject.getDouble("longitud");

                airports.add(new Airport(nombre, codigo, ciudad, pais, latitud, longitud));
            }

            Log.d(TAG, "Cargados " + airports.size() + " aeropuertos desde JSON");

        } catch (IOException | org.json.JSONException e) {
            Log.e(TAG, "Error cargando aeropuertos desde JSON: ", e);
            // Fallback: crear lista básica en caso de error
            airports.add(new Airport("Sydney Kingsford Smith Airport", "SYD", "Sydney", "Australia", -33.9399, 151.1753));
            airports.add(new Airport("Melbourne Airport", "MEL", "Melbourne", "Australia", -37.6690, 144.8410));
        }

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
            Marker marker = mapLibreMap.addMarker(new MarkerOptions()
                    .position(new LatLng(airport.getLatitude(), airport.getLongitude()))
                    .title(airport.getName())
                    .snippet(airport.getCode() + " - " + airport.getLocation()));

            // Guardar relaciones entre marcador y aeropuerto
            markerToAirport.put(marker, airport);
            airportToMarker.put(airport, marker);
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
            boundsBuilder.include(new LatLng(LAT_INICIAL, LNG_INICIAL));

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

    // Crear InfoWindow personalizado para mostrar botones debajo del nombre
    private View createInfoWindow(Marker marker) {
        View infoWindow = LayoutInflater.from(this).inflate(R.layout.custom_info_window, null);
        currentInfoWindow = infoWindow;

        boolean isInitialMarker = marker.equals(initialMarker);

        // Configurar textos
        android.widget.TextView tvTitle = infoWindow.findViewById(R.id.tvInfoTitle);
        android.widget.TextView tvSnippet = infoWindow.findViewById(R.id.tvInfoSnippet);

        tvTitle.setText(marker.getTitle());
        tvSnippet.setText(marker.getSnippet());

        // Configurar botones
        Button btnConnect = infoWindow.findViewById(R.id.btnInfoConnect);
        Button btnDelete = infoWindow.findViewById(R.id.btnInfoDelete);

        // Configurar listeners
        btnConnect.setOnClickListener(v -> {
            connectFlight(marker);
            marker.hideInfoWindow(); // Cerrar InfoWindow después de la acción
        });

        btnDelete.setOnClickListener(v -> {
            deleteAirportFromMap(marker);
            marker.hideInfoWindow(); // Cerrar InfoWindow después de la acción
        });

        // Ocultar botón eliminar para el marcador inicial
        if (isInitialMarker) {
            btnDelete.setVisibility(View.GONE);
        }

        return infoWindow;
    }

    // Eliminar aeropuerto del mapa (pero NO de la lista disponible)
    private void deleteAirportFromMap(Marker marker) {
        Airport airport = markerToAirport.get(marker);
        if (airport != null && !marker.equals(initialMarker)) {
            // Remover SOLO de la lista de aeropuertos seleccionados
            // NO eliminamos de availableAirports para que siga disponible para agregar
            selectedAirports.remove(airport);

            // Remover SOLO las conexiones de vuelo que involucran este marcador específico
            List<Polyline> toRemove = new ArrayList<>();
            for (Polyline polyline : flightConnections) {
                Marker sourceMarker = polylineToSourceMarker.get(polyline);
                Marker destMarker = polylineToDestMarker.get(polyline);

                // Solo eliminar si este marcador es origen o destino de la conexión
                if (marker.equals(sourceMarker) || marker.equals(destMarker)) {
                    toRemove.add(polyline);
                }
            }

            // Eliminar las conexiones encontradas
            for (Polyline polyline : toRemove) {
                mapLibreMap.removePolyline(polyline);
                flightConnections.remove(polyline);
                // Limpiar también los mapas de relación
                polylineToSourceMarker.remove(polyline);
                polylineToDestMarker.remove(polyline);
            }

            // Remover marcador del mapa
            mapLibreMap.removeMarker(marker);

            // Remover de los mapas de relación
            markerToAirport.remove(marker);
            airportToMarker.remove(airport);

            // Actualizar vista del mapa
            updateMapBounds();

            Log.d(TAG, "Aeropuerto removido del mapa: " + airport.getName() +
                    " (eliminadas " + toRemove.size() + " conexiones, sigue disponible para agregar)");
        }
    }

    // Mostrar diálogo para seleccionar destino de conexión
    private void connectFlight(Marker sourceMarker) {
        // Obtener lista de posibles destinos (todos los marcadores excepto el origen)
        List<Marker> availableDestinations = new ArrayList<>();

        // Agregar marcador inicial si no es el origen
        if (!sourceMarker.equals(initialMarker)) {
            availableDestinations.add(initialMarker);
        }

        // Agregar otros aeropuertos en el mapa (excepto el origen)
        for (Map.Entry<Marker, Airport> entry : markerToAirport.entrySet()) {
            if (!entry.getKey().equals(sourceMarker)) {
                availableDestinations.add(entry.getKey());
            }
        }

        if (availableDestinations.isEmpty()) {
            Log.d(TAG, "No hay destinos disponibles para conectar");
            return;
        }

        showDestinationSelectionDialog(sourceMarker, availableDestinations);
    }

    // Mostrar diálogo de selección de destino
    private void showDestinationSelectionDialog(Marker sourceMarker, List<Marker> destinations) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_destination, null);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewDestinations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Crear lista de aeropuertos para el adaptador
        List<Airport> destinationAirports = new ArrayList<>();
        for (Marker marker : destinations) {
            if (marker.equals(initialMarker)) {
                // Crear un Airport temporal para el marcador inicial
                Airport initialAirport = new Airport("Beijing Daxing International Airport", "PKX", "Beijing", "China", LAT_INICIAL, LNG_INICIAL);
                destinationAirports.add(initialAirport);
            } else {
                Airport airport = markerToAirport.get(marker);
                if (airport != null) {
                    destinationAirports.add(airport);
                }
            }
        }

        // Crear adaptador con callback para selección
        AirportAdapter adapter = new AirportAdapter(destinationAirports, airport -> {
            // Encontrar el marcador correspondiente al aeropuerto seleccionado
            Marker destinationMarker = null;
            if (airport.getLatitude() == LAT_INICIAL && airport.getLongitude() == LNG_INICIAL) {
                destinationMarker = initialMarker;
            } else {
                destinationMarker = airportToMarker.get(airport);
            }

            if (destinationMarker != null) {
                createFlightConnection(sourceMarker, destinationMarker);
            }
            dialog.dismiss();
        });

        recyclerView.setAdapter(adapter);

        dialogView.findViewById(R.id.btnCancelConnection).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Crear conexión de vuelo entre dos marcadores específicos
    private void createFlightConnection(Marker sourceMarker, Marker destinationMarker) {
        if (mapLibreMap != null) {
            LatLng startPoint = sourceMarker.getPosition();
            LatLng endPoint = destinationMarker.getPosition();

            // Crear línea de vuelo
            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(startPoint)
                    .add(endPoint)
                    .color(android.graphics.Color.BLUE)
                    .width(3.0f);

            Polyline polyline = mapLibreMap.addPolyline(polylineOptions);
            flightConnections.add(polyline);

            // Guardar relación entre polyline y marcadores para poder eliminar conexiones específicas
            polylineToSourceMarker.put(polyline, sourceMarker);
            polylineToDestMarker.put(polyline, destinationMarker);

            // Obtener nombres para el log
            String sourceName = sourceMarker.equals(initialMarker) ? "Beijing (Inicial)" :
                    (markerToAirport.get(sourceMarker) != null ? markerToAirport.get(sourceMarker).getName() : "Origen");
            String destName = destinationMarker.equals(initialMarker) ? "Beijing (Inicial)" :
                    (markerToAirport.get(destinationMarker) != null ? markerToAirport.get(destinationMarker).getName() : "Destino");

            Log.d(TAG, "Vuelo conectado: " + sourceName + " → " + destName);
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
