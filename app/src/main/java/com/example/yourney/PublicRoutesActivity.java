package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;

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
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("itemDetail", item);
        startActivity(intent);
    }
}