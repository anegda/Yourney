package com.example.yourney;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import androidx.recyclerview.widget.GridLayoutManager;
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


public class TaskGetImagenes extends AsyncTask<Void, Void, ArrayList<JSONObject>> {

    private final String urlString;
    private RecyclerView imagesRV;
    private Context context;

    public TaskGetImagenes(String url, RecyclerView imagesRV, Context context) {
        this.urlString = url;
        this.imagesRV = imagesRV;
        this.context = context;
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

            System.out.println(stringBuilder.toString());

            // Lo transformo en un JSON para devolver los datos
            JSONParser parser = new JSONParser();
            JSONArray jsonResultado = (JSONArray) parser.parse(stringBuilder.toString());
            int i = 0;

            ArrayList<JSONObject> misImagenes = new ArrayList<JSONObject>();
            while (i < jsonResultado.size()) {
                JSONObject item = (JSONObject) jsonResultado.get(i);

                // Devuelvo un ArrayList de JSON
                misImagenes.add(item);
                i++;
            }
            System.out.println(misImagenes);
            return misImagenes;

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onPostExecute(ArrayList<JSONObject> misImagenes) {
        if (misImagenes == null) {
            // No ha habido resultado para la consulta --> no existen imagenes para esa ruta


        } else {
            // Asigno la info de los json devueltos a donde toque

            for (int i = 0; i < misImagenes.size(); i++) {
                JSONObject row = (JSONObject) misImagenes.get(i);

                // Obtengo los datos
                String idImg = (String) row.get("IdImg");
                String imgString = (String) row.get("ImgBlob");

                GaleriaFotosRuta.idImgList.add(Integer.parseInt(idImg));
                GaleriaFotosRuta.imageBlobs.add(imgString);

            }

            GaleriaFotosRuta.imageRVAdapter = new RecyclerViewAdapter(context, GaleriaFotosRuta.idImgList, GaleriaFotosRuta.imageBlobs);
            GridLayoutManager manager = new GridLayoutManager(context, 4);
            imagesRV.setLayoutManager(manager);
            imagesRV.setAdapter(GaleriaFotosRuta.imageRVAdapter);
        }
    }
}
