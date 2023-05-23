package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class MisAmigos extends AppCompatActivity implements ElAdaptadorRecyclerAmigos.RecyclerItemClick, SearchView.OnQueryTextListener {
    SearchView buscadorUsuarios;
    RecyclerView lalista;
    private List<ItemListAmigo> items = new ArrayList<ItemListAmigo>();
    ElAdaptadorRecyclerAmigos adapter = new ElAdaptadorRecyclerAmigos(items, MisAmigos.this);
    TextView placeholder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Cargo la pagina en el idioma elegido
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Locale nuevaloc;
        if (prefs.getString("idiomaPref", "1").equals("2")) {
            nuevaloc = new Locale("en");
        } else {
            nuevaloc = new Locale("es");
        }

        Locale.setDefault(nuevaloc);
        Configuration configuration = getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);

        Context context = getBaseContext().createConfigurationContext(configuration);
        getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_amigos);

        buscadorUsuarios = findViewById(R.id.search_friends);
        lalista = findViewById(R.id.elreciclerview);

        LinearLayoutManager manager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lalista.setLayoutManager(manager);

        placeholder = findViewById(R.id.placeholder);
        placeholder.setVisibility(View.GONE);

        // OBTENGO EL USUARIO ACTUAL
        Sesion sesion = new Sesion(this);
        String username = sesion.getUsername();

        // Llamada al asynctask
        String urlAmigos = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selects.php";
        String urlUsuarios = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selectUsuarios.php";
        String paramsAmigos = "?consulta=Amigos&username=" + username;
        String paramsUsuarios = "?consulta=Usuarios&username=";
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.elreciclerview);
        TaskGetAmigos taskGetAmigos = new TaskGetAmigos(urlAmigos + paramsAmigos,urlUsuarios + paramsUsuarios, buscadorUsuarios, recyclerView, MisAmigos.this, username, placeholder);
        taskGetAmigos.execute();

        buscadorUsuarios.setOnQueryTextListener(MisAmigos.this);

        ImageView añadirAmigo = findViewById(R.id.añadirAmigo);
        añadirAmigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogoPeticion();
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
        Intent intent = new Intent(this, DetallesAmigo.class);
        intent.putExtra("username", item.getUsername());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        // Vuelvo a la actividad Main
        super.onBackPressed();
        Intent intent = new Intent(MisAmigos.this, MainActivity.class);
        startActivity(intent);
        // Termino esta actividad
        finish();
    }

    void showDialogoPeticion() {
        // Creo el dialogo de login con el layout
        Dialog dialog = new Dialog(this, R.style.RoundedDialogStyle);
        dialog.setContentView(R.layout.dialog_peticion);
        dialog.setCancelable(true);

        Button btn_enviar = dialog.findViewById(R.id.btn_enviarSolicitud);
        btn_enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Cogemos del campo el nombre de usuario / mensaje / usuario actual
                EditText userEdit = dialog.findViewById(R.id.usuarioEdit);
                String userPeti = userEdit.getText().toString();
                EditText mensajeEdit = dialog.findViewById(R.id.mensajeEdit);
                String mensaje = mensajeEdit.getText().toString();
                Sesion sesion = new Sesion(MisAmigos.this);
                String username = sesion.getUsername();

                // Comprobar que los campos no estén vacíos
                if (mensaje.equals("") || userPeti.equals("")) {
                    Toast.makeText(MisAmigos.this, R.string.campos_vacios, Toast.LENGTH_LONG).show();
                }
                else if(userPeti.equals(username)){
                    Toast.makeText(MisAmigos.this, R.string.mensaje_error_peti, Toast.LENGTH_LONG).show();
                }
                else{
                    Data datosLegal = new Data.Builder()
                            .putString("accion", "select")
                            .putString("consulta", "PeticionLegal")
                            .putString("username", username)
                            .putString("username2", userPeti)
                            .build();

                    // Peticion al Worker
                    OneTimeWorkRequest selectLegal = new OneTimeWorkRequest.Builder(ConexionBD.class)
                            .setInputData(datosLegal)
                            .build();

                    WorkManager.getInstance(MisAmigos.this).getWorkInfoByIdLiveData(selectLegal.getId()).observe(MisAmigos.this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(WorkInfo workInfo) {
                            if (workInfo != null && workInfo.getState().isFinished()) {
                                Data output = workInfo.getOutputData();
                                if (output.getString("resultado").equals("Con resultados")) {
                                    Toast.makeText(MisAmigos.this, getString(R.string.existe_amistad_peticion), Toast.LENGTH_SHORT).show();
                                    dialog.cancel();
                                }else{
                                    // Comprobar credenciales contra la BD
                                    Data datos = new Data.Builder()
                                            .putString("accion", "selectUsuario")
                                            .putString("consulta", "Usuarios")
                                            .putString("username", userPeti)
                                            .build();

                                    // Peticion al Worker
                                    OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                            .setInputData(datos)
                                            .build();

                                    WorkManager.getInstance(MisAmigos.this).getWorkInfoByIdLiveData(select.getId()).observe(MisAmigos.this, new Observer<WorkInfo>() {
                                        @Override
                                        public void onChanged(WorkInfo workInfo) {
                                            // Gestiono la respuesta de la peticion
                                            if (workInfo != null && workInfo.getState().isFinished()) {
                                                Data output = workInfo.getOutputData();
                                                if (!output.getString("resultado").equals("Sin resultado")) {
                                                    //si el usuario existe hacemos el insert

                                                    Data datosInsert = new Data.Builder()
                                                            .putString("accion", "insert")
                                                            .putString("consulta", "Peticiones")
                                                            .putString("username1", username)
                                                            .putString("username2", userPeti)
                                                            .putString("mensaje", mensaje)
                                                            .putInt("estado", 0)
                                                            .build();

                                                    OneTimeWorkRequest insert = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                                            .setInputData(datosInsert)
                                                            .build();

                                                    WorkManager.getInstance(MisAmigos.this).getWorkInfoByIdLiveData(insert.getId()).observe(MisAmigos.this, new Observer<WorkInfo>() {
                                                        @Override
                                                        public void onChanged(WorkInfo workInfo) {
                                                            Toast.makeText(MisAmigos.this, getString(R.string.solicitud_enviada), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                    WorkManager.getInstance(MisAmigos.this).enqueue(insert);

                                                    //mientras también cogemos el token del receptor
                                                    Data datosSelectToken = new Data.Builder()
                                                            .putString("accion", "select")
                                                            .putString("consulta", "Tokens")
                                                            .putString("username", userPeti)
                                                            .build();

                                                    OneTimeWorkRequest selectToken = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                                            .setInputData(datosSelectToken)
                                                            .build();
                                                    WorkManager.getInstance(MisAmigos.this).getWorkInfoByIdLiveData(selectToken.getId()).observe(MisAmigos.this, new Observer<WorkInfo>() {
                                                        @Override
                                                        public void onChanged(WorkInfo workInfo) {
                                                            if (workInfo != null && workInfo.getState().isFinished()) {
                                                                Data output = workInfo.getOutputData();
                                                                if (!output.getString("resultado").equals("Sin resultado")) {
                                                                    //si el usuario está logeado en algún dispositivo
                                                                    JSONParser parser = new JSONParser();
                                                                    try {
                                                                        JSONArray jsonResultado = (JSONArray) parser.parse(output.getString("resultado"));

                                                                        Integer i = 0;
                                                                        System.out.println("***** " + jsonResultado + " *****");
                                                                        while (i < jsonResultado.size()) {
                                                                            JSONObject row = (JSONObject) jsonResultado.get(i);
                                                                            System.out.println("***** " + row + " *****");
                                                                            String Token = (String) row.get("Token");

                                                                            Data datosNoti = new Data.Builder()
                                                                                    .putString("accion", "notifPeticion")
                                                                                    .putString("emisor", username)
                                                                                    .putString("receptor", Token)
                                                                                    .putString("mensaje", mensaje)
                                                                                    .build();
                                                                            OneTimeWorkRequest notif = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                                                                    .setInputData(datosNoti)
                                                                                    .build();
                                                                            WorkManager.getInstance(MisAmigos.this).getWorkInfoByIdLiveData(notif.getId()).observe(MisAmigos.this, new Observer<WorkInfo>() {
                                                                                @Override
                                                                                public void onChanged(WorkInfo workInfo) {
                                                                                    dialog.cancel();
                                                                                }
                                                                            });
                                                                            WorkManager.getInstance(MisAmigos.this).enqueue(notif);
                                                                            i = i + 1;
                                                                        }
                                                                    } catch (ParseException e) {
                                                                        throw new RuntimeException(e);
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    });
                                                    WorkManager.getInstance(MisAmigos.this).enqueue(selectToken);

                                                } else {
                                                    Toast.makeText(MisAmigos.this, R.string.peticion_incorrecta, Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    });
                                    WorkManager.getInstance(MisAmigos.this).enqueue(select);
                                }
                            }
                        }
                    });
                    WorkManager.getInstance(MisAmigos.this).enqueue(selectLegal);
                }
            }
        });

        dialog.show();
    }
}