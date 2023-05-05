package com.example.yourney;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Base64;

public class RegisterActivity2 extends AppCompatActivity {

    private Context c = this;
    private Activity a = this;
    private String user;

    private EditText editEmail;
    private ImageView fotoperfil;
    private Bitmap bitmapRedimensionado;
    private Bitmap bitmapOriginal;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 2;

    private int anchoDestino;
    private int altoDestino;

    // Sacar una fotografía usando una aplicación de fotografía y posteriormente
    // escalarlas al tamaño que se van a mostrar, pero manteniendo su aspecto
    private ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {

                if (result.getResultCode() == RESULT_OK && result.getData()!= null ) {
                    Bundle bundle = result.getData().getExtras();
                    Bitmap bitmapFoto = (Bitmap) bundle.get( "data" );
                    bitmapRedimensionado = redimensionarImagen(bitmapFoto);
                    fotoperfil.setImageBitmap(bitmapRedimensionado);

                } else {
                    Log. d ( "FOTOS" , "no se ha sacado la foto" );
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);


        // pedir permisos para acceder a camara
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }


        // inicializar los elementos del layout
        editEmail = findViewById(R.id.editEmail);
        fotoperfil = findViewById(R.id.fotoperfil);


        // si hay alguna imagen colocada por el usuario recuperarla y colocarla; sino colocar la default
        if (savedInstanceState != null) {
            String fotoen64 = savedInstanceState.getString("fotoen64");
            if (fotoen64!=null){
                byte[] decodedBytes = Base64.getDecoder().decode(fotoen64);
                bitmapRedimensionado = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                fotoperfil.setImageBitmap(bitmapRedimensionado);
            }
        }else{
            fotoperfil.setBackgroundResource(R.drawable.perfil);
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
            String fotoen64="";
            if (fotoperfil.getDrawable()!=null) {
                Bitmap bitmap = ((BitmapDrawable) fotoperfil.getDrawable()).getBitmap();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                fotoen64 = new String(Base64.getEncoder().encode(byteArray));
            }

        } else {
            Toast.makeText(this, R.string.str10, Toast.LENGTH_LONG).show();
        }

    }

    public void setFotopPerfil(View v){

        fotoperfil = findViewById(R.id.fotoperfil);
        anchoDestino = fotoperfil.getWidth();
        altoDestino = fotoperfil.getHeight();

        // dialogo para setear la foto de perfil con dos opciones --> (1)camara // (2)galeria
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.str6))
                .setIcon(R.drawable.logocolor)
                .setItems(new CharSequence[]{getString(R.string.str7), getString(R.string.str8)}, (dialog, which) -> {
                    switch (which) {

                        case 0:
                            Intent elIntentFoto= new Intent(MediaStore. ACTION_IMAGE_CAPTURE );
                            takePictureLauncher.launch(elIntentFoto);
                            break;

                        case 1:
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                            // agregar el siguiente extra para establecer la orientación de la galería a la orientación actual de la pantalla
                            int orientation = getResources().getConfiguration().orientation;
                            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                                intent.putExtra("orientation", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                            } else {
                                intent.putExtra("orientation", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                            }
                            startActivityForResult(intent, PICK_IMAGE_REQUEST);
                            break;
                    }
                })
                .show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // si la imagen viene de la galeria, primero reducir la calidad y despues colocarla en el imageview cuando el proceso termine
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(uri);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            bitmapOriginal  = BitmapFactory.decodeStream(inputStream);

            // Reducir la calidad de la imagen al 50%
            int calidad = 50; // porcentaje de calidad de la imagen (0-100)
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmapOriginal.compress(Bitmap.CompressFormat.JPEG, calidad, byteArrayOutputStream);

            // Convertir el ByteArrayOutputStream en un array de bytes
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            // Decodificar el array de bytes en un objeto Bitmap
            bitmapRedimensionado = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            // redimensionar la imagen al imageview
            bitmapRedimensionado = redimensionarImagen(bitmapRedimensionado);

            // Mostrar el Bitmap redimensionado en una ImageView
            fotoperfil.setImageBitmap(bitmapRedimensionado);

        }
        // una vez colocada la imagen restablecer la orientación de la pantalla a su valor predeterminado
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }


    public Bitmap redimensionarImagen (Bitmap bitmapFoto){

        int anchoImagen = bitmapFoto.getWidth();
        int altoImagen = bitmapFoto.getHeight();

        float ratioImagen = ( float ) anchoImagen / ( float ) altoImagen;
        float ratioDestino = ( float ) anchoDestino / ( float ) altoDestino;
        int anchoFinal = anchoDestino;
        int altoFinal = altoDestino;
        if (ratioDestino > ratioImagen) {
            anchoFinal = ( int ) (( float )altoDestino * ratioImagen);
        } else {
            altoFinal = ( int ) (( float )anchoDestino / ratioImagen);
        }
        bitmapRedimensionado = Bitmap. createScaledBitmap (bitmapFoto,anchoFinal,altoFinal, true );
        return bitmapRedimensionado;
    }


    // restablecer la orientación de la pantalla a su valor predeterminado después de cualquier cambio de orientación de pantalla
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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

}