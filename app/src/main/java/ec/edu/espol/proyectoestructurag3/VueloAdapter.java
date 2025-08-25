package ec.edu.espol.proyectoestructurag3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class VueloAdapter extends RecyclerView.Adapter<VueloAdapter.VueloViewHolder> {

    private List<Vuelo> vuelos;
    private OnVueloClickListener listener;

    public interface OnVueloClickListener {
        void onVueloClick(Vuelo vuelo);
    }

    public VueloAdapter(List<Vuelo> vuelos, OnVueloClickListener listener) {
        this.vuelos = vuelos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VueloViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vuelo, parent, false);
        return new VueloViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VueloViewHolder holder, int position) {
        Vuelo vuelo = vuelos.get(position);
        holder.bind(vuelo);
    }

    @Override
    public int getItemCount() {
        return vuelos.size();
    }

    class VueloViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAerolinea;
        private TextView tvRuta;
        private TextView tvCosto;
        private TextView tvTiempo;
        private TextView tvDistancia;

        public VueloViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAerolinea = itemView.findViewById(R.id.tvAerolinea);
            tvRuta = itemView.findViewById(R.id.tvRuta);
            tvCosto = itemView.findViewById(R.id.tvCosto);
            tvTiempo = itemView.findViewById(R.id.tvTiempo);
            tvDistancia = itemView.findViewById(R.id.tvDistancia);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onVueloClick(vuelos.get(position));
                }
            });
        }

        public void bind(Vuelo vuelo) {
            tvAerolinea.setText(vuelo.getAerolinea().getNombre() + " (" + vuelo.getAerolinea().getCodigo() + ")");
            tvRuta.setText(vuelo.getOrigen().getCode() + " → " + vuelo.getDestino().getCode());
            tvCosto.setText(String.format("$%.0f", vuelo.getCosto()));
            tvTiempo.setText(String.format("%.1f hrs", vuelo.getTiempo()));
            tvDistancia.setText(String.format("%.0f km", vuelo.getDistancia()));
        }
    }
}
