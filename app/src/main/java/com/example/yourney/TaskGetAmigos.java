package com.example.yourney;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.Base64;
import java.util.List;


public class TaskGetAmigos extends AsyncTask<Void, Void, ArrayList<JSONObject>> {

    private final String urlStringAmigos;
    private final String urlStringUsuarios;     // A falta de añadir el username al final
    private RecyclerView recyclerView;
    private ElAdaptadorRecyclerAmigos.RecyclerItemClick recyclerItemClick;
    private String username;
    List<ItemListAmigo> items = new ArrayList<ItemListAmigo>();
    ElAdaptadorRecyclerAmigos adapter;

    public TaskGetAmigos(String urlAmigos, String urlUsuarios, RecyclerView recyclerView, ElAdaptadorRecyclerAmigos.RecyclerItemClick recyclerItemClick, String username) {
        this.urlStringAmigos = urlAmigos;
        this.urlStringUsuarios = urlUsuarios;
        this.recyclerView = recyclerView;
        this.recyclerItemClick = recyclerItemClick;
        this.username = username;
    }

    @Override
    protected ArrayList<JSONObject> doInBackground(Void... voids) {

        try {
            /***** PRIMERA PETICION - OBTENER LISTA DE AMIGOS *****/
            URL url = new URL(urlStringAmigos);
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
                    // Se devuelve null --> la consulta no ha tenido resultados
                    return null;
                }
                stringBuilder.append(row);
            }

            // Lo transformo en un JSON
            JSONParser parser = new JSONParser();
            JSONArray jsonResultado = (JSONArray) parser.parse(stringBuilder.toString());

            // Lista en la que almaceno los amigos del usuario
            ArrayList<String> amigos = new ArrayList<String>();
            String user1;
            String user2;

            // Recorro el resultado devuelto
            for (int i = 0; i < jsonResultado.size(); i++) {
                JSONObject item = (JSONObject) jsonResultado.get(i);

                // Obtengo los dos nombres de usuario de la fila de la tabla y guardo el que no es el del usuario logueado
                user1 = (String) item.get("Username1");
                user2 = (String) item.get("Username2");

                if (!user1.equals(username)) {
                    amigos.add(user1);
                } else {
                    amigos.add(user2);
                }
            }
            System.out.println(amigos);


            /***** SEGUNDA PETICION - OBTENER INFO DE LOS AMIGOS *****/

            ArrayList<JSONObject> amigosList = new ArrayList<JSONObject>();

            // Recorro la lista de amigos y obtengo la info de cada uno de ellos
            for (int i = 0; i < amigos.size(); i++) {
                URL url2 = new URL(urlStringUsuarios + amigos.get(i));
                HttpURLConnection urlConnection2 = (HttpURLConnection) url2.openConnection();
                urlConnection2.setRequestMethod("GET");
                urlConnection2.connect();

                InputStream input2 = urlConnection2.getInputStream();
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(input2));
                StringBuilder stringBuilder2 = new StringBuilder();
                String row2;

                // Obtengo un string con el resultado de la consulta
                while ( (row2 = reader2.readLine()) != null ) {
                    if (row2.equals("Sin resultado")) {
                        // Se devuelve null --> la consulta no ha tenido resultados
                        return null;
                    }
                    stringBuilder2.append(row2);
                }

                // Lo transformo en un JSON para devolver los datos
                parser = new JSONParser();
                JSONObject resultado = (JSONObject) parser.parse(stringBuilder2.toString());
                amigosList.add(resultado);

            }

            return amigosList;

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onPostExecute(ArrayList<JSONObject> misAmigos) {
        if (misAmigos == null) {
            // No ha habido resultado para la consulta --> el usuario no tiene amigos registrados


        } else {
            // Asigno la info de los json devueltos a donde toque
            for (int i = 0; i < misAmigos.size(); i++) {
                JSONObject row = (JSONObject) misAmigos.get(i);
                System.out.println("***** " + row + " *****");

                // Extraigo la informacion
                String userAmigo = (String) row.get("Username");
                String nombre = (String) row.get("Nombre");
                String fotoString = (String) row.get("FotoPerfil");

                // Decodifico la imagen
                byte[] encodeBytes = Base64.getDecoder().decode(fotoString);
                Bitmap fotoPerfil = BitmapFactory.decodeByteArray(encodeBytes, 0, encodeBytes.length);

                items.add(new ItemListAmigo(userAmigo, nombre, fotoPerfil));
            }

            adapter = new ElAdaptadorRecyclerAmigos(items, recyclerItemClick);
            recyclerView.setAdapter(adapter);
        }
    }
}
