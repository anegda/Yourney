package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class EditarRuta extends AppCompatActivity {

    TextView tituloRuta;
    ImageView fotoDescRuta;
    RadioGroup dificultad, visibilidad;
    EditText informacionExtra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_ruta);

        tituloRuta = findViewById(R.id.tituloRutaEdit);
        dificultad = findViewById(R.id.dificultadRutaGroup);
        informacionExtra = findViewById(R.id.otrosMutilineText);
        visibilidad = findViewById(R.id.visibilidadRutaGroup);


        Button btn_guardar = (Button) findViewById(R.id.btn_guardarDatosRuta);
        Button btn_editores = (Button) findViewById(R.id.btn_anadirEditores);
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

        btn_editores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EditarRuta.this, VerRuta.class);
                startActivity(i);

            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("tituloRuta", tituloRuta.getText().toString());
        outState.putInt("dificultad", dificultad.indexOfChild(dificultad.findViewById(dificultad.getCheckedRadioButtonId())));
        outState.putString("infoExtra", informacionExtra.getText().toString());
        outState.putInt("visibilidad", visibilidad.indexOfChild(visibilidad.findViewById(visibilidad.getCheckedRadioButtonId())));

        if (fotoDescRuta.getDrawable() != null) {
            Bitmap imagen = ((BitmapDrawable)fotoDescRuta.getDrawable()).getBitmap();
            outState.putParcelable("imagen", imagen);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        tituloRuta.setText(outState.getString("tituloRuta"));
        dificultad.check(outState.getInt("difitultad"));
        informacionExtra.setText(outState.getString("infoExtra"));
        visibilidad.check(outState.getInt("visibilidad"));

        fotoDescRuta.setImageBitmap(outState.getParcelable("imagen"));
    }
}