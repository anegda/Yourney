package com.example.yourney;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.yourney.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Base64;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //OBTENEMOS LOS GENERALES DE LA RUTA
        DBHelper GestorBD = new DBHelper(this, "Yourney", null, 1);
        SQLiteDatabase bd = GestorBD.getWritableDatabase();
        String[] campos = new String[] {"nombre", "fotoDesc", "duracion", "distancia", "pasos", "dificultad", "fecha", "visibilidad", "creador"};
        //String[] argumentos = {"0"};
        Cursor c = bd.query("Rutas",campos,null,null, null,null,null);
        c.moveToFirst();
        String titulo = c.getString(0);
        String fotoDesc = c.getString(1);
        float duracion = c.getFloat(2);
        float distancia = c.getFloat(3);
        int pasos = c.getInt(4);
        String dificultad = c.getString(5);
        String fecha = c.getString(6);
        int visibilidad = c.getInt(7);
        String creador = c.getString(8);

        //ESTABLECEMOS EL FORMATO NECESARIO
        long duracionSec = (long) duracion;
        long hours = duracionSec / 3600;
        long minutes = (duracionSec % 3600) / 60;
        long seconds = duracionSec % 60;
        float velocidad = 0;
        if(duracion != 0) {
            velocidad = distancia / (duracion / 3600);
        }
        final String duracionFormato = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        final String distanciaFormato = String.format("%.2f KM", distancia);
        final String velocidadFormato = String.format("%.2f KM/H", velocidad);

        //OBTENEMOS LOS TEXTVIEWS y el IMAGEVIEW
        ImageView fotoDescR = (ImageView) findViewById(R.id.fotoDescR);
        TextView tituloRText = (TextView) findViewById(R.id.tituloRText);
        TextView creadorRText = (TextView) findViewById(R.id.creadorRText);
        TextView fechaRText = (TextView) findViewById(R.id.fechaRText);
        TextView dificultadRText = (TextView) findViewById(R.id.dificultadRText);
        TextView visibilidadRText = (TextView) findViewById(R.id.visibilidadRText);
        TextView duracionRNum = (TextView) findViewById(R.id.duracionRNum);
        TextView distanciaRNum = (TextView) findViewById(R.id.distanciaRNum);
        TextView velocidadRNum = (TextView) findViewById(R.id.velocidadRNum);
        TextView pasosRNum = (TextView) findViewById(R.id.pasosRNum);
        TextView caloriasRNum = (TextView) findViewById(R.id.caloriasRNum);

        //ESTABLECEMOS LOS TEXTS y LA IMAGEN
        if(fotoDesc!=null) {
            byte[] encodeByte = Base64.getDecoder().decode(fotoDesc);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            fotoDescR.setImageBitmap(bitmap);
        }
        tituloRText.setText(titulo);
        creadorRText.setText(creador);
        fechaRText.setText(fecha);
        if (visibilidad==1){
            visibilidadRText.setText(R.string.publico);
        } else{
            visibilidadRText.setText(R.string.privado);
        }
        dificultadRText.setText(dificultad);
        duracionRNum.setText(duracionFormato);
        distanciaRNum.setText(distanciaFormato);
        velocidadRNum.setText(velocidadFormato);
        pasosRNum.setText(String.valueOf(pasos));
        caloriasRNum.setText(String.valueOf(pasos/1000*35));

        //OBTENEMOS EL MAPA Y CREAMOS LA RUTA
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