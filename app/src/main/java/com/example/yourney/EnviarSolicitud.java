package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EnviarSolicitud extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                        .putString("accion", "select")
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
                                        .putString("consulta", "Peticion")
                                        .putString("Username1", username)
                                        .putString("Username2", userPeti)
                                        .putString("Mensaje",mensaje)
                                        .putInt("Estado",0)
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
}