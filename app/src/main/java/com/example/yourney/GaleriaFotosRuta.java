package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;

public class GaleriaFotosRuta extends AppCompatActivity {
    private ArrayList<String> imageBlobs;
    private RecyclerView imagesRV;
    private RecyclerViewAdapter imageRVAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galeria_fotos_ruta);

        //OBTENEMOS TODAS LAS IMAGENES DE LA BD
        imageBlobs = new ArrayList<>();

        //CREAMOS EL LAYOUT
        imagesRV = findViewById(R.id.imgRV);
        prepareRecyclerView();
    }

    private void prepareRecyclerView() {
        imageRVAdapter = new RecyclerViewAdapter(GaleriaFotosRuta.this, imageBlobs);
        GridLayoutManager manager = new GridLayoutManager(GaleriaFotosRuta.this, 4);
        imagesRV.setLayoutManager(manager);
        imagesRV.setAdapter(imageRVAdapter);
        getImageBlob();
    }

    private void getImageBlob() {
        //CARGAMOS LAS IMÁGENES
        Bitmap img = BitmapFactory.decodeResource(GaleriaFotosRuta.this.getResources(), R.drawable.fotoruta);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String foto = Base64.getEncoder().encodeToString(b);
        imageBlobs.add(foto);
        imageBlobs.add(foto);
        imageRVAdapter.notifyDataSetChanged();
    }
}