package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.simple.ItemList;

public class DetallesRuta extends AppCompatActivity {
    private ImageView detallesImagen;
    private TextView detallesTitulo;
    private TextView detallesDescripcion;
    private ItemListRuta detallesItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_ruta);
        setTitle(getClass().getSimpleName());

        // Inicialización
        detallesImagen = findViewById(R.id.imgItemDetail);
        detallesTitulo = findViewById(R.id.tvTituloDetail);
        detallesDescripcion = findViewById(R.id.tvDescripcionDetail);

        // Conseguimmos los datos de la ruta seleccionada
        detallesItem = (ItemListRuta) getIntent().getExtras().getSerializable("itemDetail");

        // Colocamos los datos en el layout
        detallesImagen.setImageResource(detallesItem.getImgResource());
        detallesTitulo.setText(detallesItem.getTitulo());
        detallesDescripcion.setText(detallesItem.getDescripcion());
    }
}