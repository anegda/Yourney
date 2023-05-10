package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterActivity1 extends AppCompatActivity {

    private EditText editPass;
    private EditText editPass2;
    private EditText editUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register1);

        //RESTRICCIONES para el editText de la contraseña
        editPass = findViewById(R.id.editPass);
        editPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        editPass2 = findViewById(R.id.editPass2);
        editPass2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        editUser = findViewById(R.id.editUsuario);

    }

    public void siguiente (View v){

        String pass1 = editPass.getText().toString();
        String pass2 = editPass2.getText().toString();
        String user = editUser.getText().toString();


        if (pass1.equals(pass2) && !pass2.isEmpty() && !user.isEmpty()){
            Intent intent = new Intent(this, RegisterActivity2.class);
            intent.putExtra("pass", pass1);
            intent.putExtra("user", user);
            startActivity(intent);
        }else{
            Toast.makeText(this, R.string.str9, Toast.LENGTH_LONG).show();
        }
    }
}