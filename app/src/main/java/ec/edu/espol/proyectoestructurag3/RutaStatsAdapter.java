package ec.edu.espol.proyectoestructurag3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RutaStatsAdapter extends RecyclerView.Adapter<RutaStatsAdapter.RutaStatsViewHolder> {

    private List<EstadisticaActivity.RutaStats> rutasStats;

    public RutaStatsAdapter(List<EstadisticaActivity.RutaStats> rutasStats) {
        this.rutasStats = rutasStats;
    }

    @NonNull
    @Override
    public RutaStatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ruta_stats, parent, false);
        return new RutaStatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutaStatsViewHolder holder, int position) {
        EstadisticaActivity.RutaStats rutaStats = rutasStats.get(position);
        holder.bind(rutaStats);
    }

    @Override
    public int getItemCount() {
        return rutasStats.size();
    }

    static class RutaStatsViewHolder extends RecyclerView.ViewHolder {
        private TextView tvRutaNombre;
        private TextView tvRutaDistancia;
        private TextView tvRutaVuelos;
        private TextView tvRutaCosto;
        private TextView tvRutaTiempo;
        private TextView tvRutaAerolineas;

        public RutaStatsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRutaNombre = itemView.findViewById(R.id.tvRutaNombre);
            tvRutaDistancia = itemView.findViewById(R.id.tvRutaDistancia);
            tvRutaVuelos = itemView.findViewById(R.id.tvRutaVuelos);
            tvRutaCosto = itemView.findViewById(R.id.tvRutaCosto);
            tvRutaTiempo = itemView.findViewById(R.id.tvRutaTiempo);
            tvRutaAerolineas = itemView.findViewById(R.id.tvRutaAerolineas);
        }

        public void bind(EstadisticaActivity.RutaStats rutaStats) {
            tvRutaNombre.setText(rutaStats.getRuta());
            tvRutaDistancia.setText(String.format("%.0f km", rutaStats.getDistancia()));
            tvRutaVuelos.setText(rutaStats.getCantidadVuelos() + " vuelos");
            tvRutaCosto.setText(String.format("$%.0f promedio", rutaStats.getCostoPromedio()));
            tvRutaTiempo.setText(String.format("%.1f hrs promedio", rutaStats.getTiempoPromedio()));

            StringBuilder aerolineas = new StringBuilder();
            for (String aerolinea : rutaStats.getAerolineas()) {
                if (aerolineas.length() > 0) aerolineas.append(", ");
                aerolineas.append(aerolinea);
            }
            tvRutaAerolineas.setText("Aerolíneas: " + aerolineas.toString());
        }
    }
}
