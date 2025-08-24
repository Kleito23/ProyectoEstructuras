package ec.edu.espol.proyectoestructurag3;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class EstadisticaActivity extends AppCompatActivity {

    private static final String TAG = "StatisticsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);


        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setSelectedItemId(R.id.nav_statistics);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_flights) {
                startActivity(new Intent(this, MapsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_statistics) {
                return true;
            }
            return false;
        });
    }
}
