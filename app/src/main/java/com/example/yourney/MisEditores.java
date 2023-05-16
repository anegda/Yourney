package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SearchView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class MisEditores extends AppCompatActivity implements ElAdaptadorRecyclerEditor.RecyclerItemClick,SearchView.OnQueryTextListener {

    SearchView buscadorUsuarios;
    RecyclerView lalista;
    Button btnGuardar;
    List<ItemListEditor> items = new ArrayList<ItemListEditor>();
    ArrayList<String> editores = new ArrayList<String>();
    ElAdaptadorRecyclerEditor adapter = new ElAdaptadorRecyclerEditor(items, MisEditores.this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_editores);

        buscadorUsuarios = findViewById(R.id.buscar_editores);
        lalista = findViewById(R.id.elreciclerview);
        btnGuardar = findViewById(R.id.btnGuardarEditores);

        LinearLayoutManager manager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lalista.setLayoutManager(manager);

        // OBTENGO EL USUARIO ACTUAL
        Sesion sesion = new Sesion(this);
        String username = sesion.getUsername();

        ArrayList<String> amigos = new ArrayList<String>();

        // CONSULTAMOS A LA BD POR AMIGOS
        Data datos = new Data.Builder()
                .putString("accion", "select")
                .putString("consulta", "Amigos")
                .putString("username", username)
                .build();

        OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datos)
                .build();

        WorkManager.getInstance(MisEditores.this).getWorkInfoByIdLiveData(select.getId()).observe(MisEditores.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo != null && workInfo.getState().isFinished()) {
                    Data output = workInfo.getOutputData();
                    if (!output.getString("resultado").equals("Sin resultado")) {
                        JSONParser parser = new JSONParser();
                        try {
                            // Obtengo la informacion de las rutas devueltas
                            JSONArray jsonResultado =(JSONArray) parser.parse(output.getString("resultado"));

                            Integer i = 0;
                            System.out.println("***** " + jsonResultado + " *****");
                            while (i < jsonResultado.size()) {
                                JSONObject row = (JSONObject) jsonResultado.get(i);
                                System.out.println("***** " + row + " *****");
                                amigos.add((String) row.get("Username2"));
                                i++;
                            }

                            items = getItems(amigos);
                            adapter = new ElAdaptadorRecyclerEditor(items,MisEditores.this);
                            lalista.setAdapter(adapter);

                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        WorkManager.getInstance(MisEditores.this).enqueue(select);

        Log.d("DAS", String.valueOf(items));
        adapter = new ElAdaptadorRecyclerEditor(items, MisEditores.this);
        lalista.setAdapter(adapter);
        buscadorUsuarios.setOnQueryTextListener(MisEditores.this);

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MisEditores.this, EditarRuta.class);
                intent.putExtra("editores", editores);

                intent.putExtra("tituloRuta", getIntent().getStringExtra("tituloRuta"));
                intent.putExtra("dificultadRuta", getIntent().getIntExtra("dificultadRuta", 1));
                intent.putExtra("infoRuta", getIntent().getStringExtra("infoRuta"));
                intent.putExtra("visibilidadRuta", getIntent().getIntExtra("visibilidadRuta", 1));

                startActivity(intent);
            }
        });

    }

    /** Metodo para probar si funciona el reciclerView, luego se cambiara por una llamada a la BD **/
    private List<ItemListEditor> getItems(ArrayList<String> amigos) {
        List<ItemListEditor> itemListAmigos = new ArrayList<>();
        for (String amigo : amigos) {
            ItemListEditor amigoAct = new ItemListEditor(amigo, "prueba","prueba", false);
            itemListAmigos.add(amigoAct);
        }
        return itemListAmigos;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    /** Cada vez que el texto cambia se filtran las busquedas **/
    public boolean onQueryTextChange(String s) {
        adapter.filter(s);
        return false;
    }

    @Override
    public void itemClick(ItemListEditor item) {
        if(item.isChecked){
            for (int i=0;i<editores.size();i++) {
                if(editores.get(i)==item.getUsername()){
                    editores.remove(i);
                }
            }
            item.isChecked = false;
            System.out.println(editores);
        } else {
            item.isChecked = true;
            editores.add(item.getUsername());
            System.out.println(editores);
        }
    }
}