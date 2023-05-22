package com.example.yourney;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.appcompat.app.AlertDialog;
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

public class SolicitudesRecibidas extends AppCompatActivity implements ElAdaptadorRecyclerAmigos.RecyclerItemClick, SearchView.OnQueryTextListener {
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

        // Llamada al asynctask
        String urlPeticiones = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selects.php";
        String urlUsuarios = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selectUsuarios.php";
        String paramsPeticiones = "?consulta=Peticiones&username=" + username;
        String paramsUsuarios = "?consulta=Usuarios&username=";
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.elreciclerview);
        TaskGetSolicitudesRecibidas taskGetPeticiones = new TaskGetSolicitudesRecibidas(urlPeticiones + paramsPeticiones, urlUsuarios + paramsUsuarios, recyclerView, SolicitudesRecibidas.this, username);
        taskGetPeticiones.execute();

        buscadorUsuarios.setOnQueryTextListener(SolicitudesRecibidas.this);

        ImageView añadirAmigo = (ImageView) findViewById(R.id.añadirAmigo);
        añadirAmigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SolicitudesRecibidas.this, EnviarSolicitud.class));
                finish();
            }
        });
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
        new AlertDialog.Builder(this).setIcon(R.drawable.logocolor).setTitle(getString(R.string.msg_aceptar_solicitud)).setMessage(item.getUsername()+": "+item.getNombre())
        .setPositiveButton(getString(R.string.si), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // OBTENGO EL USUARIO ACTUAL
                Sesion sesion = new Sesion(SolicitudesRecibidas.this);
                String username = sesion.getUsername();

                //ELIMINAMOS LA PETICIÓN
                Data datosPeti = new Data.Builder()
                        .putString("accion", "delete")
                        .putString("consulta", "Peticiones")
                        .putString("username1", item.getUsername())
                        .putString("username2", username)
                        .build();
                OneTimeWorkRequest deletePeticion = new OneTimeWorkRequest.Builder(ConexionBD.class)
                        .setInputData(datosPeti)
                        .build();
                WorkManager.getInstance(SolicitudesRecibidas.this).getWorkInfoByIdLiveData(deletePeticion.getId()).observe(SolicitudesRecibidas.this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        Log.d("DAS", "solicitud eliminada");
                    }
                });
                WorkManager.getInstance(SolicitudesRecibidas.this).enqueue(deletePeticion);

                //AÑADIMOS AMIGO
                Data datosAmistad = new Data.Builder()
                        .putString("accion", "insert")
                        .putString("consulta", "Amigos")
                        .putString("username1", item.getUsername())
                        .putString("username2", username)
                        .build();
                OneTimeWorkRequest insertAmigo = new OneTimeWorkRequest.Builder(ConexionBD.class)
                        .setInputData(datosAmistad)
                        .build();
                WorkManager.getInstance(SolicitudesRecibidas.this).getWorkInfoByIdLiveData(insertAmigo.getId()).observe(SolicitudesRecibidas.this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        Log.d("DAS", "amistad insertada");
                        startActivity(new Intent(SolicitudesRecibidas.this, SolicitudesRecibidas.class));
                        finish();
                    }
                });
                WorkManager.getInstance(SolicitudesRecibidas.this).enqueue(insertAmigo);
            }
        })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // OBTENGO EL USUARIO ACTUAL
                Sesion sesion = new Sesion(SolicitudesRecibidas.this);
                String username = sesion.getUsername();

                //ELIMINAMOS LA PETICIÓN
                Data datosPeti = new Data.Builder()
                        .putString("accion", "delete")
                        .putString("consulta", "Peticiones")
                        .putString("username1", item.getUsername())
                        .putString("username2", username)
                        .build();
                OneTimeWorkRequest deletePeticion = new OneTimeWorkRequest.Builder(ConexionBD.class)
                        .setInputData(datosPeti)
                        .build();
                WorkManager.getInstance(SolicitudesRecibidas.this).getWorkInfoByIdLiveData(deletePeticion.getId()).observe(SolicitudesRecibidas.this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        Log.d("DAS", "solicitud eliminada");
                        startActivity(new Intent(SolicitudesRecibidas.this, SolicitudesRecibidas.class));
                        finish();
                    }
                });
                WorkManager.getInstance(SolicitudesRecibidas.this).enqueue(deletePeticion);
            }
        })
        .show();
    }
}