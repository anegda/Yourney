package com.example.yourney;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class VerRuta extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    int idRuta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        idRuta = getIntent().getIntExtra("idRuta",0);

        //OBTENEMOS LOS GENERALES DE LA RUTA
        DBHelper GestorBD = new DBHelper(this, "Yourney", null, 1);
        SQLiteDatabase bd = GestorBD.getWritableDatabase();
        String[] campos = new String[] {"nombre", "descripcion", "fotoDesc", "duracion", "distancia", "pasos", "dificultad", "fecha", "visibilidad", "creador"};
        String[] argumentos = {String.valueOf(idRuta)};
        Cursor c = bd.query("Rutas",campos,"idRuta=?",argumentos, null,null,null);
        c.moveToFirst();
        String titulo = c.getString(0);
        String descripcion = c.getString(1);
        String fotoDesc = c.getString(2);
        Log.d("DAS",fotoDesc);
        float duracion = c.getFloat(3);
        float distancia = c.getFloat(4);
        int pasos = c.getInt(5);
        String dificultad = c.getString(6);
        String fecha = c.getString(7);
        int visibilidad = c.getInt(8);
        String creador = c.getString(9);

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
        TextView descripcionRText = (TextView) findViewById(R.id.descripcionRText);
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
        descripcionRText.setText(descripcion);
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

        //AÑADIMOS LAS FUNCIONALIDADES A LOS BOTONES
        //BOTÓN PARA DAR VISIBILIDAD A LA RUTA
        Button btn_ruta = (Button) findViewById(R.id.btn_ruta);
        btn_ruta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                getSupportFragmentManager().beginTransaction().show(mapFragment).commit();

                TextView descripcionRuta = findViewById(R.id.descripcionRText);
                descripcionRuta.setVisibility(View.GONE);

                LinearLayout datosNum = findViewById(R.id.datosNumRuta);
                datosNum.setVisibility(View.VISIBLE);
            }
        });

        //BOTÓN PARA ACCEDER A LA GALERÍA DE IMÁGENES
        Button btn_imagenes = (Button) findViewById(R.id.btn_imagenes);
        btn_imagenes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(VerRuta.this, GaleriaFotosRuta.class);
                startActivity(i);
                finish();
            }
        });

        //BOTÓN PARA ACCEDER A LA DESCRIPCIÓN / OTROS
        Button btn_otros = findViewById(R.id.btn_otros);
        btn_otros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                getSupportFragmentManager().beginTransaction().hide(mapFragment).commit();

                TextView descripcionRuta = findViewById(R.id.descripcionRText);
                descripcionRuta.setVisibility(View.VISIBLE);

                LinearLayout datosNum = findViewById(R.id.datosNumRuta);
                datosNum.setVisibility(View.GONE);
            }
        });

        //BOTÓN PARA EDITAR LA INFORMACIÓN DE LA RUTA
        Button btn_editarRuta = findViewById(R.id.btn_editar);
        btn_editarRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(VerRuta.this, GaleriaFotosRuta.class);
                startActivity(i);
                finish();
            }
        });

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