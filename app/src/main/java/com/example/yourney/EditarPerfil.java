package com.example.yourney;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
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
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Base64;

public class EditarPerfil extends AppCompatActivity {

    EditText nombre, apellido, email, password;
    ImageView fotoperfil;
    Button btnGuardar;
    Bitmap bitmap;
    String fotoen64;
    private Bitmap bitmapRedimensionado;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        fotoperfil = findViewById(R.id.fotoPerfilEditarPerfil);
        nombre = findViewById(R.id.nombre);
        apellido = findViewById(R.id.apellido);
        email = findViewById(R.id.email);
        password = findViewById(R.id.contrasenia);

        btnGuardar = findViewById(R.id.btnGuardarEditarUsuario);
        Sesion sesion = new Sesion(this);

        // Comprobar credenciales contra la BD
        Data datos = new Data.Builder()
                .putString("accion", "selectUsuario")
                .putString("consulta", "Usuarios")
                .putString("username", sesion.getUsername())
                .build();

        OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datos)
                .build();

        WorkManager.getInstance(EditarPerfil.this).getWorkInfoByIdLiveData(select.getId()).observe(EditarPerfil.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo != null && workInfo.getState().isFinished()) {
                    Data output = workInfo.getOutputData();
                    if (!output.getString("resultado").equals("Sin resultado")) {
                        JSONParser parser = new JSONParser();
                        try {
                            // Obtengo la informacion de el usuario devuelto
                            nombre.setText(output.getString("nombre"));
                            apellido.setText(output.getString("apellidos"));
                            email.setText(output.getString("email"));

                            String fotoPerfil = (String) output.getString("FotoPerfil");
                            if(fotoPerfil!=null) {
                                byte[] encodeByte = Base64.getDecoder().decode(fotoPerfil);
                                bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        WorkManager.getInstance(EditarPerfil.this).enqueue(select);

        fotoperfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFotopPerfil(view);
            }
        });
    }

    public void update(View v){
        // Validar si el correo electrónico es válido utilizando una expresión regular

        String emailStr = (String) email.getText().toString();
        boolean isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches();

        if (isEmailValid) {
            // obtener bitmap del imageview actual --> comprimirla --> convertirlo en base64 para subirlo a la bbdd
            //if (fotoperfil.getDrawable()!=null) {
            if (fotoen64 != null) {
                Bitmap bitmap = ((BitmapDrawable) fotoperfil.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                fotoen64 = new String(Base64.getEncoder().encode(byteArray));
            }

            System.out.println("******** BOTON PULSADO ********");
            System.out.println(fotoen64);

            Sesion sesion = new Sesion(this);
            Data datos;

            datos = new Data.Builder()
                    .putString("accion", "update")
                    .putString("consulta", "Usuarios")
                    .putString("username", sesion.getUsername())
                    .putString("nombreNew", nombre.getText().toString())
                    .putString("apellidosNew", apellido.getText().toString())
                    .putString("passwordNew", password.getText().toString())
                    .putString("emailNew", email.getText().toString())
                    .build();

            OneTimeWorkRequest update = new OneTimeWorkRequest.Builder(ConexionBD.class)
                    .setInputData(datos)
                    .build();

            System.out.println(datos);

            // Actualizo el ususario en la base de datos
            WorkManager.getInstance(EditarPerfil.this).getWorkInfoByIdLiveData(update.getId()).observe(EditarPerfil.this, new Observer<WorkInfo>() {
                @Override
                public void onChanged(WorkInfo workInfo) {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        Data output = workInfo.getOutputData();
                        if (output.getBoolean("resultado", false)) {
                            System.out.println("***** PASSWORd " + password.getText().toString());
                            System.out.println("***** " + fotoen64);
                            // Paso a la siguiente actividad
                            Toast.makeText(EditarPerfil.this, "Cambios guardados con exito", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditarPerfil.this, MainActivity.class);
                            startActivity(intent);

                            finish();
                        }
                    }
                }
            });
            WorkManager.getInstance(EditarPerfil.this).enqueue(update);

        } else {
            Toast.makeText(this, "Registro incorrecto", Toast.LENGTH_LONG).show();
        }
    }

    public void setFotopPerfil(View v){
        try{
            Intent i1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            Intent i2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            Intent chooser = new Intent(Intent.ACTION_CHOOSER);
            chooser.putExtra(Intent.EXTRA_INTENT, i1);

            Intent[] intentArray = { i2 };
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            startActivityForResult(chooser, PICK_IMAGE_REQUEST); //ESTA DEPRECATED PERO FUNCIONA
            //ALTERNATIVA A ESTE MÉTODO: https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
        }catch (Exception e){
            Log.d("DAS","Error la imagen no se carga correctamente");
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                Bitmap bitmapOriginal = null;
                if(imageUri==null){
                    bitmapOriginal = (Bitmap) data.getExtras().get("data");
                }else {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    bitmapOriginal = BitmapFactory.decodeStream(imageStream);
                }
                //Pasar de bitmap a string y guardar en la variable
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmapOriginal.compress(Bitmap.CompressFormat.PNG, 80, baos);
                byte[] b = baos.toByteArray();
                b = tratarImagen(b);
                fotoen64 = Base64.getEncoder().encodeToString(b);

                //Poner el array comprimido como foto
                Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
                fotoperfil.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(EditarPerfil.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);

        if (bitmapRedimensionado!=null){
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapRedimensionado.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String fotoen64 = Base64.getEncoder().encodeToString(byteArray);
            savedInstanceState.putString("fotoen64", fotoen64);
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
        while(img.length > 50000){
            Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
            Bitmap compacto = Bitmap.createScaledBitmap(bitmap, (int)(bitmap.getWidth()*0.3), (int)(bitmap.getHeight()*0.3), true);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            compacto.compress(Bitmap.CompressFormat.PNG, 100, stream);
            img = stream.toByteArray();
        }
        return img;
    }
}