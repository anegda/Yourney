package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

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

        //BOTÓN DE DESCARGAS
        ImageView descargar = findViewById(R.id.btnDescargar);
        descargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int i = 0;
                for(String blob : imageBlobs){
                    //CONVERTIMOS LOS STRINGS A BITMAP
                    byte[] encodeByte = Base64.getDecoder().decode(blob);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

                    //COGEMOS LA FECHA DE HOY PARA EL NOMBRE DEL ARCHIVO
                    Date date = Calendar.getInstance().getTime();
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd_hh.mm.ss");
                    String strDate = dateFormat.format(date);

                    //CREAMOS EL ARCHIVO EN NUESTRO DISPOSITIVO
                    File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    File file = new File(storageLoc, "nombreRuta"+strDate+i+".jpg");

                    try{
                        FileOutputStream fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();
                        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        scanIntent.setData(Uri.fromFile(file));
                        GaleriaFotosRuta.this.sendBroadcast(scanIntent);
                        Toast.makeText(GaleriaFotosRuta.this, getString(R.string.descargaCorrecta), Toast.LENGTH_SHORT);

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(GaleriaFotosRuta.this, "ERROR", Toast.LENGTH_SHORT);
                    }
                    i=i+1;
                }
            }
        });

        //BOTÓN DE ANADIR IMAGEN
        ImageView btn_anadir = findViewById(R.id.btnAdd);
        btn_anadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //ABRIMOS EL INTENT DE CAMARA Y GALERÍA
                try{
                    Intent i1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    Intent i2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    Intent chooser = new Intent(Intent.ACTION_CHOOSER);
                    chooser.putExtra(Intent.EXTRA_INTENT, i1);

                    Intent[] intentArray = { i2 };
                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                    startActivityForResult(chooser, 1); //ESTA DEPRECATED PERO FUNCIONA
                }catch (Exception e){
                    Log.d("DAS","Error la imagen no se carga correctamente");
                }
            }
        });
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

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                if(imageUri==null){
                    Bitmap img = (Bitmap) data.getExtras().get("data");

                    //AÑADIMOS AL ARRAY Y NOTIFICAMOS
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    img.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] b = baos.toByteArray();
                    String fotoNueva = Base64.getEncoder().encodeToString(b);

                    //TODO: LLAMADA A LA BD PARA SUBIR LA IMAGEN

                    imageBlobs.add(fotoNueva);
                    imageRVAdapter.notifyDataSetChanged();
                }else {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap img = BitmapFactory.decodeStream(imageStream);

                    //AÑADIMOS AL ARRAY Y NOTIFICAMOS
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    img.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] b = baos.toByteArray();
                    String fotoNueva = Base64.getEncoder().encodeToString(b);

                    //TODO: LLAMADA A LA BD PARA SUBIR LA IMAGEN

                    imageBlobs.add(fotoNueva);
                    imageRVAdapter.notifyDataSetChanged();

                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(GaleriaFotosRuta.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(GaleriaFotosRuta.this, "ERROR",Toast.LENGTH_SHORT).show();
        }
    }
}