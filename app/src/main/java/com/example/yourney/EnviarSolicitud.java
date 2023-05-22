package com.example.yourney;

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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Locale;

public class EnviarSolicitud extends AppCompatActivity {

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
        setContentView(R.layout.activity_enviar_solicitud);

        Button btn_enviar = findViewById(R.id.btn_enviarSolicitud);
        btn_enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cogemos del campo el nombre de usuario
                EditText userEdit = findViewById(R.id.usuarioEdit);
                String userPeti = userEdit.getText().toString();

                // Comprobar credenciales contra la BD
                Data datos = new Data.Builder()
                        .putString("accion", "selectUsuario")
                        .putString("consulta", "Usuarios")
                        .putString("username", userPeti)
                        .build();

                // Peticion al Worker
                OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                        .setInputData(datos)
                        .build();

                WorkManager.getInstance(EnviarSolicitud.this).getWorkInfoByIdLiveData(select.getId()).observe(EnviarSolicitud.this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        // Gestiono la respuesta de la peticion
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            Data output = workInfo.getOutputData();
                            if (!output.getString("resultado").equals("Sin resultado")) {
                                //si el usuario existe hacemos el insert
                                // OBTENGO EL USUARIO ACTUAL y EL MENSAJE
                                Sesion sesion = new Sesion(EnviarSolicitud.this);
                                String username = sesion.getUsername();
                                EditText mensajeEdit = findViewById(R.id.mensajeEdit);
                                String mensaje = mensajeEdit.getText().toString();

                                Data datosInsert = new Data.Builder()
                                        .putString("accion", "insert")
                                        .putString("consulta", "Peticiones")
                                        .putString("username1", username)
                                        .putString("username2", userPeti)
                                        .putString("mensaje",mensaje)
                                        .putInt("estado",0)
                                        .build();

                                OneTimeWorkRequest insert = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                        .setInputData(datosInsert)
                                        .build();

                                WorkManager.getInstance(EnviarSolicitud.this).getWorkInfoByIdLiveData(insert.getId()).observe(EnviarSolicitud.this, new Observer<WorkInfo>() {
                                    @Override
                                    public void onChanged(WorkInfo workInfo) {
                                        Toast.makeText(EnviarSolicitud.this, "OK", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                WorkManager.getInstance(EnviarSolicitud.this).enqueue(insert);

                                //mientras también cogemos el token del receptor
                                Data datosSelectToken = new Data.Builder()
                                        .putString("accion", "select")
                                        .putString("consulta", "Tokens")
                                        .putString("username", userPeti)
                                        .build();

                                OneTimeWorkRequest selectToken = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                        .setInputData(datosSelectToken)
                                        .build();
                                WorkManager.getInstance(EnviarSolicitud.this).getWorkInfoByIdLiveData(selectToken.getId()).observe(EnviarSolicitud.this, new Observer<WorkInfo>() {
                                    @Override
                                    public void onChanged(WorkInfo workInfo) {
                                        if (workInfo != null && workInfo.getState().isFinished()) {
                                            Data output = workInfo.getOutputData();
                                            if (!output.getString("resultado").equals("Sin resultado")) {
                                                //si el usuario está logeado en algún dispositivo
                                                JSONParser parser = new JSONParser();
                                                try {
                                                    JSONArray jsonResultado = (JSONArray) parser.parse(output.getString("resultado"));

                                                    Integer i = 0;
                                                    System.out.println("***** " + jsonResultado + " *****");
                                                    while (i < jsonResultado.size()) {
                                                        JSONObject row = (JSONObject) jsonResultado.get(i);
                                                        System.out.println("***** " + row + " *****");
                                                        String Token = (String) row.get("Token");

                                                        Data datosNoti = new Data.Builder()
                                                                .putString("accion", "notifPeticion")
                                                                .putString("emisor", username)
                                                                .putString("receptor", Token)
                                                                .putString("mensaje", mensaje)
                                                                .build();
                                                        OneTimeWorkRequest notif = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                                                .setInputData(datosNoti)
                                                                .build();
                                                        WorkManager.getInstance(EnviarSolicitud.this).getWorkInfoByIdLiveData(notif.getId()).observe(EnviarSolicitud.this, new Observer<WorkInfo>() {
                                                            @Override
                                                            public void onChanged(WorkInfo workInfo) {
                                                            }
                                                        });
                                                        WorkManager.getInstance(EnviarSolicitud.this).enqueue(notif);
                                                        i=i+1;
                                                    }
                                                }catch (ParseException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }
                                    }
                                });
                                WorkManager.getInstance(EnviarSolicitud.this).enqueue(selectToken);

                            } else {
                                Toast.makeText(EnviarSolicitud.this, R.string.peticion_incorrecta, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                WorkManager.getInstance(EnviarSolicitud.this).enqueue(select);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Vuelvo a SolicitudesRecibidas
        Intent intent = new Intent(EnviarSolicitud.this, SolicitudesRecibidas.class);
        startActivity(intent);
        finish();
    }
}