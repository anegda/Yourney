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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MisEditores extends AppCompatActivity implements ElAdaptadorRecyclerEditor.RecyclerItemClick,SearchView.OnQueryTextListener {

    SearchView buscadorUsuarios;
    RecyclerView lalista;
    Button btnGuardar;
    List<ItemListEditor> items = new ArrayList<ItemListEditor>();
    ArrayList<String> editores = new ArrayList<String>();
    ElAdaptadorRecyclerEditor adapter = new ElAdaptadorRecyclerEditor(items, MisEditores.this);
    private Integer idRuta;
    private String parent;
    TextView placeholder;
    private String parent2;

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
        setContentView(R.layout.activity_mis_editores);

        parent = getIntent().getStringExtra("parent");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            idRuta = extras.getInt("idRuta", 0);
            parent = extras.getString("parent");
            parent2 = extras.getString("parent2");
        }

        buscadorUsuarios = findViewById(R.id.buscar_editores);
        lalista = findViewById(R.id.elreciclerview);
        btnGuardar = findViewById(R.id.btnGuardarEditores);
        placeholder = findViewById(R.id.placeholder);
        placeholder.setVisibility(View.GONE);

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        lalista.setLayoutManager(manager);

        // OBTENGO EL USUARIO ACTUAL
        Sesion sesion = new Sesion(this);
        String username = sesion.getUsername();

        ArrayList<String> amigos = new ArrayList<String>();

        // Llamada al asynctask
        String urlAmigos = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selects.php";
        String urlUsuarios = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selectUsuarios.php";
        String paramsAmigos = "?consulta=Amigos&username=" + username;
        String paramsUsuarios = "?consulta=Usuarios&username=";
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.elreciclerview);
        TaskGetEditores taskGetAmigos = new TaskGetEditores(urlAmigos + paramsAmigos,urlUsuarios + paramsUsuarios, buscadorUsuarios, recyclerView, MisEditores.this, username, placeholder);
        taskGetAmigos.execute();

        buscadorUsuarios.setOnQueryTextListener(MisEditores.this);

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MisEditores.this, EditarRuta.class);
                intent.putExtra("editores", editores);

                intent.putExtra("tituloRuta", getIntent().getStringExtra("tituloRuta"));
                intent.putExtra("dificultadRuta", getIntent().getIntExtra("dificultadRuta", 0));
                intent.putExtra("infoRuta", getIntent().getStringExtra("infoRuta"));
                intent.putExtra("visibilidadRuta", getIntent().getIntExtra("visibilidadRuta", 0));
                intent.putExtra("idRuta", idRuta);
                intent.putExtra("parent", "VerRuta");
                intent.putExtra("parentVerRuta", parent);

                startActivity(intent);
            }
        });

    }

    /**
     * Metodo para probar si funciona el reciclerView, luego se cambiara por una llamada a la BD
     **/
    private List<ItemListEditor> getItems(ArrayList<String> amigos) {
        List<ItemListEditor> itemListAmigos = new ArrayList<>();
        for (String amigo : amigos) {
            ItemListEditor amigoAct = new ItemListEditor(amigo, "prueba", "prueba", false);
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
        if (item.isChecked) {
            for (int i = 0; i < editores.size(); i++) {
                if (editores.get(i) == item.getUsername()) {
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // Vuelvo a la actividad que toque
        Intent intent = new Intent(MisEditores.this, EditarRuta.class);
        intent.putExtra("tituloRuta", getIntent().getStringExtra("tituloRuta"));
        intent.putExtra("dificultadRuta", getIntent().getIntExtra("dificultadRuta", 1));
        intent.putExtra("infoRuta", getIntent().getStringExtra("infoRuta"));
        intent.putExtra("visibilidadRuta", getIntent().getIntExtra("visibilidadRuta", 1));
        intent.putExtra("idRuta", idRuta);
        intent.putExtra("parent", parent);
        if(parent.equals("VerRuta")){
            intent.putExtra("parentVerRuta", getIntent().getStringExtra("parentVerRuta"));
        }
        startActivity(intent);
        finish();
    }
}