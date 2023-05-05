package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

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

        ArrayList<Integer> imagenes = new ArrayList<Integer>();
        for (String nombreImagen : nombreImagenes) {
            int id = getResources().getIdentifier(nombreImagen, "drawable", getPackageName());
            imagenes.add(id);
        }

        ElAdaptadorRecycler eladaptador = new ElAdaptadorRecycler(nombreRutas, imagenes.stream().mapToInt(i -> i).toArray(), descrRutas);
        lalista.setAdapter(eladaptador);

        /** definir el aspecto del RecyclerView --> horizontal, vertical, grid... **/
        LinearLayoutManager elLayoutLineal= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lalista.setLayoutManager(elLayoutLineal);
    }
}