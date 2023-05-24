package com.example.yourney;

import androidx.annotation.NonNull;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private EditText editPass;
    private EditText editUser;
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
        setContentView(R.layout.activity_login);

        //RESTRICCIONES para el editText de la contraseña
        editPass = findViewById(R.id.editPass);
        editPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        editUser = findViewById(R.id.editUsuario);

    }
    public void login (View v){
        //COMPROBAMOS SI EXISTE CONEXIÓN A INTERNET
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||  connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        if(connected) {
            String pass = editPass.getText().toString();
            String user = editUser.getText().toString();


            if (!pass.isEmpty() && !user.isEmpty()) {

                // Obtengo el nombre de usuario y contraseña introducidos
                String username = editUser.getText().toString();
                String password = editPass.getText().toString();

                // Comprobar credenciales contra la bbdd
                Data datos = new Data.Builder()
                        .putString("accion", "selectUsuario")
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
                            if (!output.getString("resultado").equals("Sin resultado")) {

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
                                                    Toast.makeText(LoginActivity.this, getString(R.string.login_correcto) + " " + user + "!", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(intent);

                                                    finish();
                                                }
                                            }
                                        });
                                        WorkManager.getInstance(LoginActivity.this).enqueue(insert);
                                    }
                                });
                            } else {
                                Toast.makeText(LoginActivity.this, R.string.login_incorrecto, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                WorkManager.getInstance(LoginActivity.this).enqueue(selectLogin);

            } else {
                Toast.makeText(this, R.string.str9, Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this, getString(R.string.error_conexión), Toast.LENGTH_LONG).show();
        }
    }

    //VOLVEMOS A LOGINREGISTER SI PULSAMOS ATRAS
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, LoginRegisterActivity.class));
        finish();
    }

}