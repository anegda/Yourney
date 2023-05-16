package com.example.yourney;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SolicitudesRecibidas extends AppCompatActivity implements ElAdaptadorRecyclerAmigos.RecyclerItemClick,SearchView.OnQueryTextListener {
    SearchView buscadorUsuarios;
    RecyclerView lalista;
    private List<ItemListAmigo> items = new ArrayList<ItemListAmigo>();
    ElAdaptadorRecyclerAmigos adapter = new ElAdaptadorRecyclerAmigos(items, SolicitudesRecibidas.this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_amigos);

        buscadorUsuarios = findViewById(R.id.search_friends);
        lalista = findViewById(R.id.elreciclerview);

        LinearLayoutManager manager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lalista.setLayoutManager(manager);

        // OBTENGO EL USUARIO ACTUAL
        Sesion sesion = new Sesion(this);
        String username = sesion.getUsername();

        ArrayList<String> userPeticion = new ArrayList<String>();
        ArrayList<String> messagePeticion = new ArrayList<String>();

        // CONSULTAMOS A LA BD POR PETICIONES DE AMISTAD
        Data datos = new Data.Builder()
                .putString("accion", "select")
                .putString("consulta", "Peticiones")
                .putString("username", username)
                .build();

        OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datos)
                .build();

        WorkManager.getInstance(SolicitudesRecibidas.this).getWorkInfoByIdLiveData(select.getId()).observe(SolicitudesRecibidas.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo != null && workInfo.getState().isFinished()) {
                    Data output = workInfo.getOutputData();
                    if (!output.getString("resultado").equals("Sin resultado")) {
                        JSONParser parser = new JSONParser();
                        try {
                            // Obtengo la informacion de los amigos devueltos
                            JSONArray jsonResultado =(JSONArray) parser.parse(output.getString("resultado"));

                            Integer i = 0;
                            System.out.println("***** " + jsonResultado + " *****");
                            while (i < jsonResultado.size()) {
                                JSONObject row = (JSONObject) jsonResultado.get(i);
                                System.out.println("***** " + row + " *****");
                                String username2 = (String) row.get("Username2");
                                String message = (String) row.get("Mensaje");
                                int estado = (int) row.get("Estado");
                                //SI LA PETICIÓN ESTA SIN RESOLVER LA IMPRIMIMOS POR PANTALLA
                                if(estado == 0){
                                    userPeticion.add(username2);
                                    messagePeticion.add(message);
                                }
                                i++;
                            }

                            items = getItems(userPeticion, messagePeticion);
                            adapter = new ElAdaptadorRecyclerAmigos(items, SolicitudesRecibidas.this);
                            lalista.setAdapter(adapter);
                            buscadorUsuarios.setOnQueryTextListener(SolicitudesRecibidas.this);

                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        WorkManager.getInstance(SolicitudesRecibidas.this).enqueue(select);

        Log.d("DAS", String.valueOf(items));
        adapter = new ElAdaptadorRecyclerAmigos(items, SolicitudesRecibidas.this);
        lalista.setAdapter(adapter);
        buscadorUsuarios.setOnQueryTextListener(SolicitudesRecibidas.this);

        Button btn_añadir = (Button) findViewById(R.id.añadirAmigo);
        btn_añadir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SolicitudesRecibidas.this, EnviarSolicitud.class));
                finish();
            }
        });
    }

    private List<ItemListAmigo> getItems(ArrayList<String> usuarios, ArrayList<String> mensajes) {
        List<ItemListAmigo> itemListPeticiones = new ArrayList<>();
        int i = 0;
        while(i < usuarios.size()){
            final String[] username = {usuarios.get(i)};
            final String[] mensaje = {mensajes.get(i)};
            final Bitmap[] bitmap = new Bitmap[1];
            // CONSULTAMOS A LA BD POR AMIGOS
            Data datos = new Data.Builder()
                    .putString("accion", "select")
                    .putString("consulta", "Usuarios")
                    .putString("username", username[0])
                    .build();

            OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                    .setInputData(datos)
                    .build();

            WorkManager.getInstance(SolicitudesRecibidas.this).getWorkInfoByIdLiveData(select.getId()).observe(SolicitudesRecibidas.this, new Observer<WorkInfo>() {
                @Override
                public void onChanged(WorkInfo workInfo) {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        Data output = workInfo.getOutputData();
                        if (!output.getString("resultado").equals("Sin resultado")) {
                            JSONParser parser = new JSONParser();
                            try {
                                // Obtengo la informacion de el usuario devuelto
                                JSONObject jsonResultado =(JSONObject) parser.parse(output.getString("resultado"));
                                Log.d("DAS", String.valueOf(jsonResultado));
                                String fotoPerfil = (String) jsonResultado.get("FotoPerfil");

                                byte[] encodeByte = Base64.getDecoder().decode(fotoPerfil);
                                bitmap[0] = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                                ItemListAmigo amigoAct = new ItemListAmigo(username[0], mensaje[0], bitmap[0]);
                                itemListPeticiones.add(amigoAct);

                                adapter = new ElAdaptadorRecyclerAmigos(itemListPeticiones, SolicitudesRecibidas.this);
                                lalista.setAdapter(adapter);
                                buscadorUsuarios.setOnQueryTextListener(SolicitudesRecibidas.this);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            });
            WorkManager.getInstance(SolicitudesRecibidas.this).enqueue(select);
        }
        return itemListPeticiones;
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
    public void itemClick(ItemListAmigo item) {
        Intent intent = new Intent(this, DetallesAmigo.class);
        intent.putExtra("itemDetail", item);
        startActivity(intent);
    }
}