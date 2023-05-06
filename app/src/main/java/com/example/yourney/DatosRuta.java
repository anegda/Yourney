package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Base64;

public class DatosRuta extends AppCompatActivity {
    static int idRuta;
    static String fotoRuta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_ruta);

        idRuta = getIntent().getIntExtra("idRuta", 0);

        Button btn_guardar = findViewById(R.id.btn_guardarDatosRuta);
        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                if (visibilidadString.equals(R.string.privado)){
                    visibilidad = 0;
                }

                ImageView fotoDescRuta = (ImageView) findViewById(R.id.fotoDescRuta);
                Bitmap img = ((BitmapDrawable) fotoDescRuta.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                img.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();
                fotoRuta = Base64.getEncoder().encodeToString(b);

                //REALIZAMOS EL UPDATE EN LA BASE DE DATOS
                DBHelper GestorBD = new DBHelper(getApplicationContext(), "Yourney", null, 1);
                SQLiteDatabase bd = GestorBD.getWritableDatabase();
                bd.execSQL("UPDATE Rutas set nombre='"+titulo+"', descripcion='"+descr+"', fotoDesc='"+img+"', dificultad='"+dificultad+"', visibilidad="+visibilidad+" WHERE idRuta='"+idRuta+"'");

                //ABRIMOS LA INTERFAZ PARA VISUALIZAR LA RUTA
                startActivity(new Intent(DatosRuta.this, MapsActivity.class).putExtra("idRuta", idRuta));
                finish();
            }
        });

        //MODIFICAR FOTO DE GALERÍA O CAMARA
        ImageView fotoPerfil = (ImageView) findViewById(R.id.fotoDescRuta);
        fotoPerfil.setClickable(true);
        fotoPerfil.setOnClickListener(new View.OnClickListener() {
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
                    ImageView fotoPerfil = (ImageView) findViewById(R.id.fotoDescRuta);
                    fotoPerfil.setImageBitmap(foto);
                }else {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    ImageView fotoPerfil = (ImageView) findViewById(R.id.fotoDescRuta);
                    fotoPerfil.setImageBitmap(selectedImage);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(DatosRuta.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(DatosRuta.this, "ERROR",Toast.LENGTH_SHORT).show();
        }
    }
}

