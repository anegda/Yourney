package com.example.yourney;

import android.os.AsyncTask;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class TaskGetRutasGuardadas extends AsyncTask<Void, Void, ArrayList<JSONObject>> {

    private final String urlString;
    private RecyclerView recyclerView;
    private ElAdaptadorRecycler.RecyclerItemClick recyclerItemClick;
    List<ItemListRuta> items = new ArrayList<>();
    ElAdaptadorRecycler adapter;
    SearchView searchView;
    TextView placeholder;

    public TaskGetRutasGuardadas(String url, RecyclerView recyclerView, SearchView searchView, ElAdaptadorRecycler.RecyclerItemClick recyclerItemClick, TextView placeholder) {
        this.urlString = url;
        this.recyclerView = recyclerView;
        this.searchView = searchView;
        this.recyclerItemClick = recyclerItemClick;
        this.placeholder = placeholder;
    }

    @Override
    protected ArrayList<JSONObject> doInBackground(Void... voids) {

        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream input = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder stringBuilder = new StringBuilder();
            String row;

            // Obtengo un string con el resultado de la consulta
            while ( (row = reader.readLine()) != null ) {
                if (row.equals("Sin resultado")) {
                    return null;
                }
                stringBuilder.append(row);

            }

            // Lo transformo en un JSON para devolver los datos
            JSONParser parser = new JSONParser();
            JSONArray jsonResultado = (JSONArray) parser.parse(stringBuilder.toString());
            int i = 0;

            ArrayList<JSONObject> rutasGuardadas = new ArrayList<JSONObject>();
            while (i < jsonResultado.size()) {
                JSONObject unaRuta = (JSONObject) jsonResultado.get(i);

                // Devuelvo un ArrayList de JSON
                rutasGuardadas.add(unaRuta);
                i++;
            }
            System.out.println(rutasGuardadas);
            return rutasGuardadas;

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onPostExecute(ArrayList<JSONObject> rutasGuardadas) {
        if (rutasGuardadas == null) {
            // No ha habido resultado para la consulta --> no existen rutas publicas
            // Cambio la visibilidad del placeholder
            placeholder.setVisibility(View.VISIBLE);

        } else {
            // Asigno la info de los json devueltos a donde toque
            for (int i = 0; i < rutasGuardadas.size(); i++) {
                JSONObject row = (JSONObject) rutasGuardadas.get(i);
                System.out.println("***** " + row + " *****");
                // Vuelco la informacion en las variables

                String id = (String) row.get("IdRuta");
                String titulo = (String) row.get("Nombre");
                String descripcion = (String) row.get("Descripcion");
                String imagen = (String) row.get("FotoDesc");
                items.add(new ItemListRuta(id, titulo, descripcion, imagen));
            }
            System.out.println(items);
            adapter = new ElAdaptadorRecycler(items, recyclerItemClick);
            recyclerView.setAdapter(adapter);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    adapter.filter(s);
                    return false;
                }
            });

        }

    }
}
