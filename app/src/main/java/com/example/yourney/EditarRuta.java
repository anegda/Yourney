package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class EditarRuta extends AppCompatActivity {

    TextView tituloRuta;
    ImageView fotoDescRuta;
    RadioGroup dificultad, visibilidad;
    EditText informacionExtra;
    ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_ruta);

        fotoDescRuta = findViewById(R.id.fotoDescRuta);
        tituloRuta = findViewById(R.id.tituloRutaEdit);
        dificultad = findViewById(R.id.dificultadRutaGroup);
        informacionExtra = findViewById(R.id.otrosMutilineText);
        visibilidad = findViewById(R.id.visibilidadRutaGroup);

        restaurarCampos();

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
                Intent i = new Intent(EditarRuta.this, MisEditores.class);
                i.putExtra("tituloRuta", tituloRuta.getText().toString());
                i.putExtra("dificultadRuta", dificultad.indexOfChild(dificultad.findViewById(dificultad.getCheckedRadioButtonId())));
                i.putExtra("infoRuta", informacionExtra.getText().toString());
                i.putExtra("visibilidadRuta", visibilidad.indexOfChild(visibilidad.findViewById(visibilidad.getCheckedRadioButtonId())));
                startActivity(i);
                finish();
            }
        });
    }

    public void restaurarCampos(){
        ArrayList<String> lista = (ArrayList<String>) getIntent().getSerializableExtra("editores");
        System.out.println("ListaEditores" + lista);

        if(lista != null){
            mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lista);
            ListView listView = findViewById(R.id.listEditores);
            listView.setAdapter(mAdapter);
        }

        tituloRuta.setText(getIntent().getStringExtra("tituloRuta"));
        dificultad.check(getIntent().getIntExtra("dificultadRuta", 0));
        informacionExtra.setText(getIntent().getStringExtra("infoRuta"));
        visibilidad.check(getIntent().getIntExtra("visibilidadRuta",  0));

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