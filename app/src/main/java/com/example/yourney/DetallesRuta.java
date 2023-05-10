package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.simple.ItemList;

public class DetallesRuta extends AppCompatActivity {
    private ImageButton btnFavoritos;
    private ImageView detallesImagen;
    private TextView detallesTitulo;
    private TextView detallesDescripcion;
    private ItemListRuta detallesItem;
    private boolean esFavorito = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_ruta);
        setTitle(getClass().getSimpleName());

        // Inicialización
        btnFavoritos = findViewById(R.id.btnFav);
        detallesImagen = findViewById(R.id.imgItemDetail);
        detallesTitulo = findViewById(R.id.tvTituloDetail);
        detallesDescripcion = findViewById(R.id.tvDescripcionDetail);

        // Conseguimmos los datos de la ruta seleccionada
        detallesItem = (ItemListRuta) getIntent().getExtras().getSerializable("itemDetail");

        // Colocamos los datos en el layout
        detallesImagen.setImageResource(detallesItem.getImgResource());
        detallesTitulo.setText(detallesItem.getTitulo());
        detallesDescripcion.setText(detallesItem.getDescripcion());

        btnFavoritos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int resId = esFavorito ? R.drawable.no_favorito : R.drawable.favorito;
                btnFavoritos.setImageResource(resId);
                esFavorito = !esFavorito;
            }
        });
    }
}