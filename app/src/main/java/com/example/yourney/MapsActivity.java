package com.example.yourney;

import androidx.fragment.app.FragmentActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.yourney.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // OBTENEMOS LAS UBICACIONES DE LA RUTA Y REALIZAMOS EL RECORRIDO
        DBHelper GestorBD = new DBHelper(this, "Yourney", null, 1);
        SQLiteDatabase bd = GestorBD.getWritableDatabase();
        String[] campos = new String[] {"idUbi", "idRuta", "altitud", "longitud", "latitud"};
        Cursor c = bd.query("Ubicaciones",campos,null,null, null,null,null);

        PolylineOptions line = new PolylineOptions().clickable(false);
        LatLng firstLoc = null;
        LatLng lastLoc = null;
        while(c.moveToNext()) {
            LatLng loc = new LatLng(c.getDouble(4), c.getDouble(3));
            if(c.isFirst()) {
                firstLoc = loc;
            }
            if(c.isLast()) {
                lastLoc = loc;
            }
            line.add(loc);
        }

        float zoom = 15.0f;
        if(lastLoc != null && firstLoc != null) {
            mMap.addMarker(new MarkerOptions().position(firstLoc).title("Comienzo"));
            mMap.addMarker(new MarkerOptions().position(lastLoc).title("Fin"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLoc, zoom));
        }
        mMap.addPolyline(line);
    }
}