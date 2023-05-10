package com.example.yourney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ElAdaptadorRecycler.RecyclerItemClick {

    public ActionBarDrawerToggle  actionBarDrawerToggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //AÑADIMOS EL ACTION BAR Y EL NAVIGATIONDRAWER
        setSupportActionBar(findViewById(R.id.labarra));

        final DrawerLayout elmenudesplegable = findViewById(R.id.drawer_layout);
        NavigationView elnavigation = findViewById(R.id.elnavigationview);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, elmenudesplegable, R.string.nav_open, R.string.nav_close);
        elmenudesplegable.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView lalista= findViewById(R.id.elreciclerview);

        //las imagenes deben ser cuadradas
        String [] nombreImagenes = {"fotoruta","fotoruta2","fotoruta3"};

        // unos 15 caracteres max
        String [] nombreRutas = {"Monte Aventura","Bicicleta Salvaje","Cascada Misteriosa"};

        // descripciones de unas 25 palabras (180 caracteres aprox)
        String [] descrRutas = {"Una emocionante caminata de día completo a través de los senderos del monte, con impresionantes vistas panorámicas y desafiantes ascensos.",
                "Una emocionante ruta de mountain bike de medio día a través de bosques y senderos sinuosos, con saltos y obstáculos desafiantes.",
                "Una relajante caminata de medio día a través de un hermoso bosque y un río cristalino, hasta llegar a una impresionante cascada rodeada de un ambiente natural y tranquilo."};

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