package com.example.yourney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ElAdaptadorRecycler.RecyclerItemClick {

    public ActionBarDrawerToggle  actionBarDrawerToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView lalista= findViewById(R.id.elreciclerview);

        //las imagenes deben ser cuadradas
        String [] nombreImagenes = {"fotoruta","fotoruta2","fotoruta3"};

        // unos 15 caracteres max
        String [] nombreRutas = {"Monte Aventura","Bicicleta Salvaje","Cascada Misteriosa"};

        // descripciones de unas 25 palabras (180 caracteres aprox)
        String [] descrRutas = {"Una emocionante caminata de día completo a través de los senderos del monte, con impresionantes vistas panorámicas y desafiantes ascensos.",
                "Una emocionante ruta de mountain bike de medio día a través de bosques y senderos sinuosos, con saltos y obstáculos desafiantes.",
                "Una relajante caminata de medio día a través de un hermoso bosque y un río cristalino, hasta llegar a una impresionante cascada rodeada de un ambiente natural y tranquilo."};

        // Consulto la BD por mis rutas
        Sesion sesion = new Sesion(this);

        Data datos = new Data.Builder()
                .putString("accion", "selectRuta")
                .putString("consulta", "MisRutas")
                .putString("username", sesion.getUsername())
                .build();

        OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datos)
                .build();

        WorkManager.getInstance(MainActivity.this).getWorkInfoByIdLiveData(select.getId()).observe(MainActivity.this, new Observer<WorkInfo>() {
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
        WorkManager.getInstance(MainActivity.this).enqueue(select);

        ArrayList<Integer> imagenes = new ArrayList<Integer>();
        for (String nombreImagen : nombreImagenes) {
            int id = getResources().getIdentifier(nombreImagen, "drawable", getPackageName());
            imagenes.add(id);
        }
        List<ItemListRuta> items = getItems();
        ElAdaptadorRecycler eladaptador = new ElAdaptadorRecycler(items, this);
        lalista.setAdapter(eladaptador);

        /** definir el aspecto del RecyclerView --> horizontal, vertical, grid... **/
        LinearLayoutManager elLayoutLineal= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lalista.setLayoutManager(elLayoutLineal);
    }

    private List<ItemListRuta> getItems() {
        List<ItemListRuta> itemListRutas = new ArrayList<>();
        itemListRutas.add(new ItemListRuta("Monte Aventura", "Una emocionante caminata de día completo a través de los senderos del monte, con impresionantes vistas panorámicas y desafiantes ascensos.", R.drawable.fotoruta));
        itemListRutas.add(new ItemListRuta("Bicicleta Salvaje", "Una emocionante ruta de mountain bike de medio día a través de bosques y senderos sinuosos, con saltos y obstáculos desafiantes.", R.drawable.fotoruta2));
        itemListRutas.add(new ItemListRuta("Cascada Misteriosa", "Una relajante caminata de medio día a través de un hermoso bosque y un río cristalino, hasta llegar a una impresionante cascada rodeada de un ambiente natural y tranquilo.", R.drawable.fotoruta3));
        return itemListRutas;
    }

    @Override
    public void itemClick(ItemListRuta item) {
        Intent intent = new Intent(this, DetallesRuta.class);
        intent.putExtra("itemDetail", item);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}