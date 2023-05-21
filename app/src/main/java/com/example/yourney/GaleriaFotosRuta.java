package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
    static ArrayList<String> imageBlobs;
    static ArrayList<Integer> idImgList;
    private RecyclerView imagesRV;
    static RecyclerViewAdapter imageRVAdapter;
    private Integer idRuta = 1;
    static String fotoNueva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_galeria_fotos_ruta);

        Bundle extras = getIntent().getExtras();
        if (extras != null && idRuta == null) {
            idRuta = extras.getInt("idRuta");
        }

        idImgList = new ArrayList<>();
        imageBlobs = new ArrayList<>();

        //CREAMOS EL LAYOUT
        imagesRV = findViewById(R.id.imgRV);
        prepareRecyclerView();

        //OBTENEMOS TODAS LAS IMAGENES DE LA BD
        String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selectRutas.php";
        String params = "?consulta=ImagenesRuta&idRuta=" + idRuta;
        TaskGetImagenes taskGetImagenes = new TaskGetImagenes(url + params, imagesRV, GaleriaFotosRuta.this);
        taskGetImagenes.execute();

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
                } catch (Exception e){
                    Log.d("DAS","Error la imagen no se carga correctamente");
                }
            }
        });
    }

    private void prepareRecyclerView() {
        imageRVAdapter = new RecyclerViewAdapter(GaleriaFotosRuta.this, idImgList, imageBlobs);
        GridLayoutManager manager = new GridLayoutManager(GaleriaFotosRuta.this, 4);
        imagesRV.setLayoutManager(manager);
        imagesRV.setAdapter(imageRVAdapter);
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
                    // PARA QUE NO EXISTAN PROBLEMAS CON EL TAMAÑO DE LA IMAGEN
                    b = tratarImagen(b);
                    fotoNueva = Base64.getEncoder().encodeToString(b);

                    // Llamada a la BD para subir la imagen
                    Sesion sesion = new Sesion(this);

                    Data datos = new Data.Builder()
                            .putString("accion", "insert")
                            .putString("consulta", "Imagenes")
                            .putInt("idRuta", idRuta)
                            .putString("username", sesion.getUsername())
                            .build();

                    // Peticion al Worker
                    OneTimeWorkRequest insert = new OneTimeWorkRequest.Builder(ConexionBD.class)
                            .setInputData(datos)
                            .build();

                    WorkManager.getInstance(GaleriaFotosRuta.this).getWorkInfoByIdLiveData(insert.getId()).observe(GaleriaFotosRuta.this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if (workInfo != null && workInfo.getState().isFinished()) {
                                System.out.println("Imagen insertada");

                                // Añado el ID de la imagen que acabo de insertar
                                Data datos = new Data.Builder()
                                        .putString("accion", "select")
                                        .putString("consulta", "UltimaImagen")
                                        .putString("username", sesion.getUsername())
                                        .build();

                                // Peticion al Worker
                                OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                        .setInputData(datos)
                                        .build();

                                WorkManager.getInstance(GaleriaFotosRuta.this).getWorkInfoByIdLiveData(select.getId()).observe(GaleriaFotosRuta.this, new Observer<WorkInfo>() {
                                    @Override
                                    public void onChanged(WorkInfo workInfo) {
                                        if (workInfo != null && workInfo.getState().isFinished()) {
                                            Data output = workInfo.getOutputData();
                                            if (output.getString("resultado").equals("Sin resultado")) {
                                                JSONParser parser = new JSONParser();

                                                try {
                                                    JSONArray jsonResultado = (JSONArray) parser.parse(output.getString("resultado"));
                                                    JSONObject row = (JSONObject) jsonResultado.get(0); // Solo hay un elemento

                                                    Integer id = (Integer) row.get("MAX(IdImg)");
                                                    if (id != null) {
                                                        idImgList.add(id);
                                                    }

                                                } catch (ParseException e) {
                                                    throw new RuntimeException(e);
                                                }

                                            }
                                        }
                                    }
                                });
                                WorkManager.getInstance(GaleriaFotosRuta.this).enqueue(select);

                            }
                        }
                    });
                    WorkManager.getInstance(GaleriaFotosRuta.this).enqueue(insert);

                    imageBlobs.add(fotoNueva);
                    imageRVAdapter.notifyDataSetChanged();

                }else {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap img = BitmapFactory.decodeStream(imageStream);

                    //AÑADIMOS AL ARRAY Y NOTIFICAMOS
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    img.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] b = baos.toByteArray();
                    // PARA QUE NO EXISTAN PROBLEMAS CON EL TAMAÑO DE LA IMAGEN
                    b = tratarImagen(b);
                    fotoNueva = Base64.getEncoder().encodeToString(b);

                    //Llamada a la BD para subir la imagen
                    Sesion sesion = new Sesion(this);

                    Data datos = new Data.Builder()
                            .putString("accion", "insert")
                            .putString("consulta", "Imagenes")
                            .putInt("idRuta", idRuta)
                            .putString("username", sesion.getUsername())
                            .build();

                    // Peticion al Worker
                    OneTimeWorkRequest insert = new OneTimeWorkRequest.Builder(ConexionBD.class)
                            .setInputData(datos)
                            .build();

                    WorkManager.getInstance(GaleriaFotosRuta.this).getWorkInfoByIdLiveData(insert.getId()).observe(GaleriaFotosRuta.this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if (workInfo != null && workInfo.getState().isFinished()) {
                                System.out.println("Imagen insertada");

                                // Añado el ID de la imagen que acabo de insertar
                                Data datos = new Data.Builder()
                                        .putString("accion", "select")
                                        .putString("consulta", "UltimaImagen")
                                        .putString("username", sesion.getUsername())
                                        .build();

                                // Peticion al Worker
                                OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                        .setInputData(datos)
                                        .build();

                                WorkManager.getInstance(GaleriaFotosRuta.this).getWorkInfoByIdLiveData(select.getId()).observe(GaleriaFotosRuta.this, new Observer<WorkInfo>() {
                                    @Override
                                    public void onChanged(WorkInfo workInfo) {
                                        if (workInfo != null && workInfo.getState().isFinished()) {
                                            Data output = workInfo.getOutputData();
                                            if (output.getString("resultado").equals("Sin resultado")) {
                                                JSONParser parser = new JSONParser();

                                                try {
                                                    JSONArray jsonResultado = (JSONArray) parser.parse(output.getString("resultado"));
                                                    JSONObject row = (JSONObject) jsonResultado.get(0); // Solo hay un elemento

                                                    String id = (String) row.get("MAX(IdImg)");
                                                    if (id != null) {
                                                        idImgList.add(Integer.parseInt(id));
                                                    }

                                                } catch (ParseException e) {
                                                    throw new RuntimeException(e);
                                                }

                                            }
                                        }
                                    }
                                });
                                WorkManager.getInstance(GaleriaFotosRuta.this).enqueue(select);
                            }
                        }
                    });
                    WorkManager.getInstance(GaleriaFotosRuta.this).enqueue(insert);

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

    protected byte[] tratarImagen(byte[] img){
        /**
         * Basado en el código extraído de Stack Overflow
         * Pregunta: https://stackoverflow.com/questions/57107489/sqliteblobtoobigexception-row-too-big-to-fit-into-cursorwindow-while-writing-to
         * Autor: https://stackoverflow.com/users/3694451/leo-vitor
         * Modificado por Ane García para traducir varios términos y adaptarlo a la aplicación
         */
        Log.d("DAS", String.valueOf(img.length));
        if (img.length > 10000000){
            while(img.length > 50000){
                Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                Bitmap compacto = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth()*0.3), (int)(bitmap.getHeight()*0.3), true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                compacto.compress(Bitmap.CompressFormat.PNG, 100, stream);
                img = stream.toByteArray();
            }
        }else{
            while(img.length > 50000){
                Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
                Bitmap compacto = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth()*0.8), (int)(bitmap.getHeight()*0.8), true);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                compacto.compress(Bitmap.CompressFormat.PNG, 100, stream);
                img = stream.toByteArray();
            }
        }
        return img;
    }
}