package com.example.yourney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {

    private EditText editPass;
    private EditText editUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //RESTRICCIONES para el editText de la contraseña
        editPass = findViewById(R.id.editPass);
        editPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        editUser = findViewById(R.id.editUsuario);

    }
    public void login (View v){

        String pass = editPass.getText().toString();
        String user = editUser.getText().toString();


        if (!pass.isEmpty() && !user.isEmpty()){

            // Obtengo el nombre de usuario y contraseña introducidos
            String username = editUser.getText().toString();
            String password = editPass.getText().toString();

            // Comprobar credenciales contra la bbdd
            Data datos = new Data.Builder()
                    .putString("accion", "select")
                    .putString("consulta", "Login")
                    .putString("username", username)
                    .putString("password", password)
                    .build();

            // Peticion al Worker
            OneTimeWorkRequest selectLogin = new OneTimeWorkRequest.Builder(ConexionBD.class)
                    .setInputData(datos)
                    .build();

            WorkManager.getInstance(LoginActivity.this).getWorkInfoByIdLiveData(selectLogin.getId()).observe(LoginActivity.this, new Observer<WorkInfo>() {
                @Override
                public void onChanged(WorkInfo workInfo) {
                    // Gestiono la respuesta de la peticion
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        Data output = workInfo.getOutputData();
                        if(!output.getString("resultado").equals("Sin resultado")) {

                            // Obtengo el token del usuario logueado
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
                                            .putString("username", username)
                                            .putString("token", token)
                                            .build();

                                    OneTimeWorkRequest insert = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                            .setInputData(datos)
                                            .build();

                                    WorkManager.getInstance(LoginActivity.this).getWorkInfoByIdLiveData(insert.getId()).observe(LoginActivity.this, new Observer<WorkInfo>() {
                                        @Override
                                        public void onChanged(WorkInfo workInfo) {
                                            if (workInfo != null && workInfo.getState().isFinished()) {
                                                // Guardo el usuario en la sesion
                                                Sesion sesion = new Sesion(LoginActivity.this);
                                                sesion.setUsername(editUser.getText().toString());

                                                // Paso a la siguiente actividad
                                                Intent intent = new Intent(LoginActivity.this, PublicRoutesActivity.class);
                                                startActivity(intent);

                                                finish();
                                            }
                                        }
                                    });
                                    WorkManager.getInstance(LoginActivity.this).enqueue(insert);

                                }
                            });
                        }
                    }
                }
            });
            WorkManager.getInstance(LoginActivity.this).enqueue(selectLogin);

            /**
            Sesion sesion = new Sesion(this);
            sesion.setUsername(editUser.getText().toString());
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            **/

        }else{
            Toast.makeText(this, R.string.str9, Toast.LENGTH_LONG).show();
        }
    }
}