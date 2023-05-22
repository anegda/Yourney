package com.example.yourney;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Locale;

public class RegisterActivity2 extends AppCompatActivity {

    private Context c = this;
    private Activity a = this;
    private String user;
    private String pass;
    private EditText editEmail;
    private EditText editNombre;
    private EditText editApellido;
    private ImageView fotoperfil;
    static String fotoen64;
    private Button btnLogin;
    private Bitmap bitmapRedimensionado;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;


    private ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK && result.getData()!= null) {
            Bundle bundle = result.getData().getExtras();
            ImageView img_perfil = findViewById(R.id.fotoperfil);
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
            ImageView img_perfil = findViewById(R.id.fotoperfil);
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
        setContentView(R.layout.activity_register2);


        // pedir permisos para acceder a camara
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        // inicializar los elementos del layout
        editEmail = findViewById(R.id.editEmail);
        editNombre = findViewById(R.id.editNombre);
        editApellido = findViewById(R.id.editApellido);
        fotoperfil = findViewById(R.id.fotoperfil);
        btnLogin = findViewById(R.id.button);

        // obtener los datos del usuario ya introducidos
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            user = extras.getString("user");
            pass = extras.getString("pass");
        }

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

        // objeto OrientationEventListener para detectar cambios de orientación de pantalla
        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
            }
        };
        orientationEventListener.enable();
    }

    public void login(View v){
        // Validar si el correo electrónico es válido utilizando una expresión regular
        String email = editEmail.getText().toString().trim();
        boolean isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();

        if (isEmailValid) {
            // obtener bitmap del imageview actual --> comprimirla --> convertirlo en base64 para subirlo a la bbdd
            //if (fotoperfil.getDrawable()!=null) {
            if (fotoen64 == null) {
                ImageView fotoPerfil = (ImageView) findViewById(R.id.fotoperfil);
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

            // Obtengo los datos a introducir en la BD
            String nombre = editNombre.getText().toString();
            String apellidos = editApellido.getText().toString();

            // Comprobar credenciales contra la BD
            Data datos = new Data.Builder()
                     .putString("accion", "selectUsuario")
                     .putString("consulta", "Usuarios")
                     .putString("actividad", "Registro")
                     .putString("username", user)
                     .build();

            // Peticion al Worker
            OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                    .setInputData(datos)
                    .build();

            WorkManager.getInstance(RegisterActivity2.this).getWorkInfoByIdLiveData(select.getId()).observe(RegisterActivity2.this, new Observer<WorkInfo>() {
                @Override
                public void onChanged(WorkInfo workInfo) {
                    // Gestiono la respuesta de la peticion
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        Data output = workInfo.getOutputData();
                        if (!output.getString("resultado").equals("Sin resultado")) {
                            Toast.makeText(RegisterActivity2.this, R.string.registro_incorrecto, Toast.LENGTH_SHORT).show();
                        } else {
                            Data datos = new Data.Builder()
                                    .putString("accion", "insert")
                                    .putString("consulta", "Usuarios")
                                    .putString("username", user)
                                    .putString("nombre", nombre)
                                    .putString("apellidos", apellidos)
                                    .putString("password", pass)
                                    .putString("email", email)
                                    .build();

                            OneTimeWorkRequest insert = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                    .setInputData(datos)
                                    .build();

                            // Introduzco en la BD
                            WorkManager.getInstance(RegisterActivity2.this).getWorkInfoByIdLiveData(insert.getId()).observe(RegisterActivity2.this, new Observer<WorkInfo>() {
                                @Override
                                public void onChanged(WorkInfo workInfo) {
                                    if (workInfo != null && workInfo.getState().isFinished()) {
                                        Data output = workInfo.getOutputData();
                                        if (output.getBoolean("resultado", false)) {

                                            // Obtengo el token del usuario registrado
                                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                                                @Override
                                                public void onComplete(@NonNull Task<String> task) {
                                                    if (!task.isSuccessful()) {
                                                        String token = "";
                                                        return;
                                                    }
                                                    String token = task.getResult();

                                                    // Registro el token del usuario en la bd
                                                    Data datos = new Data.Builder()
                                                            .putString("accion", "insert")
                                                            .putString("consulta", "Tokens")
                                                            .putString("username", user)
                                                            .putString("token", token)
                                                            .build();

                                                    OneTimeWorkRequest insert = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                                            .setInputData(datos)
                                                            .build();

                                                    WorkManager.getInstance(RegisterActivity2.this).getWorkInfoByIdLiveData(insert.getId()).observe(RegisterActivity2.this, new Observer<WorkInfo>() {
                                                        @Override
                                                        public void onChanged(WorkInfo workInfo) {
                                                            if (workInfo != null && workInfo.getState().isFinished()) {
                                                            }
                                                        }
                                                    });
                                                    WorkManager.getInstance(RegisterActivity2.this).enqueue(insert);
                                                }
                                            });

                                            // Guardo el usuario en la sesion
                                            Sesion sesion = new Sesion(RegisterActivity2.this);
                                            sesion.setUsername(user);

                                            System.out.println("***** " + fotoen64);
                                            // Paso a la siguiente actividad
                                            Toast.makeText(RegisterActivity2.this, getString(R.string.registro_correcto) + " " + user + "!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(RegisterActivity2.this, MainActivity.class);
                                            startActivity(intent);

                                            // Cerramos la anterior actividad
                                            Intent i = new Intent();
                                            i.setAction("finish");
                                            sendBroadcast(i);

                                            // Cerramos esta actividad
                                            finish();
                                        }
                                    }
                                }
                            });
                            WorkManager.getInstance(RegisterActivity2.this).enqueue(insert);
                        }
                    }
                }
            });
            WorkManager.getInstance(RegisterActivity2.this).enqueue(select);

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


    //PARA ESTABLECER IMAGEN
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                Uri imageUri = data.getData();
                if(imageUri==null){
                    Bitmap foto = (Bitmap) data.getExtras().get("data");
                    ImageView fotoPerfil = (ImageView) findViewById(R.id.fotoperfil);
                    fotoPerfil.setImageBitmap(foto);
                }else {
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    ImageView fotoPerfil = (ImageView) findViewById(R.id.fotoperfil);
                    fotoPerfil.setImageBitmap(selectedImage);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(RegisterActivity2.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(RegisterActivity2.this,  "ERROR",Toast.LENGTH_SHORT).show();
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

}