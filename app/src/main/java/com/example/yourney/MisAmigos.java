package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class MisAmigos extends AppCompatActivity implements ElAdaptadorRecyclerAmigos.RecyclerItemClick,SearchView.OnQueryTextListener {
    SearchView buscadorUsuarios;
    RecyclerView lalista;
    private List<ItemListAmigo> items = new ArrayList<ItemListAmigo>();
    ElAdaptadorRecyclerAmigos adapter = new ElAdaptadorRecyclerAmigos(items, MisAmigos.this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_amigos);

        buscadorUsuarios = findViewById(R.id.search_friends);
        lalista = findViewById(R.id.elreciclerview);

        LinearLayoutManager manager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lalista.setLayoutManager(manager);

        // OBTENGO EL USUARIO ACTUAL
        Sesion sesion = new Sesion(this);
        String username = sesion.getUsername();

        ArrayList<String> amigos = new ArrayList<String>();

        // CONSULTAMOS A LA BD POR AMIGOS
        Data datos = new Data.Builder()
                .putString("accion", "select")
                .putString("consulta", "Amigos")
                .putString("username", username)
                .build();

        OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datos)
                .build();

        WorkManager.getInstance(MisAmigos.this).getWorkInfoByIdLiveData(select.getId()).observe(MisAmigos.this, new Observer<WorkInfo>() {
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
                                amigos.add((String) row.get("Username2"));
                                i++;
                            }

                            items = getItems(amigos);
                            adapter = new ElAdaptadorRecyclerAmigos(items,MisAmigos.this);
                            lalista.setAdapter(adapter);

                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        WorkManager.getInstance(MisAmigos.this).enqueue(select);

        Log.d("DAS", String.valueOf(items));
        adapter = new ElAdaptadorRecyclerAmigos(items, MisAmigos.this);
        lalista.setAdapter(adapter);
        buscadorUsuarios.setOnQueryTextListener(MisAmigos.this);
    }

    /** Metodo para probar si funciona el reciclerView, luego se cambiara por una llamada a la BD **/
    private List<ItemListAmigo> getItems(ArrayList<String> amigos) {
        List<ItemListAmigo> itemListAmigos = new ArrayList<>();
        for (String amigo : amigos) {
            ItemListAmigo amigoAct = new ItemListAmigo(amigo, "prueba", R.drawable.fotoruta);
            itemListAmigos.add(amigoAct);
        }
        return itemListAmigos;
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