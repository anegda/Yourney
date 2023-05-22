package com.example.yourney;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Base64;
import java.util.Locale;

public class VerRuta extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    int idRuta;
    static String fotoDesc;
    boolean editor;
    private boolean esFavorito = false;
    String idRuta2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Cargo la pagina en el idioma elegido
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Locale nuevaloc;
        if (prefs.getString("idiomaPref", "1").equals("2")) {
            nuevaloc = new Locale("en");
        } else {
            nuevaloc = new Locale("es");
        }

        Locale.setDefault(nuevaloc);
        Configuration configuration = getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);

        Context context = getBaseContext().createConfigurationContext(configuration);
        getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());

        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //OBTENGO LA RUTA ACTUAL
        idRuta = getIntent().getIntExtra("idRuta",0);

        //OBTENEMOS LOS GENERALES DE LA RUTA
        int idRuta = getIntent().getIntExtra("idRuta",0);
        Data datos = new Data.Builder()
                .putString("accion", "selectRuta")
                .putString("consulta", "InfoRuta")
                .putInt("idRuta", idRuta)
                .build();
        OneTimeWorkRequest selectRuta = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(VerRuta.this).getWorkInfoByIdLiveData(selectRuta.getId()).observe(VerRuta.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo != null && workInfo.getState().isFinished()) {
                    Data output = workInfo.getOutputData();
                    if (!output.getString("resultado").equals("Sin resultado")) {
                        try {
                            String titulo = output.getString("Nombre");
                            String descripcion = output.getString("Descripcion");
                            float duracion = output.getFloat("Duracion",0);
                            float distancia = output.getFloat("Distancia",0);
                            int pasos = output.getInt("Pasos",0);
                            String dificultad = output.getString("Dificultad");
                            String fecha = output.getString("Fecha").split("\\s+")[0];
                            int visibilidad = output.getInt("Visibilidad", 0);
                            String creador = output.getString("Creador");

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
                                Drawable d = new BitmapDrawable(getResources(), bitmap);
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
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        WorkManager.getInstance(VerRuta.this).enqueue(selectRuta);


        // OBTENGO EL USUARIO ACTUAL
        Sesion sesion = new Sesion(this);
        String username = sesion.getUsername();

        //DESHABILITAMOS FUNCIONES SI NO ERES EDITOR
        Data datosEditor = new Data.Builder()
                .putString("accion", "select")
                .putString("consulta", "Editores")
                .putInt("idRuta", idRuta)
                .putString("username", username)
                .build();
        OneTimeWorkRequest selectEditor = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datosEditor)
                .build();
        WorkManager.getInstance(VerRuta.this).getWorkInfoByIdLiveData(selectEditor.getId()).observe(VerRuta.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo != null && workInfo.getState().isFinished()) {
                    Data output = workInfo.getOutputData();
                    if (output.getString("resultado").equals("Sin resultado")) {
                        Button btn_editarRuta = findViewById(R.id.btn_editar);
                        btn_editarRuta.setEnabled(false);
                        editor=false;
                    }else{
                        editor=true;
                    }
                }
            }
        });
        WorkManager.getInstance(VerRuta.this).enqueue(selectEditor);


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

                ConstraintLayout datosNum = findViewById(R.id.datosNumRuta);
                datosNum.setVisibility(View.VISIBLE);
            }
        });

        //BOTÓN PARA ACCEDER A LA GALERÍA DE IMÁGENES
        Button btn_imagenes = (Button) findViewById(R.id.btn_imagenes);
        btn_imagenes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(VerRuta.this, GaleriaFotosRuta.class);
                i.putExtra("idRuta", idRuta);
                i.putExtra("editor", editor);
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

                ConstraintLayout datosNum = findViewById(R.id.datosNumRuta);
                datosNum.setVisibility(View.GONE);
            }
        });

        //BOTÓN PARA EDITAR LA INFORMACIÓN DE LA RUTA
        Button btn_editarRuta = findViewById(R.id.btn_editar);
        btn_editarRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(VerRuta.this, EditarRuta.class);
                i.putExtra("idRuta", idRuta);
                startActivity(i);
                finish();
            }
        });

        //OBTENEMOS EL MAPA Y CREAMOS LA RUTA
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ////////////////////////////////////////////////////////
        ImageButton btnFavoritos = (ImageButton) findViewById(R.id.btnFavoritos);

        Data datos2 = new Data.Builder()
                .putString("accion", "selectRuta")
                .putString("consulta", "RutasGuardadas2")
                .putString("username", sesion.getUsername())
                .build();

        OneTimeWorkRequest selectRutaGuardada = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datos2)
                .build();

        WorkManager.getInstance(VerRuta.this).getWorkInfoByIdLiveData(selectRutaGuardada.getId()).observe(VerRuta.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                Log.d("OUTPUT", workInfo.toString());
                if (workInfo != null && workInfo.getState().isFinished()) {
                    Data output = workInfo.getOutputData();
                    Log.d("OUTPUT", output.toString());
                    if (!output.getString("resultado").equals("Sin resultado")) {
                        JSONParser parser = new JSONParser();
                        try {
                            // Obtengo la informacion de las rutas devueltas
                            JSONArray jsonResultado =(JSONArray) parser.parse(output.getString("resultado"));
                            Integer i = 0;
                            while (i < jsonResultado.size()) {
                                JSONObject row = (JSONObject) jsonResultado.get(i);
                                // Vuelco la informacion en las variables creadas anteriormente
                                idRuta2 = row.get("idRuta").toString();
                                if(idRuta2.equals(Integer.toString(idRuta))){
                                    Log.d("ENTRA EN ES FAVORITO", "");
                                    btnFavoritos.setImageResource(R.drawable.favorito);
                                    esFavorito = true;
                                }
                                i++;
                            }
                        } catch (ParseException e) {
                            System.out.print("ERROR AL BUSCAR RUTA GUARDADA");
                        }
                    }
                }
            }
        });
        WorkManager.getInstance(VerRuta.this).enqueue(selectRutaGuardada);
        btnFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int resId = esFavorito ? R.drawable.no_favorito : R.drawable.favorito;
                btnFavoritos.setImageResource(resId);
                esFavorito = !esFavorito;

                if(esFavorito){
                    // Añadimos la ruta a la lista de rutas favoritas
                    Data datos = new Data.Builder()
                            .putString("accion", "insert")
                            .putString("consulta", "RutasGuardadas")
                            .putInt("idRuta", idRuta)
                            .putString("username", sesion.getUsername())
                            .build();

                    OneTimeWorkRequest insertRutaGuardada = new OneTimeWorkRequest.Builder(ConexionBD.class)
                            .setInputData(datos)
                            .build();

                    WorkManager.getInstance(VerRuta.this).getWorkInfoByIdLiveData(insertRutaGuardada.getId()).observe(VerRuta.this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if (workInfo != null && workInfo.getState().isFinished()) {
                            }
                        }
                    });
                    WorkManager.getInstance(VerRuta.this).enqueue(insertRutaGuardada);

                } else {
                    // Eliminamos la ruta de la lista de rutas favoritas
                    Data datos = new Data.Builder()
                            .putString("accion", "delete")
                            .putString("consulta", "RutasGuardadas")
                            .putInt("idRuta", idRuta)
                            .putString("username", sesion.getUsername())
                            .build();

                    OneTimeWorkRequest deleteRutaGuardada = new OneTimeWorkRequest.Builder(ConexionBD.class)
                            .setInputData(datos)
                            .build();

                    WorkManager.getInstance(VerRuta.this).getWorkInfoByIdLiveData(deleteRutaGuardada.getId()).observe(VerRuta.this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if (workInfo != null && workInfo.getState().isFinished()) {
                            }
                        }
                    });
                    WorkManager.getInstance(VerRuta.this).enqueue(deleteRutaGuardada);
                }
            }
        });
        ////////////////////////////////////////////////////////
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // OBTENEMOS LAS UBICACIONES DE LA RUTA Y REALIZAMOS EL RECORRIDO
        int idRuta = getIntent().getIntExtra("idRuta",0);
        Data datos = new Data.Builder()
                .putString("accion", "selectRuta")
                .putString("consulta", "UbisRuta")
                .putInt("idRuta", idRuta)
                .build();
        OneTimeWorkRequest selectRuta = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datos)
                .build();
        WorkManager.getInstance(VerRuta.this).getWorkInfoByIdLiveData(selectRuta.getId()).observe(VerRuta.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo != null && workInfo.getState().isFinished()) {
                    Data output = workInfo.getOutputData();
                    if(!output.getString("resultado").equals("Sin resultado")) {
                        JSONParser parser = new JSONParser();
                        try {
                            PolylineOptions line = new PolylineOptions().clickable(false);
                            LatLng firstLoc = null;
                            LatLng lastLoc = null;

                            JSONArray jsonResultado = (JSONArray) parser.parse(output.getString("resultado"));

                            Integer i = 0;
                            while(i < jsonResultado.size()){
                                JSONObject row = (JSONObject) jsonResultado.get(i);
                                LatLng loc = new LatLng((Double) row.get("Latitud"), (Double) row.get("Longitud"));

                                if(i==0){
                                    firstLoc = loc;
                                }
                                if(i==(jsonResultado.size()-1)){
                                    lastLoc = loc;
                                }
                                line.add(loc);
                                i++;
                            }

                            float zoom = 15.0f;
                            if (lastLoc != null && firstLoc != null) {
                                mMap.addMarker(new MarkerOptions().position(firstLoc).title("Comienzo"));
                                mMap.addMarker(new MarkerOptions().position(lastLoc).title("Fin"));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLoc, zoom));
                            }
                            mMap.addPolyline(line);
                        }catch (ParseException e){
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        WorkManager.getInstance(VerRuta.this).enqueue(selectRuta);
    }
}