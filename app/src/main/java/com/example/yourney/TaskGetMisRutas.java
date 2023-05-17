package com.example.yourney;

import android.os.AsyncTask;

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


public class TaskGetMisRutas extends AsyncTask<Void, Void, ArrayList<JSONObject>> {

    private final String urlString;
    private RecyclerView recyclerView;
    private ElAdaptadorRecycler.RecyclerItemClick recyclerItemClick;
    List<ItemListRuta> items = new ArrayList<>();
    ElAdaptadorRecycler adapter;

    public TaskGetMisRutas(String url, RecyclerView recyclerView, ElAdaptadorRecycler.RecyclerItemClick recyclerItemClick) {
        this.urlString = url;
        this.recyclerView = recyclerView;
        this.recyclerItemClick = recyclerItemClick;
    }

    @Override
    protected ArrayList<JSONObject> doInBackground(Void... voids) {

        try {
            System.out.println(urlString);
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream input = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            StringBuilder stringBuilder = new StringBuilder();
            String row;

            ArrayList<JSONObject> misRutas = new ArrayList<JSONObject>();

            // Obtengo un string con el resultado de la consulta
            while ( (row = reader.readLine()) != null ) {
                System.out.println(row);
                stringBuilder.append(row);

                if (row.equals("Sin resultado")) {
                    // Se devuelve la lista vacia --> la consulta no ha tenido resultados
                    return misRutas;
                }
            }

            // Lo transformo en un JSON para devolver los datos
            JSONParser parser = new JSONParser();
            JSONArray jsonResultado = (JSONArray) parser.parse(stringBuilder.toString());
            int i = 0;


            while (i < jsonResultado.size()) {
                JSONObject unaRuta = (JSONObject) jsonResultado.get(i);

                // Tambien puedo devolver un ArrayList de JSON???
                misRutas.add(unaRuta);
                i++;
            }
            System.out.println(misRutas);
            return misRutas;

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onPostExecute(ArrayList<JSONObject> misRutas) {
        if (misRutas.isEmpty()) {
            // No ha habido resultado para la consulta --> el usuario no ha creado ninguna ruta


        } else {
            // Asigno la info de los json devueltos a donde toque

            for (int i = 0; i < misRutas.size(); i++) {
                JSONObject row = (JSONObject) misRutas.get(i);
                System.out.println("***** " + row + " *****");
                // Vuelco la informacion en las variables creadas anteriormente

                String titulo = (String) row.get("Nombre");
                String descripcion = (String) row.get("Descripcion");
                String imagen = (String) row.get("FotoDesc");
                items.add(new ItemListRuta(titulo, descripcion, imagen));
            }
            System.out.println(items);
            adapter = new ElAdaptadorRecycler(items, recyclerItemClick);
            recyclerView.setAdapter(adapter);

        }
    }
}
