package ec.edu.espol.proyectoestructurag3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AirportAdapter extends RecyclerView.Adapter<AirportAdapter.AirportViewHolder> {

    private List<Airport> airports;
    private OnAirportClickListener listener;

    public interface OnAirportClickListener {
        void onAirportClick(Airport airport);
    }

    public AirportAdapter(List<Airport> airports, OnAirportClickListener listener) {
        this.airports = airports;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AirportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_airport, parent, false);
        return new AirportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AirportViewHolder holder, int position) {
        Airport airport = airports.get(position);
        holder.bind(airport);
    }

    @Override
    public int getItemCount() {
        return airports.size();
    }

    class AirportViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAirportName;
        private TextView tvAirportCode;
        private TextView tvAirportLocation;

        public AirportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAirportName = itemView.findViewById(R.id.tvAirportName);
            tvAirportCode = itemView.findViewById(R.id.tvAirportCode);
            tvAirportLocation = itemView.findViewById(R.id.tvAirportLocation);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAirportClick(airports.get(position));
                }
            });
        }

        public void bind(Airport airport) {
            tvAirportName.setText(airport.getName());
            tvAirportCode.setText(airport.getCode());
            tvAirportLocation.setText(airport.getLocation());
        }
    }
}
