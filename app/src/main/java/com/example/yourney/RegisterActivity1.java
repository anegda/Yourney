package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class RegisterActivity1 extends AppCompatActivity {

    private EditText editPass;
    private EditText editPass2;
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
        setContentView(R.layout.activity_register1);

        //RESTRICCIONES para el editText de la contraseña
        editPass = findViewById(R.id.editPass);
        editPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editPass2 = findViewById(R.id.editPass2);
        editPass2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        editUser = findViewById(R.id.editUsuario);

        //BROADCAST RECEIVER PARA CERRAR ESTA ACTIVIDAD UNA VEZ COMPLETADO EL REGISTRO
        IntentFilter filter = new IntentFilter();

        filter.addAction("finish");
        registerReceiver(broadcastReceiver, filter);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    public void siguiente (View v){
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||  connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        if(connected) {
            String pass1 = editPass.getText().toString();
            String pass2 = editPass2.getText().toString();
            String user = editUser.getText().toString();


            if (pass1.equals(pass2) && !pass2.isEmpty() && !user.isEmpty()) {
                Intent intent = new Intent(this, RegisterActivity2.class);
                intent.putExtra("pass", pass1);
                intent.putExtra("user", user);
                startActivity(intent);
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