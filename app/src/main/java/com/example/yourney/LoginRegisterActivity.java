package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LoginRegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginregister);

        /**
        Sesion sesion = new Sesion(this);
        String username = sesion.getUsername();
        if (!username.equals("")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        **/
    }

    public void login(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    public void registrarse(View v) {
        Intent intent = new Intent(this, RegisterActivity1.class);
        startActivity(intent);
    }
}