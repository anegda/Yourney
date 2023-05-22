package com.example.yourney;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class SolicitudesRecibidas extends AppCompatActivity implements ElAdaptadorRecyclerAmigos.RecyclerItemClick, SearchView.OnQueryTextListener {
    SearchView buscadorUsuarios;
    RecyclerView lalista;
    private List<ItemListAmigo> items = new ArrayList<ItemListAmigo>();
    ElAdaptadorRecyclerAmigos adapter = new ElAdaptadorRecyclerAmigos(items, SolicitudesRecibidas.this);
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
        setContentView(R.layout.activity_mis_amigos);

        buscadorUsuarios = findViewById(R.id.search_friends);
        lalista = findViewById(R.id.elreciclerview);

        LinearLayoutManager manager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lalista.setLayoutManager(manager);

        // OBTENGO EL USUARIO ACTUAL
        Sesion sesion = new Sesion(this);
        String username = sesion.getUsername();

        // Llamada al asynctask
        String urlPeticiones = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selects.php";
        String urlUsuarios = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selectUsuarios.php";
        String paramsPeticiones = "?consulta=Peticiones&username=" + username;
        String paramsUsuarios = "?consulta=Usuarios&username=";
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.elreciclerview);
        TaskGetSolicitudesRecibidas taskGetPeticiones = new TaskGetSolicitudesRecibidas(urlPeticiones + paramsPeticiones, urlUsuarios + paramsUsuarios, recyclerView, SolicitudesRecibidas.this, username);
        taskGetPeticiones.execute();

        buscadorUsuarios.setOnQueryTextListener(SolicitudesRecibidas.this);

        Button añadirAmigo = (Button) findViewById(R.id.añadirAmigo);
        añadirAmigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SolicitudesRecibidas.this, EnviarSolicitud.class));
                finish();
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    /** Cada vez que el texto cambia se filtran las busquedas **/
    public boolean onQueryTextChange(String s) {
        adapter.filter(s);
        return false;
    }

    @Override
    public void itemClick(ItemListAmigo item) {
        Intent intent = new Intent(this, DetallesAmigo.class);
        intent.putExtra("itemDetail", item);
        startActivity(intent);
    }
}