package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class EditarRuta extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_ruta);

        Button btn_guardar = (Button) findViewById(R.id.btn_guardarDatosRuta);
        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: REALIZAR EL UPDATE EN LA BD

                //VOLVEMOS A LA ACTIVIDAD DE VER RUTA
                Intent i = new Intent(EditarRuta.this, VerRuta.class);
                startActivity(i);
                finish();
            }
        });
    }
}