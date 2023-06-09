package com.example.yourney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ElAdaptadorRecycler.RecyclerItemClick {
    public ActionBarDrawerToggle  actionBarDrawerToggle;
    private ArrayList<String> nombreImagenes = new ArrayList<String>();
    List<ItemListRuta> items = new ArrayList<>();
    String titulo, descripcion, imagen = "";
    static String fotoPerfil;
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
        setContentView(R.layout.activity_main);

        RecyclerView lalista= findViewById(R.id.elreciclerview);

        //AÑADIMOS EL ACTION BAR Y EL NAVIGATIONDRAWER
        setSupportActionBar(findViewById(R.id.labarra));

        Toolbar toolbar = findViewById(R.id.labarra);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Oculta el título por defecto
        }


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

        //REALIZAMOS EL SELECT NECESARIO PARA ESTABLECER FOTO DE PERFIL
        Data datosUsuarios = new Data.Builder()
                .putString("accion", "selectUsuario")
                .putString("consulta", "Usuarios")
                .putString("username", sesion.getUsername())
                .build();
        OneTimeWorkRequest selectUsuario = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datosUsuarios)
                .build();
        WorkManager.getInstance(MainActivity.this).getWorkInfoByIdLiveData(selectUsuario.getId()).observe(MainActivity.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if (workInfo != null && workInfo.getState().isFinished()) {
                    if(fotoPerfil!=null) {
                        ImageView fotoPerfil_IV = (ImageView) viewHeader.findViewById(R.id.fotoperfil);
                        byte[] encodeBytes = Base64.getDecoder().decode(fotoPerfil);
                        Bitmap foto = BitmapFactory.decodeByteArray(encodeBytes, 0, encodeBytes.length);
                        fotoPerfil_IV.setImageBitmap(foto);
                    }
                }
            }
        });
        WorkManager.getInstance(MainActivity.this).enqueue(selectUsuario);

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
                       // Cierro el drawer
                       DrawerLayout drawer = findViewById(R.id.drawer_layout);
                       drawer.closeDrawer(GravityCompat.START);
                       // Desplegamos el dialogo de cerrar sesion
                       DialogFragment dialogoCerrarSesion = new DialogoCerrarSesion();
                       dialogoCerrarSesion.show(getSupportFragmentManager(), "cerrar_sesion");
                       break;
                   default:
                       break;
               }
               elmenudesplegable.closeDrawers();
               return false;
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView fotoPerfil = (ImageView) viewHeader.findViewById(R.id.fotoperfil);
        fotoPerfil.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, EditarPerfil.class));
                finish();
            }
        });

        //AÑADIR LA FUNCIONALIDAD AL BOTÓN FLOTANTE DE GRABAR
        ImageView btn_2Add = findViewById(R.id.button2);
        btn_2Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, GrabarRuta.class));
                finish();
            }
        });

        //COMPROBAMOS SI EXISTE CONEXIÓN A INTERNET
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||  connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
        if(connected) {
            // Llamada al AsyncTask
            String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selectRutas.php";
            String params = "?consulta=MisRutas&username=" + sesion.getUsername();
            System.out.println(url + params);
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.elreciclerview);
            TextView placeHolder = (TextView) findViewById(R.id.tvDescrRutPers);
            TaskGetMisRutas taskGetMisRutas = new TaskGetMisRutas(url + params, recyclerView, MainActivity.this);
            taskGetMisRutas.execute();
        }else{
            Toast.makeText(this, getString(R.string.error_conexión), Toast.LENGTH_LONG).show();
        }

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

        Drawable navigationIcon = toolbar.getNavigationIcon();
        // Cambia el color del icono del desplegable
        if (navigationIcon != null) {
            navigationIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            toolbar.setNavigationIcon(navigationIcon);
        }
    }

    @Override
    public void itemClick(ItemListRuta item) {
        Intent intent = new Intent(this, VerRuta.class);
        int idRuta = Integer.parseInt(item.getId());
        intent.putExtra("idRuta", idRuta);
        intent.putExtra("parent", "Main");
        Log.d("DAS", String.valueOf(idRuta));
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // Si esta abierto el Drawer lo cierro, si no despliego el mensaje de cerrar sesion
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
           System.exit(0);
        }
    }
}