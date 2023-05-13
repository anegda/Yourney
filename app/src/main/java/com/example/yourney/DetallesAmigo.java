package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class DetallesAmigo extends AppCompatActivity {
    private ImageButton btnFavoritos;
    private ImageView detallesImagen;
    private TextView detallesTitulo;
    private TextView detallesDescripcion;
    private ItemListAmigo detallesItem;
    private boolean esFavorito = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalles_amigo);setTitle(getClass().getSimpleName());

        // Inicialización
        btnFavoritos = findViewById(R.id.btnAgregar);
        detallesImagen = findViewById(R.id.imgItemDetail);
        detallesTitulo = findViewById(R.id.tvTituloDetail);
        detallesDescripcion = findViewById(R.id.tvDescripcionDetail);

        // Conseguimmos los datos de la ruta seleccionada
        detallesItem = (ItemListAmigo) getIntent().getExtras().getSerializable("itemDetail");

        // Colocamos los datos en el layout
        detallesImagen.setImageBitmap(detallesItem.getFotoDePerfil());
        detallesTitulo.setText(detallesItem.getUsername());
        detallesDescripcion.setText(detallesItem.getNombre());

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