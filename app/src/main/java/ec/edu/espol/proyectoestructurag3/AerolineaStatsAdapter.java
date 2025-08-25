package ec.edu.espol.proyectoestructurag3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AerolineaStatsAdapter extends RecyclerView.Adapter<AerolineaStatsAdapter.AerolineaStatsViewHolder> {

    private List<Aerolinea> aerolineas;
    private List<Vuelo> vuelos;

    public AerolineaStatsAdapter(List<Aerolinea> aerolineas, List<Vuelo> vuelos) {
        this.aerolineas = aerolineas;
        this.vuelos = vuelos;
    }

    @NonNull
    @Override
    public AerolineaStatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_aerolinea_stats, parent, false);
        return new AerolineaStatsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AerolineaStatsViewHolder holder, int position) {
        Aerolinea aerolinea = aerolineas.get(position);
        holder.bind(aerolinea, contarVuelosDeAerolinea(aerolinea));
    }

    @Override
    public int getItemCount() {
        return aerolineas.size();
    }

    private int contarVuelosDeAerolinea(Aerolinea aerolinea) {
        int count = 0;
        for (Vuelo vuelo : vuelos) {
            if (vuelo.getAerolinea().equals(aerolinea)) {
                count++;
            }
        }
        return count;
    }

    static class AerolineaStatsViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAerolineaNombre;
        private TextView tvAerolineaCosto;
        private TextView tvAerolineaTiempo;
        private TextView tvAerolineaVuelos;

        public AerolineaStatsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAerolineaNombre = itemView.findViewById(R.id.tvAerolineaNombre);
            tvAerolineaCosto = itemView.findViewById(R.id.tvAerolineaCosto);
            tvAerolineaTiempo = itemView.findViewById(R.id.tvAerolineaTiempo);
            tvAerolineaVuelos = itemView.findViewById(R.id.tvAerolineaVuelos);
        }

        public void bind(Aerolinea aerolinea, int cantidadVuelos) {
            tvAerolineaNombre.setText(aerolinea.getNombre() + " (" + aerolinea.getCodigo() + ")");
            tvAerolineaCosto.setText(String.format("$%.0f promedio", aerolinea.getCostoPromedio()));
            tvAerolineaTiempo.setText(String.format("%.1f hrs promedio", aerolinea.getTiempoPromedio()));
            tvAerolineaVuelos.setText(cantidadVuelos + " vuelos activos");
        }
    }
}
