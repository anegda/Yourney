package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RutasFavoritas extends AppCompatActivity implements ElAdaptadorRecycler.RecyclerItemClick,SearchView.OnQueryTextListener {
    SearchView buscadorRutas;
    RecyclerView lalista;
    ElAdaptadorRecycler adapter;
    List<ItemListRuta> items = new ArrayList<>();
    String titulo, descripcion, imagen = "";
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
        setContentView(R.layout.activity_rutas_favoritas);

        buscadorRutas = findViewById(R.id.search_view);
        lalista = findViewById(R.id.elreciclerview);

        LinearLayoutManager manager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lalista.setLayoutManager(manager);

        /**
         adapter = new ElAdaptadorRecycler(items, RutasFavoritas.this);
         lalista.setAdapter(adapter);
         **/

        buscadorRutas.setOnQueryTextListener(this);

        // Llamada al AsyncTask
        Sesion sesion = new Sesion(this);
        String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selectRutas.php";
        String params = "?consulta=RutasGuardadas&username=" + sesion.getUsername();
        System.out.println(url + params);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.elreciclerview);
        TaskGetRutasGuardadas taskGetRutasGuardadas = new TaskGetRutasGuardadas(url + params, recyclerView, buscadorRutas, RutasFavoritas.this);
        taskGetRutasGuardadas.execute();
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
    public void itemClick(ItemListRuta item) {
        Intent intent = new Intent(this, VerRuta.class);
        int idRuta = Integer.parseInt(item.getId());
        intent.putExtra("idRuta", idRuta);
        Log.d("DAS", String.valueOf(idRuta));
        startActivity(intent);
    }
}