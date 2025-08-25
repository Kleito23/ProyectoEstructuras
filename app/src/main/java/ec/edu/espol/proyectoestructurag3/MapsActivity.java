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
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.SymbolLayer;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;
import org.maplibre.geojson.FeatureCollection;
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
    private List<Aeropuerto> selectedAirports;
    private List<Aeropuerto> availableAirports;

    // Mapas para relacionar marcadores con aeropuertos
    private Map<Marker, Aeropuerto> markerToAirport;
    private Map<Aeropuerto, Marker> airportToMarker;
    private List<Polyline> flightConnections;
    private Map<Polyline, Marker> polylineToSourceMarker;
    private Map<Polyline, Marker> polylineToDestMarker;
    private List<String> flightLayerIds;
    private static int layerCounter = 0;
    private Marker initialMarker;
    private View currentInfoWindow;

    // Grafo y gestión de aerolíneas
    private Grafo grafo;
    private GestorAerolineas gestorAerolineas;
    private DataPersistence dataPersistence;

    // Botones de control
    private Button btnDijkstra;
    private Button btnReset;

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
            flightLayerIds = new ArrayList<>();


            // Inicializar grafo, gestor de aerolíneas y persistencia
            grafo = Grafo.getInstance();
            gestorAerolineas = GestorAerolineas.getInstance();
            dataPersistence = new DataPersistence(this);

            // Agregar aeropuertos al grafo
            for (Aeropuerto aeropuerto : availableAirports) {
                grafo.agregarAeropuerto(aeropuerto);
            }

            // Los datos persistidos se cargarán cuando el mapa esté listo

            // Inicializar vistas
            mapView = findViewById(R.id.mapView);
            fabAirports = findViewById(R.id.fabAirports);
            btnDijkstra = findViewById(R.id.btnDijkstra);
            btnReset = findViewById(R.id.btnReset);

            if (mapView != null) {
                mapView.onCreate(savedInstanceState);
                mapView.getMapAsync(this);
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

            // Configurar botones de control
            btnDijkstra.setOnClickListener(v -> mostrarDialogoDijkstra());
            btnReset.setOnClickListener(v -> mostrarDialogoReset());

        } catch (Exception e) {
        }
    }

    @Override
    public void onMapReady(MapLibreMap map) {
        try {
            this.mapLibreMap = map;

            // Establecer estilo con callback para manejar errores
            map.setStyle(new Style.Builder().fromUri("https://demotiles.maplibre.org/style.json"),
                    new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(Style style) {

                            // Centrar cámara en ubicación inicial
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(LAT_INICIAL, LNG_INICIAL))
                                    .zoom(4.0)
                                    .build();
                            map.setCameraPosition(cameraPosition);

                            // Agregar marcador inicial
                            initialMarker = map.addMarker(new MarkerOptions()
                                    .position(new LatLng(LAT_INICIAL, LNG_INICIAL))
                                    .title("Beijing Daxing International Airport")
                                    .snippet("PKX - Beijing, China"));

                            // Agregar aeropuerto inicial al grafo
                            Aeropuerto aeropuertoInicial = new Aeropuerto("Beijing Daxing International Airport", "PKX", "Beijing", "China", LAT_INICIAL, LNG_INICIAL);
                            grafo.agregarAeropuerto(aeropuertoInicial);
                            markerToAirport.put(initialMarker, aeropuertoInicial);
                            airportToMarker.put(aeropuertoInicial, initialMarker);

                            // Configurar InfoWindow personalizado
                            map.setInfoWindowAdapter(new MapLibreMap.InfoWindowAdapter() {
                                @Override
                                public View getInfoWindow(Marker marker) {
                                    return createInfoWindow(marker);
                                }
                            });

                            cargarDatosPersistidosCompleto();
                        }
                    });

        } catch (Exception e) {
        }
    }

    // Cargar aeropuertos desde el archivo JSON
    private List<Aeropuerto> loadAirportsFromJson() {
        List<Aeropuerto> airports = new ArrayList<>();
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

                airports.add(new Aeropuerto(nombre, codigo, ciudad, pais, latitud, longitud));
            }



        } catch (IOException | org.json.JSONException e) {
            airports.add(new Aeropuerto("Sydney Kingsford Smith Airport", "SYD", "Sydney", "Australia", -33.9399, 151.1753));
            airports.add(new Aeropuerto("Melbourne Airport", "MEL", "Melbourne", "Australia", -37.6690, 144.8410));
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
    private void onAirportSelected(Aeropuerto airport) {
        if (!selectedAirports.contains(airport)) {
            selectedAirports.add(airport);
            addAirportMarker(airport);
            updateMapBounds();

        }
    }

    // Agregar marcador de aeropuerto al mapa
    private void addAirportMarker(Aeropuerto airport) {
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
            for (Aeropuerto airport : selectedAirports) {
                boundsBuilder.include(new LatLng(airport.getLatitude(), airport.getLongitude()));
            }

            LatLngBounds bounds = boundsBuilder.build();

            // Animar la cámara para mostrar todos los puntos con padding
            int padding = 300; // padding en pixels (más padding = menos zoom)
            mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding), 1000);



        } catch (Exception e) {
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
        Aeropuerto airport = markerToAirport.get(marker);
        if (airport != null && !marker.equals(initialMarker)) {
            // Remover SOLO de la lista de aeropuertos seleccionados
            // NO eliminamos de availableAirports para que siga disponible para agregar
            selectedAirports.remove(airport);

            // Eliminar vuelos del grafo que involucran este aeropuerto
            List<Vuelo> vuelosAEliminar = new ArrayList<>();
            for (Vuelo vuelo : grafo.listarVuelos()) {
                if (vuelo.getOrigen().equals(airport) || vuelo.getDestino().equals(airport)) {
                    vuelosAEliminar.add(vuelo);
                }
            }

            for (Vuelo vuelo : vuelosAEliminar) {
                grafo.eliminarVuelo(vuelo);
            }

            // Limpiar todas las conexiones y recrearlas sin las que involucran este aeropuerto
            limpiarConexiones();

            // Recrear las conexiones restantes
            for (Vuelo vuelo : grafo.listarVuelos()) {
                Marker origenMarker = airportToMarker.get(vuelo.getOrigen());
                Marker destinoMarker = airportToMarker.get(vuelo.getDestino());

                if (origenMarker == null && vuelo.getOrigen().getCode().equals("PKX")) {
                    origenMarker = initialMarker;
                }
                if (destinoMarker == null && vuelo.getDestino().getCode().equals("PKX")) {
                    destinoMarker = initialMarker;
                }

                if (origenMarker != null && destinoMarker != null) {
                    createFlightConnectionWithVuelo(origenMarker, destinoMarker, vuelo);
                }
            }

            // Remover marcador del mapa
            mapLibreMap.removeMarker(marker);

            // Remover de los mapas de relación
            markerToAirport.remove(marker);
            airportToMarker.remove(airport);

            updateMapBounds();
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
        for (Map.Entry<Marker, Aeropuerto> entry : markerToAirport.entrySet()) {
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
        List<Aeropuerto> destinationAirports = new ArrayList<>();
        for (Marker marker : destinations) {
            if (marker.equals(initialMarker)) {
                // Crear un Airport temporal para el marcador inicial
                Aeropuerto initialAirport = new Aeropuerto("Beijing Daxing International Airport", "PKX", "Beijing", "China", LAT_INICIAL, LNG_INICIAL);
                destinationAirports.add(initialAirport);
            } else {
                Aeropuerto airport = markerToAirport.get(marker);
                if (airport != null) {
                    destinationAirports.add(airport);
                }
            }
        }

        // Crear adaptador con callback para selección
        AirportAdapter adapter = new AirportAdapter(destinationAirports, airport -> {
            // Mostrar diálogo de selección de vuelos para este destino
            showFlightSelectionDialog(sourceMarker, airport);
            dialog.dismiss();
        });

        recyclerView.setAdapter(adapter);

        dialogView.findViewById(R.id.btnCancelConnection).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Mostrar diálogo de selección de vuelos con aerolíneas
    private void showFlightSelectionDialog(Marker sourceMarker, Aeropuerto destino) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_flight, null);

        Aeropuerto origen = markerToAirport.get(sourceMarker);
        if (origen == null) return;

        // Configurar información de la ruta
        android.widget.TextView tvRutaInfo = dialogView.findViewById(R.id.tvRutaInfo);
        tvRutaInfo.setText("Desde: " + origen.getCode() + " → Hacia: " + destino.getCode());

        // Crear vuelos disponibles con diferentes aerolíneas
        List<Vuelo> vuelosDisponibles = crearVuelosDisponibles(origen, destino);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewFlights);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Crear adaptador de vuelos
        VueloAdapter adapter = new VueloAdapter(vuelosDisponibles, vuelo -> {
            // Agregar el vuelo seleccionado al grafo
            grafo.agregarVuelo(vuelo);

            // Crear conexión visual en el mapa
            Marker destinationMarker = airportToMarker.get(destino);
            if (destinationMarker != null) {
                createFlightConnectionWithVuelo(sourceMarker, destinationMarker, vuelo);
            }

            // Guardar datos después de crear la conexión
            guardarDatos();
            dialog.dismiss();
        });

        recyclerView.setAdapter(adapter);

        // Configurar botones de ordenamiento
        dialogView.findViewById(R.id.btnOrdenarCosto).setOnClickListener(v -> {
            vuelosDisponibles.sort((v1, v2) -> Double.compare(v1.getCosto(), v2.getCosto()));
            adapter.notifyDataSetChanged();
        });

        dialogView.findViewById(R.id.btnOrdenarTiempo).setOnClickListener(v -> {
            vuelosDisponibles.sort((v1, v2) -> Double.compare(v1.getTiempo(), v2.getTiempo()));
            adapter.notifyDataSetChanged();
        });

        dialogView.findViewById(R.id.btnCancelFlight).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // Crear vuelos disponibles entre dos aeropuertos
    private List<Vuelo> crearVuelosDisponibles(Aeropuerto origen, Aeropuerto destino) {
        List<Vuelo> vuelos = new ArrayList<>();
        List<Aerolinea> aerolineas = gestorAerolineas.obtenerVariasAerolineasAleatorias(5); // 5 aerolíneas aleatorias

        double distancia = origen.calcularDistancia(destino);
        int vueloId = 1000 + (int)(Math.random() * 9000); // ID aleatorio

        for (Aerolinea aerolinea : aerolineas) {
            Vuelo vuelo = new Vuelo(vueloId++, origen, destino, distancia, aerolinea);
            vuelos.add(vuelo);
        }

        return vuelos;
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

    // Crear conexión de vuelo con información específica del vuelo
    private void createFlightConnectionWithVuelo(Marker sourceMarker, Marker destinationMarker, Vuelo vuelo) {
        if (mapLibreMap != null) {
            LatLng startPoint = sourceMarker.getPosition();
            LatLng endPoint = destinationMarker.getPosition();

            String vueloId = vuelo.getOrigen().getCode() + "-" + vuelo.getDestino().getCode() + "-" + (++layerCounter);
            crearLineaConTexto(startPoint, endPoint, vuelo.getDistancia(), getColorForAirline(vuelo.getAerolinea().getCodigo()), vueloId);
        }
    }

    // Obtener color basado en código de aerolínea
    private int getColorForAirline(String airlineCode) {
        switch (airlineCode) {
            case "AA": return android.graphics.Color.RED;
            case "DL": return android.graphics.Color.BLUE;
            case "UA": return android.graphics.Color.GREEN;
            case "WN": return android.graphics.Color.MAGENTA;
            case "B6": return android.graphics.Color.CYAN;
            case "AS": return 0xFF800080; // Purple
            case "NK": return 0xFFFFA500; // Orange
            case "F9": return 0xFF8B4513; // Brown
            case "CA": case "MU": return android.graphics.Color.RED;
            case "EK": case "QR": return 0xFFFFD700; // Gold
            case "SQ": return 0xFF4169E1; // Royal Blue
            case "LH": case "BA": return android.graphics.Color.GRAY;
            default: return android.graphics.Color.BLUE;
        }
    }

    private void crearLineaConTexto(LatLng inicio, LatLng fin, double distancia, int color, String vueloId) {
        if (mapLibreMap == null || mapLibreMap.getStyle() == null) {
            return;
        }

        // Si el estilo no está completamente cargado, esperar un poco
        if (!mapLibreMap.getStyle().isFullyLoaded()) {
            new android.os.Handler().postDelayed(() -> {
                crearLineaConTexto(inicio, fin, distancia, color, vueloId);
            }, 100);
            return;
        }

        List<Point> coordinates = new ArrayList<>();
        coordinates.add(Point.fromLngLat(inicio.getLongitude(), inicio.getLatitude()));
        coordinates.add(Point.fromLngLat(fin.getLongitude(), fin.getLatitude()));

        LineString lineString = LineString.fromLngLats(coordinates);
        Feature lineFeature = Feature.fromGeometry(lineString);
        lineFeature.addStringProperty("distance", String.format("%.0f km", distancia));

        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[]{lineFeature});

        String sourceId = "source-" + vueloId;
        String lineLayerId = "line-" + vueloId;
        String labelLayerId = "label-" + vueloId;

        try {
            // Solo crear si no existe ya
            if (mapLibreMap.getStyle().getSource(sourceId) == null) {
                GeoJsonSource source = new GeoJsonSource(sourceId, featureCollection);
                mapLibreMap.getStyle().addSource(source);
            }

            if (mapLibreMap.getStyle().getLayer(lineLayerId) == null) {
                LineLayer lineLayer = new LineLayer(lineLayerId, sourceId)
                        .withProperties(
                                PropertyFactory.lineColor(color),
                                PropertyFactory.lineWidth(4f)
                        );
                mapLibreMap.getStyle().addLayer(lineLayer);
            }

            if (mapLibreMap.getStyle().getLayer(labelLayerId) == null) {
                SymbolLayer labelLayer = new SymbolLayer(labelLayerId, sourceId)
                        .withProperties(
                                PropertyFactory.symbolPlacement("line"),
                                PropertyFactory.textField("{distance}"),
                                PropertyFactory.textSize(14f),
                                PropertyFactory.textColor("#000000"),
                                PropertyFactory.textHaloColor("#FFFFFF"),
                                PropertyFactory.textHaloWidth(2f),
                                PropertyFactory.textRotationAlignment("map"),
                                PropertyFactory.textPitchAlignment("map")
                        );
                mapLibreMap.getStyle().addLayer(labelLayer);
            }

            flightLayerIds.add(sourceId);
            flightLayerIds.add(lineLayerId);
            flightLayerIds.add(labelLayerId);
        } catch (Exception e) {
        }
    }

    // Mostrar diálogo para seleccionar origen y destino para Dijkstra
    private void mostrarDialogoDijkstra() {
        if (selectedAirports.size() < 2) {
            android.widget.Toast.makeText(this, "Necesitas al menos 2 aeropuertos para calcular ruta",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear lista de aeropuertos disponibles (incluyendo inicial)
        List<Aeropuerto> aeropuertosDisponibles = new ArrayList<>(selectedAirports);

        // Agregar aeropuerto inicial
        Aeropuerto aeropuertoInicial = new Aeropuerto("Beijing Daxing International Airport", "PKX", "Beijing", "China", LAT_INICIAL, LNG_INICIAL);
        aeropuertosDisponibles.add(0, aeropuertoInicial);

        // Mostrar diálogo de selección de origen
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Origen para Dijkstra");

        String[] nombres = aeropuertosDisponibles.stream()
                .map(a -> a.getName() + " (" + a.getCode() + ")")
                .toArray(String[]::new);

        builder.setItems(nombres, (dialog, which) -> {
            Aeropuerto origen = aeropuertosDisponibles.get(which);
            mostrarDialogoDestinoParaDijkstra(origen, aeropuertosDisponibles);
        });

        builder.show();
    }

    private void mostrarDialogoDestinoParaDijkstra(Aeropuerto origen, List<Aeropuerto> aeropuertos) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Destino para Dijkstra");

        // Filtrar aeropuertos (quitar el origen)
        List<Aeropuerto> destinos = new ArrayList<>(aeropuertos);
        destinos.remove(origen);

        String[] nombres = destinos.stream()
                .map(a -> a.getName() + " (" + a.getCode() + ")")
                .toArray(String[]::new);

        builder.setItems(nombres, (dialog, which) -> {
            Aeropuerto destino = destinos.get(which);
            ejecutarDijkstra(origen, destino);
        });

        builder.show();
    }

    private void ejecutarDijkstra(Aeropuerto origen, Aeropuerto destino) {
        List<Vuelo> rutaMasCorta = grafo.dijkstra(origen, destino);

        if (rutaMasCorta == null || rutaMasCorta.isEmpty()) {
            android.widget.Toast.makeText(this, "No hay ruta disponible entre estos aeropuertos",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        // Limpiar conexiones actuales
        limpiarConexiones();

        // Dibujar solo la ruta más corta
        double distanciaTotal = 0;
        for (Vuelo vuelo : rutaMasCorta) {
            Marker origenMarker = airportToMarker.get(vuelo.getOrigen());
            Marker destinoMarker = airportToMarker.get(vuelo.getDestino());

            if (origenMarker != null && destinoMarker != null) {
                createFlightConnectionWithVuelo(origenMarker, destinoMarker, vuelo);
                distanciaTotal += vuelo.getDistancia();
            }
        }

        android.widget.Toast.makeText(this,
                String.format("Ruta más corta: %.0f km total (%d vuelos)", distanciaTotal, rutaMasCorta.size()),
                android.widget.Toast.LENGTH_LONG).show();
    }

    private void limpiarConexiones() {
        flightConnections.clear();
        polylineToSourceMarker.clear();
        polylineToDestMarker.clear();

        if (mapLibreMap != null && mapLibreMap.getStyle() != null) {
            for (String layerId : flightLayerIds) {
                try {
                    if (mapLibreMap.getStyle().getLayer(layerId) != null) {
                        mapLibreMap.getStyle().removeLayer(layerId);
                    }
                    if (mapLibreMap.getStyle().getSource(layerId) != null) {
                        mapLibreMap.getStyle().removeSource(layerId);
                    }
                } catch (Exception e) {
                }
            }
        }
        flightLayerIds.clear();
    }

    private void mostrarDialogoReset() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reiniciar Aplicación");
        builder.setMessage("¿Estás seguro de que quieres borrar todos los datos guardados?");

        builder.setPositiveButton("Sí, Reiniciar", (dialog, which) -> {
            reiniciarAplicacion();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.show();
    }

    private void reiniciarAplicacion() {
        dataPersistence.limpiarDatos();

        limpiarConexiones();

        for (Aeropuerto aeropuerto : new ArrayList<>(selectedAirports)) {
            Marker marker = airportToMarker.get(aeropuerto);
            if (marker != null && !marker.equals(initialMarker)) {
                mapLibreMap.removeMarker(marker);
                markerToAirport.remove(marker);
                airportToMarker.remove(aeropuerto);
            }
        }
        selectedAirports.clear();

        grafo = Grafo.getInstance();
        grafo.limpiar();
        for (Aeropuerto aeropuerto : availableAirports) {
            grafo.agregarAeropuerto(aeropuerto);
        }

        Aeropuerto aeropuertoInicial = new Aeropuerto("Beijing Daxing International Airport", "PKX", "Beijing", "China", LAT_INICIAL, LNG_INICIAL);
        grafo.agregarAeropuerto(aeropuertoInicial);

        if (initialMarker != null) {
            markerToAirport.put(initialMarker, aeropuertoInicial);
            airportToMarker.put(aeropuertoInicial, initialMarker);
        }

        layerCounter = 0;

        android.widget.Toast.makeText(this, "Aplicación reiniciada", android.widget.Toast.LENGTH_SHORT).show();
    }

    // Métodos de persistencia de datos
    private void guardarDatos() {
        dataPersistence.guardarAeropuertos(selectedAirports);
        dataPersistence.guardarVuelos(grafo.listarVuelos());
    }

    // Método original para cargar solo datos (sin elementos visuales)
    private void cargarDatosPersistidos() {
        if (!dataPersistence.hayDatosGuardados()) {
            Log.d(TAG, "No hay datos guardados para cargar");
            return;
        }

        // Cargar aeropuertos seleccionados
        List<Aeropuerto> aeropuertosGuardados = dataPersistence.cargarAeropuertos();
        for (Aeropuerto aeropuerto : aeropuertosGuardados) {
            selectedAirports.add(aeropuerto);
            grafo.agregarAeropuerto(aeropuerto);
        }

        // Cargar vuelos
        List<Vuelo> vuelosGuardados = dataPersistence.cargarVuelos();
        for (Vuelo vuelo : vuelosGuardados) {
            grafo.agregarVuelo(vuelo);
        }
    }

    // Método completo para cargar datos y restaurar elementos visuales
    private void cargarDatosPersistidosCompleto() {
        if (!dataPersistence.hayDatosGuardados()) {
            Log.d(TAG, "No hay datos guardados para cargar");
            return;
        }

        // Cargar aeropuertos seleccionados y crear marcadores
        List<Aeropuerto> aeropuertosGuardados = dataPersistence.cargarAeropuertos();
        for (Aeropuerto aeropuerto : aeropuertosGuardados) {
            selectedAirports.add(aeropuerto);
            grafo.agregarAeropuerto(aeropuerto);

            // Crear marcador visual para el aeropuerto
            if (mapLibreMap != null) {
                Marker marker = mapLibreMap.addMarker(new MarkerOptions()
                        .position(new LatLng(aeropuerto.getLatitude(), aeropuerto.getLongitude()))
                        .title(aeropuerto.getName())
                        .snippet(aeropuerto.getCode() + " - " + aeropuerto.getLocation()));

                // Guardar relaciones entre marcador y aeropuerto
                markerToAirport.put(marker, aeropuerto);
                airportToMarker.put(aeropuerto, marker);
            }
        }

        // Cargar vuelos y crear conexiones visuales
        List<Vuelo> vuelosGuardados = dataPersistence.cargarVuelos();
        for (Vuelo vuelo : vuelosGuardados) {
            grafo.agregarVuelo(vuelo);

            // Crear conexión visual para el vuelo
            Marker origenMarker = airportToMarker.get(vuelo.getOrigen());
            Marker destinoMarker = airportToMarker.get(vuelo.getDestino());

            // Si no encontramos el marcador, podría ser el aeropuerto inicial
            if (origenMarker == null && vuelo.getOrigen().getCode().equals("PKX")) {
                origenMarker = initialMarker;
            }
            if (destinoMarker == null && vuelo.getDestino().getCode().equals("PKX")) {
                destinoMarker = initialMarker;
            }

            if (origenMarker != null && destinoMarker != null) {
                createFlightConnectionWithVuelo(origenMarker, destinoMarker, vuelo);
            }
        }

        updateMapBounds();
    }

    // Sobrescribir onAirportSelected para guardar datos
    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        // Guardar datos al pausar la aplicación
        guardarDatos();
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
    protected void onStop() {
        super.onStop();
        if (mapView != null) {
            mapView.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Guardar datos antes de destruir la actividad
        guardarDatos();
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
