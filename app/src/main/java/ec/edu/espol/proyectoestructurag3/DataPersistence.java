package ec.edu.espol.proyectoestructurag3;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DataPersistence {
    private static final String TAG = "DataPersistence";
    private static final String PREFS_NAME = "flight_data";
    private static final String KEY_AEROPUERTOS = "aeropuertos_seleccionados";
    private static final String KEY_VUELOS = "vuelos_creados";

    private Context context;
    private SharedPreferences prefs;

    public DataPersistence(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Guardar aeropuertos seleccionados
    public void guardarAeropuertos(List<Aeropuerto> aeropuertos) {
        try {
            JSONArray jsonArray = new JSONArray();

            for (Aeropuerto aeropuerto : aeropuertos) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("nombre", aeropuerto.getName());
                jsonObject.put("codigo", aeropuerto.getCode());
                jsonObject.put("ciudad", aeropuerto.getCity());
                jsonObject.put("pais", aeropuerto.getCountry());
                jsonObject.put("latitud", aeropuerto.getLatitude());
                jsonObject.put("longitud", aeropuerto.getLongitude());
                jsonArray.put(jsonObject);
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_AEROPUERTOS, jsonArray.toString());
            editor.apply();

            Log.d(TAG, "Aeropuertos guardados: " + aeropuertos.size());

        } catch (JSONException e) {
            Log.e(TAG, "Error guardando aeropuertos: ", e);
        }
    }

    public List<Aeropuerto> cargarAeropuertos() {
        List<Aeropuerto> aeropuertos = new ArrayList<>();

        try {
            String jsonString = prefs.getString(KEY_AEROPUERTOS, "[]");
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String nombre = jsonObject.getString("nombre");
                String codigo = jsonObject.getString("codigo");
                String ciudad = jsonObject.getString("ciudad");
                String pais = jsonObject.getString("pais");
                double latitud = jsonObject.getDouble("latitud");
                double longitud = jsonObject.getDouble("longitud");

                aeropuertos.add(new Aeropuerto(nombre, codigo, ciudad, pais, latitud, longitud));
            }

            Log.d(TAG, "Aeropuertos cargados: " + aeropuertos.size());

        } catch (JSONException e) {
            Log.e(TAG, "Error cargando aeropuertos: ", e);
        }

        return aeropuertos;
    }

    public void guardarVuelos(List<Vuelo> vuelos) {
        try {
            JSONArray jsonArray = new JSONArray();

            for (Vuelo vuelo : vuelos) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", vuelo.getId());
                jsonObject.put("distancia", vuelo.getDistancia());

                // Aeropuerto origen
                JSONObject origenObj = new JSONObject();
                origenObj.put("nombre", vuelo.getOrigen().getName());
                origenObj.put("codigo", vuelo.getOrigen().getCode());
                origenObj.put("ciudad", vuelo.getOrigen().getCity());
                origenObj.put("pais", vuelo.getOrigen().getCountry());
                origenObj.put("latitud", vuelo.getOrigen().getLatitude());
                origenObj.put("longitud", vuelo.getOrigen().getLongitude());
                jsonObject.put("origen", origenObj);

                // Aeropuerto destino
                JSONObject destinoObj = new JSONObject();
                destinoObj.put("nombre", vuelo.getDestino().getName());
                destinoObj.put("codigo", vuelo.getDestino().getCode());
                destinoObj.put("ciudad", vuelo.getDestino().getCity());
                destinoObj.put("pais", vuelo.getDestino().getCountry());
                destinoObj.put("latitud", vuelo.getDestino().getLatitude());
                destinoObj.put("longitud", vuelo.getDestino().getLongitude());
                jsonObject.put("destino", destinoObj);

                // Aerolínea
                JSONObject aerolineaObj = new JSONObject();
                aerolineaObj.put("nombre", vuelo.getAerolinea().getNombre());
                aerolineaObj.put("codigo", vuelo.getAerolinea().getCodigo());
                aerolineaObj.put("costoPromedio", vuelo.getAerolinea().getCostoPromedio());
                aerolineaObj.put("tiempoPromedio", vuelo.getAerolinea().getTiempoPromedio());
                jsonObject.put("aerolinea", aerolineaObj);

                jsonArray.put(jsonObject);
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_VUELOS, jsonArray.toString());
            editor.apply();

            Log.d(TAG, "Vuelos guardados: " + vuelos.size());

        } catch (JSONException e) {
            Log.e(TAG, "Error guardando vuelos: ", e);
        }
    }

    public List<Vuelo> cargarVuelos() {
        List<Vuelo> vuelos = new ArrayList<>();

        try {
            String jsonString = prefs.getString(KEY_VUELOS, "[]");
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                int id = jsonObject.getInt("id");
                double distancia = jsonObject.getDouble("distancia");

                // Reconstruir aeropuerto origen
                JSONObject origenObj = jsonObject.getJSONObject("origen");
                Aeropuerto origen = new Aeropuerto(
                        origenObj.getString("nombre"),
                        origenObj.getString("codigo"),
                        origenObj.getString("ciudad"),
                        origenObj.getString("pais"),
                        origenObj.getDouble("latitud"),
                        origenObj.getDouble("longitud")
                );

                // Reconstruir aeropuerto destino
                JSONObject destinoObj = jsonObject.getJSONObject("destino");
                Aeropuerto destino = new Aeropuerto(
                        destinoObj.getString("nombre"),
                        destinoObj.getString("codigo"),
                        destinoObj.getString("ciudad"),
                        destinoObj.getString("pais"),
                        destinoObj.getDouble("latitud"),
                        destinoObj.getDouble("longitud")
                );

                // Reconstruir aerolínea
                JSONObject aerolineaObj = jsonObject.getJSONObject("aerolinea");
                Aerolinea aerolinea = new Aerolinea(
                        aerolineaObj.getString("nombre"),
                        aerolineaObj.getString("codigo"),
                        aerolineaObj.getDouble("costoPromedio"),
                        aerolineaObj.getDouble("tiempoPromedio")
                );

                vuelos.add(new Vuelo(id, origen, destino, distancia, aerolinea));
            }

            Log.d(TAG, "Vuelos cargados: " + vuelos.size());

        } catch (JSONException e) {
            Log.e(TAG, "Error cargando vuelos: ", e);
        }

        return vuelos;
    }

    public void limpiarDatos() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        Log.d(TAG, "Datos limpiados");
    }

    public boolean hayDatosGuardados() {
        return prefs.contains(KEY_AEROPUERTOS) || prefs.contains(KEY_VUELOS);
    }
}
