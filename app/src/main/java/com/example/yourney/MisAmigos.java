package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MisAmigos extends AppCompatActivity implements ElAdaptadorRecyclerAmigos.RecyclerItemClick, SearchView.OnQueryTextListener {
    SearchView buscadorUsuarios;
    RecyclerView lalista;
    private List<ItemListAmigo> items = new ArrayList<ItemListAmigo>();
    ElAdaptadorRecyclerAmigos adapter = new ElAdaptadorRecyclerAmigos(items, MisAmigos.this);
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

        /**
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

        WorkManager.getInstance(MisAmigos.this).getWorkInfoByIdLiveData(select.getId()).observe(MisAmigos.this, new Observer<WorkInfo>() {
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
                                String username1 = (String) row.get("Username1");
                                String username2 = (String) row.get("Username2");
                                if(username1.equals(username)){
                                    amigos.add(username2);
                                } else{
                                    amigos.add(username1);
                                }

                                i++;
                            }

                            items = getItems(amigos);
                            adapter = new ElAdaptadorRecyclerAmigos(items, MisAmigos.this);
                            lalista.setAdapter(adapter);
                            buscadorUsuarios.setOnQueryTextListener(MisAmigos.this);

                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        WorkManager.getInstance(MisAmigos.this).enqueue(select);
        **/

        // Llamada al asynctask
        String urlAmigos = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selects.php";
        String urlUsuarios = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selectUsuarios.php";
        String paramsAmigos = "?consulta=Amigos&username=" + username;
        String paramsUsuarios = "?consulta=Usuarios&username=";
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.elreciclerview);
        TaskGetAmigos taskGetAmigos = new TaskGetAmigos(urlAmigos + paramsAmigos, urlUsuarios + paramsUsuarios, recyclerView, MisAmigos.this, username);
        taskGetAmigos.execute();

        /**
        Log.d("DAS", String.valueOf(items));
        adapter = new ElAdaptadorRecyclerAmigos(items, MisAmigos.this);
        lalista.setAdapter(adapter);
        **/

        buscadorUsuarios.setOnQueryTextListener(MisAmigos.this);

        Button añadirAmigo = (Button) findViewById(R.id.añadirAmigo);
        añadirAmigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MisAmigos.this, EnviarSolicitud.class));
                finish();
            }
        });
    }

    private List<ItemListAmigo> getItems(ArrayList<String> amigos) {
        List<ItemListAmigo> itemListAmigos = new ArrayList<>();
        for (String amigo : amigos) {
            final String[] username = {amigo};
            final String[] nombre = {""};
            final Bitmap[] bitmap = new Bitmap[1];
            // CONSULTAMOS A LA BD POR AMIGOS
            Data datos = new Data.Builder()
                    .putString("accion", "select")
                    .putString("consulta", "Usuarios")
                    .putString("username", amigo)
                    .build();

            OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                    .setInputData(datos)
                    .build();

            WorkManager.getInstance(MisAmigos.this).getWorkInfoByIdLiveData(select.getId()).observe(MisAmigos.this, new Observer<WorkInfo>() {
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
                                username[0] = (String) jsonResultado.get("Username");
                                nombre[0] = (String) jsonResultado.get("Nombre");
                                String fotoPerfil = (String) jsonResultado.get("FotoPerfil");

                                byte[] encodeByte = Base64.getDecoder().decode(fotoPerfil);
                                bitmap[0] = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                                ItemListAmigo amigoAct = new ItemListAmigo(username[0], nombre[0], bitmap[0]);
                                itemListAmigos.add(amigoAct);

                                adapter = new ElAdaptadorRecyclerAmigos(itemListAmigos, MisAmigos.this);
                                lalista.setAdapter(adapter);
                                buscadorUsuarios.setOnQueryTextListener(MisAmigos.this);
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            });
            WorkManager.getInstance(MisAmigos.this).enqueue(select);
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
    public void itemClick(ItemListAmigo item) {
        Intent intent = new Intent(this, DetallesAmigo.class);
        intent.putExtra("itemDetail", item);
        startActivity(intent);
    }
}