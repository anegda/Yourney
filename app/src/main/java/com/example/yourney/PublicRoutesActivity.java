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
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;


public class PublicRoutesActivity extends AppCompatActivity implements ElAdaptadorRecycler.RecyclerItemClick,SearchView.OnQueryTextListener {
    SearchView buscadorRutas;
    RecyclerView lalista;
    ElAdaptadorRecycler adapter;
    private List<ItemListRuta> items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_routes);

        buscadorRutas = findViewById(R.id.search_view);
        lalista = findViewById(R.id.elreciclerview);

        LinearLayoutManager manager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lalista.setLayoutManager(manager);

        items = getItems();
        adapter = new ElAdaptadorRecycler(items, PublicRoutesActivity.this);
        lalista.setAdapter(adapter);

        buscadorRutas.setOnQueryTextListener(this);

        String nombreImagenes[] = {};
        String nombreRutas[] = {};
        String descrRutas[] = {};

        // Consulto la BD por las rutas publicas
        Data datos = new Data.Builder()
                .putString("accion", "selectRuta")
                .putString("consulta", "RutasPublicas")
                .build();

        OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datos)
                .build();

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
                            while (i < jsonResultado.size()) {
                                JSONObject row = (JSONObject) jsonResultado.get(i);
                                // Vuelco la informacion en las variables creadas anteriormente
                                nombreImagenes[i] = (String) row.get("ImgBlob");
                                nombreRutas[i] = (String) row.get("Nombre");
                                descrRutas[i] = (String) row.get("Descripcion");
                                i++;
                            }

                        } catch (ParseException | JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        WorkManager.getInstance(PublicRoutesActivity.this).enqueue(select);

    }

    /** Metodo para probar si funciona el reciclerView, luego se cambiara por una llamada a la BD **/
    private List<ItemListRuta> getItems() {
        List<ItemListRuta> itemListRutas = new ArrayList<>();
        itemListRutas.add(new ItemListRuta("Monte Aventura", "Una emocionante caminata de día completo a través de los senderos del monte, con impresionantes vistas panorámicas y desafiantes ascensos.", R.drawable.fotoruta));
        itemListRutas.add(new ItemListRuta("Bicicleta Salvaje", "Una emocionante ruta de mountain bike de medio día a través de bosques y senderos sinuosos, con saltos y obstáculos desafiantes.", R.drawable.fotoruta2));
        itemListRutas.add(new ItemListRuta("Cascada Misteriosa", "Una relajante caminata de medio día a través de un hermoso bosque y un río cristalino, hasta llegar a una impresionante cascada rodeada de un ambiente natural y tranquilo.", R.drawable.fotoruta3));
        return itemListRutas;
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