package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.simple.ItemList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

public class DetallesRuta extends AppCompatActivity {
    private ImageButton btnFavoritos;
    private ImageView detallesImagen;
    private TextView detallesTitulo;
    private TextView detallesDescripcion;
    private ItemListRuta detallesItem;
    private boolean esFavorito = false;
    String idRuta;

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
        setContentView(R.layout.activity_detalles_ruta);
        setTitle(getClass().getSimpleName());

        // Inicialización
        btnFavoritos = findViewById(R.id.btnFav);
        detallesImagen = findViewById(R.id.imgItemDetail);
        detallesTitulo = findViewById(R.id.tvTituloDetail);
        detallesDescripcion = findViewById(R.id.tvDescripcionDetail);

        // Conseguimmos los datos de la ruta seleccionada
        detallesItem = (ItemListRuta) getIntent().getExtras().getSerializable("itemDetail");

        //Log.d("DESTALLES ITEM", detallesItem.getId());

        byte [] encodeByte = Base64.decode(detallesItem.getImgResource(), Base64.DEFAULT);
        InputStream inputStream  = new ByteArrayInputStream(encodeByte);
        Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);

        // Colocamos los datos en el layout
        detallesImagen.setImageBitmap(bitmap);
        detallesTitulo.setText(detallesItem.getTitulo());
        detallesDescripcion.setText(detallesItem.getDescripcion());

        Sesion sesion = new Sesion(this);

        ////////////////////////////////////////////////////////
        Data datos = new Data.Builder()
                .putString("accion", "selectRuta")
                .putString("consulta", "RutasGuardadas2")
                .putString("username", sesion.getUsername())
                .build();

        OneTimeWorkRequest selectRutaGuardada = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datos)
                .build();

        WorkManager.getInstance(DetallesRuta.this).getWorkInfoByIdLiveData(selectRutaGuardada.getId()).observe(DetallesRuta.this, new Observer<WorkInfo>() {
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
                                idRuta = row.get("idRuta").toString();
                                if(idRuta.equals(detallesItem.getId())){
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
        WorkManager.getInstance(DetallesRuta.this).enqueue(selectRutaGuardada);
        ////////////////////////////////////////////////////////

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
                            .putInt("idRuta", Integer.parseInt(detallesItem.getId()))
                            .putString("username", sesion.getUsername())
                            .build();

                    OneTimeWorkRequest insertRutaGuardada = new OneTimeWorkRequest.Builder(ConexionBD.class)
                            .setInputData(datos)
                            .build();

                    WorkManager.getInstance(DetallesRuta.this).getWorkInfoByIdLiveData(insertRutaGuardada.getId()).observe(DetallesRuta.this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if (workInfo != null && workInfo.getState().isFinished()) {
                            }
                        }
                    });
                    WorkManager.getInstance(DetallesRuta.this).enqueue(insertRutaGuardada);

                } else {
                    // Eliminamos la ruta de la lista de rutas favoritas
                    Data datos = new Data.Builder()
                            .putString("accion", "delete")
                            .putString("consulta", "RutasGuardadas")
                            .putInt("idRuta", Integer.parseInt(detallesItem.getId()))
                            .putString("username", sesion.getUsername())
                            .build();

                    OneTimeWorkRequest deleteRutaGuardada = new OneTimeWorkRequest.Builder(ConexionBD.class)
                            .setInputData(datos)
                            .build();

                    WorkManager.getInstance(DetallesRuta.this).getWorkInfoByIdLiveData(deleteRutaGuardada.getId()).observe(DetallesRuta.this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if (workInfo != null && workInfo.getState().isFinished()) {
                            }
                        }
                    });
                    WorkManager.getInstance(DetallesRuta.this).enqueue(deleteRutaGuardada);
                }
            }
        });
    }
}