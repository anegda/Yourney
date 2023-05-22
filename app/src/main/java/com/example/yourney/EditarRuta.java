package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
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
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Locale;

public class EditarRuta extends AppCompatActivity {

    TextView tituloRuta;
    ImageView fotoDescRuta;
    RadioGroup dificultad, visibilidad;
    EditText informacionExtra;
    ArrayAdapter<String> mAdapter;
    static String fotoDescriptiva;
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

        Locale.setDefault(nuevaloc);
        Configuration configuration = getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);

        Context context = getBaseContext().createConfigurationContext(configuration);
        getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_ruta);

        fotoDescRuta = findViewById(R.id.fotoDescRuta);
        tituloRuta = findViewById(R.id.tituloRutaEdit);
        dificultad = findViewById(R.id.dificultadRutaGroup);
        informacionExtra = findViewById(R.id.otrosMutilineText);
        visibilidad = findViewById(R.id.visibilidadRutaGroup);

        restaurarCampos();

        Button btn_guardar = (Button) findViewById(R.id.btn_guardarDatosRuta);
        ImageView btn_editores = (ImageView) findViewById(R.id.btn_anadirEditores);
        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //COGEMOS LA FOTO DEL IMAGEVIEW
                if (fotoDescriptiva == null) {
                    ImageView fotoDesc = (ImageView) findViewById(R.id.fotoDescRuta);
                    Bitmap img = ((BitmapDrawable) fotoDesc.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    img.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] b = baos.toByteArray();
                    //PARA QUE NO EXISTAN PROBLEMAS CON EL TAMAÑO DE LA IMAGEN
                    b = tratarImagen(b);
                    fotoDescriptiva = Base64.getEncoder().encodeToString(b);
                }

                //COGEMOS LOS DATOS DE LOS CAMPOS
                EditText tituloEdit = findViewById(R.id.tituloRutaEdit);
                String titulo = tituloEdit.getText().toString();

                EditText otrosEdit = findViewById(R.id.otrosMutilineText);
                String descr = otrosEdit.getText().toString();

                RadioGroup dificultadGroup = findViewById(R.id.dificultadRutaGroup);
                int difRadioButtonID = dificultadGroup.getCheckedRadioButtonId();
                RadioButton difRadioButtonSelected = (RadioButton) findViewById(difRadioButtonID);
                String dificultad = difRadioButtonSelected.getText().toString();

                RadioGroup visibilidadGroup = findViewById(R.id.visibilidadRutaGroup);
                int visRadioButtonID = visibilidadGroup.getCheckedRadioButtonId();
                RadioButton visRadioButtonSelected = (RadioButton) findViewById(visRadioButtonID);
                String visibilidadString = visRadioButtonSelected.getText().toString();
                int visibilidad = 1;
                if (visibilidadString.equals(getString(R.string.privado))){
                    visibilidad = 0;
                }

                //REALIZAMOS EL UPDATE
                int idRuta = getIntent().getIntExtra("idRuta",0);
                Data datos = new Data.Builder()
                        .putString("accion", "update")
                        .putString("consulta", "Rutas")
                        .putInt("idRuta", idRuta)
                        .putString("nombre", titulo)
                        .putString("descripcion", descr)
                        .putString("dificultad", dificultad)
                        .putInt("visibilidad", visibilidad)
                        .build();

                OneTimeWorkRequest updateRuta = new OneTimeWorkRequest.Builder(ConexionBD.class)
                        .setInputData(datos)
                        .build();

                WorkManager.getInstance(EditarRuta.this).getWorkInfoByIdLiveData(updateRuta.getId()).observe(EditarRuta.this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        //VOLVEMOS A LA ACTIVIDAD DE VER RUTA
                        Intent i = new Intent(EditarRuta.this, VerRuta.class);
                        i.putExtra("idRuta", idRuta);
                        startActivity(i);
                        finish();
                    }
                });

                WorkManager.getInstance(EditarRuta.this).enqueue(updateRuta);
            }
        });

        btn_editores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EditarRuta.this, MisEditores.class);
                i.putExtra("tituloRuta", tituloRuta.getText().toString());
                i.putExtra("dificultadRuta", dificultad.indexOfChild(dificultad.findViewById(dificultad.getCheckedRadioButtonId())));
                i.putExtra("infoRuta", informacionExtra.getText().toString());
                i.putExtra("visibilidadRuta", visibilidad.indexOfChild(visibilidad.findViewById(visibilidad.getCheckedRadioButtonId())));
                startActivity(i);
                finish();
            }
        });

        // recoger foto descriptiva de galería / cámara
        ImageView fotoDesc = (ImageView) findViewById(R.id.fotoDescRuta);
        fotoDesc.setClickable(true);
        fotoDesc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Intent i1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    Intent i2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    Intent chooser = new Intent(Intent.ACTION_CHOOSER);
                    chooser.putExtra(Intent.EXTRA_INTENT, i1);

                    Intent[] intentArray = { i2 };
                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                    startActivityForResult(chooser, 1); //ESTA DEPRECATED PERO FUNCIONA
                    //ALTERNATIVA A ESTE MÉTODO: https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
                }catch (Exception e){
                    Log.d("DAS","Error la imagen no se carga correctamente");
                }
            }
        });
    }

    //PARA ESTABLECER IMAGEN
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                if(imageUri==null){
                    Bitmap foto = (Bitmap) data.getExtras().get("data");
                    ImageView fotoDesc = (ImageView) findViewById(R.id.fotoDescRuta);
                    fotoDesc.setImageBitmap(foto);
                }else {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    ImageView fotoDesc = (ImageView) findViewById(R.id.fotoDescRuta);
                    fotoDesc.setImageBitmap(selectedImage);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(EditarRuta.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(EditarRuta.this, "ERROR",Toast.LENGTH_SHORT).show();
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

    public void restaurarCampos(){
        ArrayList<String> lista = (ArrayList<String>) getIntent().getSerializableExtra("editores");
        System.out.println("ListaEditores" + lista);

        if(lista != null){
            mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista);
            ListView listView = findViewById(R.id.listEditores);
            listView.setAdapter(mAdapter);
        }

        tituloRuta.setText(getIntent().getStringExtra("tituloRuta"));
        dificultad.check(getIntent().getIntExtra("dificultadRuta", 1));
        informacionExtra.setText(getIntent().getStringExtra("infoRuta"));
        visibilidad.check(getIntent().getIntExtra("visibilidadRuta",  1));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);


        outState.putString("tituloRuta", tituloRuta.getText().toString());
        outState.putInt("dificultad", dificultad.indexOfChild(dificultad.findViewById(dificultad.getCheckedRadioButtonId())));
        outState.putString("infoExtra", informacionExtra.getText().toString());
        outState.putInt("visibilidad", visibilidad.indexOfChild(visibilidad.findViewById(visibilidad.getCheckedRadioButtonId())));
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        tituloRuta.setText(outState.getString("tituloRuta"));
        dificultad.check(outState.getInt("difitultad"));
        informacionExtra.setText(outState.getString("infoExtra"));
        visibilidad.check(outState.getInt("visibilidad"));
    }
}