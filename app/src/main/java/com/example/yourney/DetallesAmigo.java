package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Locale;

public class DetallesAmigo extends AppCompatActivity {
    private boolean esFavorito = false;
    static String fotoPerfil;
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
        setContentView(R.layout.activity_detalles_amigo);setTitle(getClass().getSimpleName());

        // HACEMOS SELECT DEL USUARIO ELEGIDO
        String username = getIntent().getExtras().getString("username");
        Data datosUsuarios = new Data.Builder()
                .putString("accion", "selectUsuario")
                .putString("consulta", "Usuarios")
                .putString("username", username)
                .build();
        OneTimeWorkRequest selectUsuario = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datosUsuarios)
                .build();
        WorkManager.getInstance(DetallesAmigo.this).getWorkInfoByIdLiveData(selectUsuario.getId()).observe(DetallesAmigo.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo != null && workInfo.getState().isFinished()) {
                    Data output = workInfo.getOutputData();
                    if (!output.getString("resultado").equals("Sin resultado")) {
                        if (fotoPerfil != null) {
                            ImageView fotoPerfil_IV = (ImageView) findViewById(R.id.fotoAmigo);
                            byte[] encodeBytes = Base64.getDecoder().decode(fotoPerfil);
                            Bitmap foto = BitmapFactory.decodeByteArray(encodeBytes, 0, encodeBytes.length);
                            fotoPerfil_IV.setImageBitmap(foto);
                        }

                        TextView usernameTV = findViewById(R.id.usernameAmigo);
                        TextView nombreTV = findViewById(R.id.nombreAmigo);
                        TextView apellidosTV = findViewById(R.id.apellidosAmigo);
                        TextView emailTV = findViewById(R.id.emailAmigo);

                        String username = output.getString("username");
                        String nombre = output.getString("nombre");
                        String apellidos = output.getString("apellidos");
                        String email = output.getString("email");

                        usernameTV.setText(username);
                        nombreTV.setText(nombre);
                        apellidosTV.setText(apellidos);
                        emailTV.setText(email);
                    }
                }
            }
        });
        WorkManager.getInstance(DetallesAmigo.this).enqueue(selectUsuario);

        Button btnEliminar = (Button) findViewById(R.id.btn_eliminarAmigo);
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView usernameTV = findViewById(R.id.usernameAmigo);
                String username1 = usernameTV.getText().toString();

                Sesion sesion = new Sesion(DetallesAmigo.this);
                String username2 = sesion.getUsername();

                //ELIMINAMOS LA PETICIÓN
                Data datosAmistad = new Data.Builder()
                        .putString("accion", "delete")
                        .putString("consulta", "Amigos")
                        .putString("username1", username1)
                        .putString("username2", username2)
                        .build();
                OneTimeWorkRequest delete = new OneTimeWorkRequest.Builder(ConexionBD.class)
                        .setInputData(datosAmistad)
                        .build();
                WorkManager.getInstance(DetallesAmigo.this).getWorkInfoByIdLiveData(delete.getId()).observe(DetallesAmigo.this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        Log.d("DAS", "amistad eliminada");
                        startActivity(new Intent(DetallesAmigo.this, MisAmigos.class));
                        finish();
                    }
                });
                WorkManager.getInstance(DetallesAmigo.this).enqueue(delete);
            }
        });
    }
}