package com.example.yourney;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Locale;

public class EditarPerfil extends AppCompatActivity {

    EditText nombre, apellido, email, password;
    ImageView fotoperfil;
    Button btnGuardar;
    Bitmap bitmap;
    static String fotoen64;
    static String fotoperfilStr;
    private Bitmap bitmapRedimensionado;
    private static final int PICK_IMAGE_REQUEST = 1;

    private ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData()!= null) {
            Bundle bundle = result.getData().getExtras();
            ImageView img_perfil = findViewById(R.id.fotoPerfilEditarPerfil);
            Bitmap miniatura = (Bitmap) bundle.get("data");
            img_perfil.setImageBitmap(miniatura);

            /** Código para convertir un BitMap en un String
             *  Pregunta de StackOverflow: https://stackoverflow.com/questions/13562429/how-many-ways-to-convert-bitmap-to-string-and-vice-versa
             *  Autor de la respuesta: https://stackoverflow.com/users/1191766/sachin10
             */
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            miniatura.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
            byte[] img_bytes = byteStream.toByteArray();
            fotoen64 = java.util.Base64.getEncoder().encodeToString(img_bytes);
            System.out.println(fotoen64);

        } else {
            Log.d("TakenPicture", "No photo taken");
        }
    });

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: " + uri);
            Bitmap bmap = null;
            try {
                /** Código utilizado para obtener el BitMap de una imagen sacada de la galería mediante una URI
                 *  Pregunta de StackOverflow: https://stackoverflow.com/questions/3879992/how-to-get-bitmap-from-an-uri
                 *  Autor de la respuesta: https://stackoverflow.com/users/986/mark-ingram
                 */
                bmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                /** Código para convertir un BitMap en un String
                 *  Pregunta de StackOverflow: https://stackoverflow.com/questions/13562429/how-many-ways-to-convert-bitmap-to-string-and-vice-versa
                 *  Autor de la respuesta: https://stackoverflow.com/users/1191766/sachin10
                 */
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                bmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
                byte[] img_bytes =byteStream.toByteArray();
                fotoen64 = java.util.Base64.getEncoder().encodeToString(img_bytes);
                System.out.println(fotoen64);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            ImageView img_perfil = findViewById(R.id.fotoPerfilEditarPerfil);
            img_perfil.setImageURI(uri);
        } else {
            Log.d("PhotoPicker", "No media selected");
        }
    });

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
        setContentView(R.layout.activity_editar_perfil);

        fotoperfil = findViewById(R.id.fotoPerfilEditarPerfil);
        nombre = findViewById(R.id.nombre);
        apellido = findViewById(R.id.apellido);
        email = findViewById(R.id.email);
        password = findViewById(R.id.contrasenia);

        btnGuardar = findViewById(R.id.btnGuardarEditarUsuario);
        Sesion sesion = new Sesion(this);

        // si hay alguna imagen colocada por el usuario recuperarla y colocarla; sino colocar la default
        if (savedInstanceState != null) {
            String fotoen64 = savedInstanceState.getString("fotoen64");
            if (fotoen64!=null){
                byte[] decodedBytes = Base64.getDecoder().decode(fotoen64);
                bitmapRedimensionado = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                fotoperfil.setImageBitmap(bitmapRedimensionado);
            }
        }else{
            fotoperfil.setImageResource(R.drawable.perfil);
        }

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
                    Log.d("OUTPUT DATA", output.toString());
                    if (!output.getString("resultado").equals("Sin resultado")) {
                        JSONParser parser = new JSONParser();
                        try {
                            // Obtengo la informacion de el usuario devuelto
                            nombre.setText(output.getString("nombre"));
                            apellido.setText(output.getString("apellidos"));
                            email.setText(output.getString("email"));

                            if(fotoperfilStr!=null) {
                                byte[] encodeByte = Base64.getDecoder().decode(fotoperfilStr);
                                bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                                fotoperfil.setImageBitmap(bitmap);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        WorkManager.getInstance(EditarPerfil.this).enqueue(select);
    }

    public void update(View v){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||  connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        if(connected) {
            // Validar si el correo electrónico es válido utilizando una expresión regular

            String emailStr = email.getText().toString().trim();
            boolean isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(emailStr).matches();

            if (isEmailValid) {
                // obtener bitmap del imageview actual --> comprimirla --> convertirlo en base64 para subirlo a la bbdd
                //if (fotoperfil.getDrawable()!=null) {
                if (fotoen64 == null) {
                    ImageView fotoPerfil = (ImageView) findViewById(R.id.fotoPerfilEditarPerfil);
                    Bitmap img = ((BitmapDrawable) fotoPerfil.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    img.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] b = baos.toByteArray();
                    //PARA QUE NO EXISTAN PROBLEMAS CON EL TAMAÑO DE LA IMAGEN
                    b = tratarImagen(b);
                    fotoen64 = Base64.getEncoder().encodeToString(b);
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
                Toast.makeText(this, "Error al editar los datos", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this, getString(R.string.error_conexión), Toast.LENGTH_LONG).show();
        }
    }

    public void setFotoPerfil(View v){
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
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                if(imageUri==null){
                    Bitmap foto = (Bitmap) data.getExtras().get("data");
                    ImageView fotoPerfil = (ImageView) findViewById(R.id.fotoPerfilEditarPerfil);
                    fotoPerfil.setImageBitmap(foto);
                }else {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    ImageView fotoPerfil = (ImageView) findViewById(R.id.fotoPerfilEditarPerfil);
                    fotoPerfil.setImageBitmap(selectedImage);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(EditarPerfil.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(EditarPerfil.this,  "ERROR",Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        // Vuelvo a la actividad Main
        super.onBackPressed();
        Intent intent = new Intent(EditarPerfil.this, MainActivity.class);
        startActivity(intent);
        // Termino esta actividad
        finish();
    }
}