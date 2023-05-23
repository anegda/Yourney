package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ImagenGaleria extends AppCompatActivity {
    private ScaleGestureDetector scaleGestureDetector;
    private float mScaleFactor = 1.0f;
    private Integer idRuta;
    private boolean editor;
    private String parent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Cargo la pagina en el idioma elegido
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Locale nuevaloc;
        if (prefs.getString("idiomaPref", "1").equals("2")) {
            nuevaloc = new Locale("en");
        } else {
            nuevaloc = new Locale("es");
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null && idRuta == null ) {
            idRuta = extras.getInt("idRuta");
            editor = extras.getBoolean("editor");
            parent = extras.getString("parent");

        }

        System.out.println("############### EDITOR: " + editor);
        System.out.println("############### IDRUTA: " + idRuta);

        Locale.setDefault(nuevaloc);
        Configuration configuration = getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);

        Context context = getBaseContext().createConfigurationContext(configuration);
        getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagen_galeria);

        String imgBlob = RecyclerViewAdapter.fotoElegidaBlob;
        ImageView imgView = findViewById(R.id.imgIV);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        byte[] encodeByte = Base64.getDecoder().decode(imgBlob);
        Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        imgView.setImageBitmap(bitmap);

        if (!editor) {
            CardView cardDescargar = findViewById(R.id.cardView);
            ImageView descargar = findViewById(R.id.btn_descargar_img);
            cardDescargar.setVisibility(View.GONE);
            cardDescargar.setVisibility(View.GONE);
            descargar.setEnabled(false);

            CardView cardEliminar = findViewById(R.id.cardView2);
            ImageView eliminar = findViewById(R.id.btn_eliminar_img);
            cardEliminar.setVisibility(View.GONE);
            eliminar.setVisibility(View.GONE);
            eliminar.setEnabled(false);

        }

        ImageView btn_descargar = findViewById(R.id.btn_descargar_img);
        btn_descargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //CONVERTIMOS EL STRING A BITMAP
                byte[] encodeByte = Base64.getDecoder().decode(imgBlob);
                Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);

                //COGEMOS LA FECHA DE HOY PARA EL NOMBRE DEL ARCHIVO
                Date date = Calendar.getInstance().getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd_hh.mm.ss");
                String strDate = dateFormat.format(date);

                //CREAMOS EL ARCHIVO EN NUESTRO DISPOSITIVO
                File storageLoc = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File file = new File(storageLoc, "nombreRuta"+strDate+".jpg");
                try{
                    FileOutputStream fos = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.close();
                    Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    scanIntent.setData(Uri.fromFile(file));
                    ImagenGaleria.this.sendBroadcast(scanIntent);
                    Toast.makeText(ImagenGaleria.this, getString(R.string.descargaCorrecta), Toast.LENGTH_SHORT);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ImagenGaleria.this, "ERROR", Toast.LENGTH_SHORT);
                }
            }
        });

        ImageView btn_eliminar = findViewById(R.id.btn_eliminar_img);
        btn_eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Borro la imagen de la bd
                Data datos = new Data.Builder()
                        .putString("accion", "delete")
                        .putString("consulta", "Imagenes")
                        .putInt("idImg", RecyclerViewAdapter.idImgElegida)
                        .build();

                // Peticion al Worker
                OneTimeWorkRequest delete = new OneTimeWorkRequest.Builder(ConexionBD.class)
                        .setInputData(datos)
                        .build();

                WorkManager.getInstance(ImagenGaleria.this).getWorkInfoByIdLiveData(delete.getId()).observe(ImagenGaleria.this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            System.out.println("Imagen borrada");

                            //CERRAMOS LA ACTIVIDAD Y VOLVEMOS A LA GALERÍA
                            Intent intent = new Intent(ImagenGaleria.this, GaleriaFotosRuta.class);
                            intent.putExtra("idRuta", idRuta);
                            intent.putExtra("editor", editor);
                            intent.putExtra("parent", parent);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
                WorkManager.getInstance(ImagenGaleria.this).enqueue(delete);
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            ImageView imgView = findViewById(R.id.imgIV);
            imgView.setScaleX(mScaleFactor);
            imgView.setScaleY(mScaleFactor);
            return true;
        }
    }

}