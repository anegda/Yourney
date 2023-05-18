package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;


public class PublicRoutesActivity extends AppCompatActivity implements ElAdaptadorRecycler.RecyclerItemClick,SearchView.OnQueryTextListener {
    SearchView buscadorRutas;
    RecyclerView lalista;
    ElAdaptadorRecycler adapter;
    List<ItemListRuta> items = new ArrayList<>();

    String titulo, descripcion, imagen = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_routes);

        buscadorRutas = findViewById(R.id.search_view);
        lalista = findViewById(R.id.elreciclerview);

        LinearLayoutManager manager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lalista.setLayoutManager(manager);

        buscadorRutas.setOnQueryTextListener(this);

        // Llamada al AsyncTask
        Sesion sesion = new Sesion(this);
        String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selectRutas.php";
        String params = "?consulta=RutasPublicas&username=" + sesion.getUsername();
        System.out.println(url + params);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.elreciclerview);
        TaskGetRutasPublicas taskGetRutasPublicas = new TaskGetRutasPublicas(url + params, recyclerView, PublicRoutesActivity.this);
        taskGetRutasPublicas.execute();

        /**
        // Consulto la BD por las rutas publicas
        Data datos = new Data.Builder()
                .putString("accion", "selectRuta")
                .putString("consulta", "RutasPublicas")
                .build();

        OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datos)
                .build();

        System.out.println("Public Routes");

        WorkManager.getInstance(PublicRoutesActivity.this).getWorkInfoByIdLiveData(select.getId()).observe(PublicRoutesActivity.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo != null && workInfo.getState().isFinished()) {
                    Data output = workInfo.getOutputData();
                    if (!output.getString("resultado").equals("Sin resultado")) {
                        JSONParser parser = new JSONParser();
                        try {
                            // Obtengo la informacion de las rutas devueltas
                            JSONArray jsonResultado =(JSONArray) parser.parse(output.getString("resultado"));

                            Integer i = 0;
                            System.out.println("***** " + jsonResultado + " *****");
                            while (i < jsonResultado.size()) {
                                JSONObject row = (JSONObject) jsonResultado.get(i);
                                System.out.println("***** " + row + " *****");
                                // Vuelco la informacion en las variables creadas anteriormente

                                titulo = (String) row.get("Nombre");
                                descripcion = (String) row.get("Descripcion");
                                imagen = (String) row.get("FotoDesc");
                                getItems(titulo, descripcion, imagen);
                                i++;
                            }

                            adapter = new ElAdaptadorRecycler(items, PublicRoutesActivity.this);
                            lalista.setAdapter(adapter);

                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        WorkManager.getInstance(PublicRoutesActivity.this).enqueue(select);
        **/

    }

    /** Metodo para probar si funciona el reciclerView, luego se cambiara por una llamada a la BD **/
    private void getItems(String titulo, String descripcion, String imagen) {
        items.add(new ItemListRuta(titulo, descripcion, imagen));
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
        Intent intent = new Intent(this, DetallesRuta.class);
        intent.putExtra("itemDetail", item);
        startActivity(intent);
    }
}