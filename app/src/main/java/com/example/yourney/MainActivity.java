package com.example.yourney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
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
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ElAdaptadorRecycler.RecyclerItemClick {

    public ActionBarDrawerToggle  actionBarDrawerToggle;

   private ArrayList<String> nombreImagenes = new ArrayList<String>();
    List<ItemListRuta> items = new ArrayList<>();
    String titulo, descripcion, imagen = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView lalista= findViewById(R.id.elreciclerview);

        //AÑADIMOS EL ACTION BAR Y EL NAVIGATIONDRAWER
        setSupportActionBar(findViewById(R.id.labarra));

        final DrawerLayout elmenudesplegable = findViewById(R.id.drawer_layout);
        NavigationView elnavigation = findViewById(R.id.elnavigationview);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, elmenudesplegable, R.string.nav_open, R.string.nav_close);
        elmenudesplegable.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();


        //AÑADIMOS EL NOMBRE Y LA FOTO DE PEFIL DEL USUARIO QUE HA INICIADO SESION
        Sesion sesion = new Sesion(this);

        //CONSEGUIMOS EL HEADER DEL NAVIGATION VIEW
        View viewHeader = elnavigation.getHeaderView(0);

        TextView username = (TextView) viewHeader.findViewById(R.id.usuario);
        username.setText(sesion.getUsername());

        ImageView fotoPerfil = (ImageView) viewHeader.findViewById(R.id.fotoperfil);


        elnavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
               switch (item.getItemId()){
                   case R.id.buscarRutas:
                       startActivity(new Intent(MainActivity.this, PublicRoutesActivity.class));
                       finish();
                       break;
                   case R.id.rutasFavoritas:
                       startActivity(new Intent(MainActivity.this, RutasFavoritas.class));
                       finish();
                       break;
                   case R.id.crearRuta:
                       startActivity(new Intent(MainActivity.this, GrabarRuta.class));
                       finish();
                       break;
                   case R.id.solicitudesAmistad:
                       startActivity(new Intent(MainActivity.this, SolicitudesRecibidas.class));
                       finish();
                       break;
                   case R.id.misAmigos:
                       startActivity(new Intent(MainActivity.this, MisAmigos.class));
                       finish();
                       break;
                   case R.id.editarPerfil:
                       startActivity(new Intent(MainActivity.this, EditarPerfil.class));
                       finish();
                       break;
                   case R.id.Preferencias:
                       startActivity(new Intent(MainActivity.this, Ajustes.class));
                       finish();
                       break;
                   case R.id.CerrarSesion:
                       //FINALIZAMOS LA SESION
                       Sesion sesion = new Sesion(MainActivity.this);
                       sesion.deleteUsername();
                       //VOLVEMOS A LA PANTALLA DE INICIO
                       startActivity(new Intent(MainActivity.this, LoginRegisterActivity.class));
                       finish();
                       break;
                   default:
                       break;
               }
               elmenudesplegable.closeDrawers();
               return false;
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //AÑADIR LA FUNCIONALIDAD AL BOTÓN FLOTANTE DE GRABAR
        ImageView btn_2Add = findViewById(R.id.button2);
        btn_2Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, GrabarRuta.class));
                finish();
            }
        });

        /*************************** ruta a mostrar de ejemplo **********************************

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.fotoruta);

         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
         byte[] byteArray = byteArrayOutputStream.toByteArray();
         String fotoen64 = new String(Base64.getEncoder().encode(byteArray));

        String foto = fotoen64;
        String nombre = "Ruta Ejemplo";
        String descr = "Este es un ejemplo de una ruta creada por defecto. Así se van a ver el resto de rutas que crees a lo largo de tu aventura";
        ItemListRuta rutaEjemplo = new ItemListRuta(foto, nombre, descr);
        items.add(rutaEjemplo);

         /****************************************************************************************

        /*
         // Consulto la BD por mis rutas
        Sesion sesion = new Sesion(this);

        Data datos = new Data.Builder()
                .putString("accion", "selectRuta")
                .putString("consulta", "MisRutas")
                .putString("username", sesion.getUsername())
                .build();


        OneTimeWorkRequest select = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datos)
                .build();

        System.out.println("Main Activity");

        WorkManager.getInstance(MainActivity.this).getWorkInfoByIdLiveData(select.getId()).observe(MainActivity.this, new Observer<WorkInfo>() {
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
                            while (i < jsonResultado.size()) {
                                JSONObject row = (JSONObject) jsonResultado.get(i);
                                // Vuelco la informacion en las variables creadas anteriormente
                                titulo = (String) row.get("FotoDesc");
                                descripcion = (String) row.get("Nombre");
                                imagen = (String) row.get("Descripcion");
                                getItems(titulo, descripcion, imagen);
                                i++;
                            }

                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        });
        WorkManager.getInstance(MainActivity.this).enqueue(select);
        */

        /****************************************************************************************/

        // Llamada al AsyncTask
        String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selectRutas.php";
        String params = "?consulta=MisRutas&username=" + sesion.getUsername();
        System.out.println(url + params);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.elreciclerview);
        TextView placeHolder = (TextView) findViewById(R.id.tvDescrRutPers);
        TaskGetMisRutas taskGetMisRutas = new TaskGetMisRutas(url + params, recyclerView, MainActivity.this, placeHolder);
        taskGetMisRutas.execute();


        ArrayList<Integer> imagenes = new ArrayList<Integer>();
        for (String nombreImagen : nombreImagenes) {
            int id = getResources().getIdentifier(nombreImagen, "drawable", getPackageName());
            imagenes.add(id);
        }

        ElAdaptadorRecycler eladaptador = new ElAdaptadorRecycler(items, this);
        lalista.setAdapter(eladaptador);

        /** definir el aspecto del RecyclerView --> horizontal, vertical, grid... **/
        LinearLayoutManager elLayoutLineal= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lalista.setLayoutManager(elLayoutLineal);

    }

    @Override
    public void itemClick(ItemListRuta item) {
        Intent intent = new Intent(this, DetallesRuta.class);
        intent.putExtra("itemDetail", item);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}