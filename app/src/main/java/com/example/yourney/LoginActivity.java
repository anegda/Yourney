package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

            // comprobar credenciales contra la bbdd

            Intent intent = new Intent(this, PublicRoutesActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(this, R.string.str9, Toast.LENGTH_LONG).show();
        }
    }
}