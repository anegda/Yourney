package com.example.yourney;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class ConexionBD extends Worker {

    public ConexionBD(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data datos = this.getInputData();
        String accion = datos.getString("accion");

        switch (accion) {
            case "insert":
                System.out.println("***** INSERT *****");
                Data outputInsert = insert();
                return Result.success(outputInsert);

            case "update":
                System.out.println("***** UPDATE *****");
                Data outputUpdate = update();
                return Result.success(outputUpdate);

            case "selectRuta":
                System.out.println("***** SELECT RUTA *****");
                Data outputSelectRuta = selectRuta();
                return Result.success(outputSelectRuta);

            case "select":
                System.out.println("***** SELECT *****");
                Data outputSelect = select();
                return Result.success(outputSelect);

            case "delete":
                System.out.println("***** DELETE *****");
                Data outputDelete = delete();
                return Result.success(outputDelete);

            default:
                return Result.failure();
        }
    }

    public Data insert() {
        String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/inserts.php";
        HttpURLConnection urlConnection = null;

        Data datos = this.getInputData();

        try {
            URL urlFinal = new URL(url);
            urlConnection = (HttpURLConnection) urlFinal.openConnection();
            Data output = null;
            // Peticion POST
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");

            JSONObject params = new JSONObject();

            // Miro a que tabla se realiza la consulta
            String consulta = datos.getString("consulta");
            params.put("consulta", consulta);
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());

            switch (consulta) {
                case "Usuarios":

                    params.put("username", datos.getString("username"));
                    params.put("nombre", datos.getString("nombre"));
                    params.put("apellidos", datos.getString("apellidos"));
                    params.put("password", datos.getString("password"));
                    params.put("email", datos.getString("email"));
                    params.put("fotoPerfil", RegisterActivity2.fotoen64);

                    out.print(params.toString());
                    out.close();

                    break;

                case "Rutas":

                    params.put("nombre", datos.getString("nombre"));
                    params.put("descripcion", datos.getString("descripcion"));
                    //params.put("fotoDesc", datos.getString("fotoDesc"));
                    params.put("duracion", datos.getInt("duracion", 0));
                    params.put("distancia", datos.getInt("distancia", 0));
                    params.put("pasos", datos.getString("pasos"));
                    params.put("dificultad", datos.getString("dificultad"));
                    params.put("fecha", datos.getString("fecha"));
                    params.put("visibilidad", datos.getInt("visibilidad", 1));
                    params.put("creador", datos.getString("creador"));

                    out.print(params.toString());
                    out.close();

                    int status = urlConnection.getResponseCode();
                    System.out.println(status);
                    if (status == 200) {
                        // La peticion ha tenido exito
                        BufferedInputStream input = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                        String row = "";
                        String resultado = "";

                        // Recorro las lineas devueltas por la peticion SQL
                        while ((row = reader.readLine()) != null) {
                            resultado += row;
                        }
                        input.close();

                        output = new Data.Builder().putString("resultado", resultado).build();
                        return output;
                    }
                    output = new Data.Builder().putString("resultado", "Sin resultado").build();
                    return output;

                case "Ubicaciones":

                    params.put("idRuta", datos.getInt("idRuta", 0));
                    params.put("altitud", datos.getDouble("altitud", 0));
                    params.put("longitud", datos.getDouble("longitud", 999));
                    params.put("latitud", datos.getDouble("latitud", 999));

                    out.print(params.toString());
                    out.close();

                    break;

                case "Editores":

                    params.put("idRuta", datos.getInt("idRuta", 0));
                    params.put("username", datos.getString("username"));

                    out.print(params.toString());
                    out.close();

                    break;

                case "Amigos":

                    params.put("username1", datos.getString("username1"));
                    params.put("username2", datos.getString("username2"));

                    out.print(params.toString());
                    out.close();

                    break;

                case "Peticiones":

                    params.put("username1", datos.getString("username1"));
                    params.put("username2", datos.getString("username2"));
                    params.put("mensaje", datos.getString("mensaje"));
                    params.put("estado", datos.getString("estado"));

                    out.print(params.toString());
                    out.close();

                    break;

                case "Imagenes":

                    params.put("idRuta", datos.getInt("idRuta", 0));
                    params.put("username", datos.getString("username"));
                    //String imgBlob = datos.getString("imgBlob");

                    out.print(params.toString());
                    out.close();

                    break;

                case "RutasGuardadas":

                    params.put("idRuta", datos.getInt("idRuta", 0));
                    params.put("username", datos.getString("username"));

                    out.print(params.toString());
                    out.close();

                    break;

                case "Tokens":

                    params.put("username", datos.getString("username"));
                    params.put("token", datos.getString("token"));

                    out.print(params.toString());
                    out.close();

                default:
                    break;
            }

            int status = urlConnection.getResponseCode();
            System.out.println(status);
            if (status == 200) {
                output = new Data.Builder().putBoolean("resultado", true).build();
                return output;
            } else {
                output = new Data.Builder().putBoolean("resultado", false).build();
                return output;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Data update() {
        String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/updates.php";
        HttpURLConnection urlConnection = null;

        Data datos = this.getInputData();

        try {
            URL urlFinal = new URL(url);
            urlConnection = (HttpURLConnection) urlFinal.openConnection();
            Data output = null;
            // Peticion POST
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");

            JSONObject params = new JSONObject();

            // Miro a que tabla se realiza la consulta
            String consulta = datos.getString("consulta");
            params.put("consulta", consulta);
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());

            switch(consulta) {
                case "Usuarios":

                    params.put("usernameOld", datos.getString("usernameOld"));
                    params.put("usernameNew", datos.getString("usernameNew"));
                    //params.put("fotoPerfil", datos.getString("fotoPerfil"));

                    out.print(params.toString());
                    out.close();

                    break;

                case "Rutas":

                    params.put("idRuta", datos.getInt("idRuta", 0));
                    params.put("nombre", datos.getString("nombre"));
                    params.put("descripcion", datos.getString("descripcion"));
                    //params.put("fotoDesc", datos.getString("fotoDesc"));
                    params.put("visibilidad", datos.getInt("visibilidad", 1));

                    out.print(params.toString());
                    out.close();

                    break;

                default:
                    break;
            }

            int status = urlConnection.getResponseCode();
            System.out.println(status);
            if (status == 200) {
                output = new Data.Builder().putBoolean("resultado", true).build();
                return output;
            } else {
                output = new Data.Builder().putBoolean("resultado", false).build();
                return output;
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Data selectRuta() {
        String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selectRutas.php";
        HttpURLConnection urlConnection = null;

        Data datos = this.getInputData();

        try {
            Data output = null;
            String params = "";

            // Miro a que tabla se realiza la consulta
            String consulta = datos.getString("consulta");

            switch(consulta) {
                case "InfoRuta":
                    params = "?consulta=" + consulta + "&idRuta=" + Integer.toString(datos.getInt("idRuta", 0));
                    break;

                case "MisRutas":
                    params = "?consulta=" + consulta + "&username=" + datos.getString("username");
                    break;

                case "RutasGuardadas":
                    params = "?consulta=" + consulta + "&username=" + datos.getString("username");
                    break;

                case "RutasPublicas":
                    params = "?consulta=" + consulta;
                    break;

                default:
                    break;
            }

            URL urlFinal = new URL(url + params);
            urlConnection = (HttpURLConnection) urlFinal.openConnection();

            int status = urlConnection.getResponseCode();
            System.out.println(status);
            if (status == 200) {
                // La peticion ha tenido exito
                BufferedInputStream input = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                String row = "";
                String resultado = "";

                // Recorro las lineas devueltas por la peticion SQL
                while ((row = reader.readLine()) != null) {
                    resultado += row;
                }
                input.close();

                output = new Data.Builder().putString("resultado", resultado).build();
                return output;
            }
            return output;

        } catch(IOException e) {
            throw new RuntimeException(e);
        }



    }

    public Data select() {
        String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selects.php";
        HttpURLConnection urlConnection = null;

        Data datos = this.getInputData();

        try {
            Data output = null;

            // En este caso todas las consultas usan los mismos parametros (menos Login que requiere contraseña)
            String params = "?consulta=" + datos.getString("consulta") + "&username=" + datos.getString("username");
            if(datos.getString("consulta").equals("Login")) {
                params += "&password=" + datos.getString("password");
            }

            URL urlFinal = new URL(url + params);
            urlConnection = (HttpURLConnection) urlFinal.openConnection();

            int status = urlConnection.getResponseCode();
            System.out.println(status);
            if (status == 200) {
                // La peticion ha tenido exito
                BufferedInputStream input = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                String row = "";
                String resultado = "";

                // Recorro las lineas devueltas por la peticion SQL
                while ((row = reader.readLine()) != null) {
                    resultado += row;
                }
                input.close();
                output = new Data.Builder().putString("resultado", resultado).build();
                return output;
            }
            return output;

        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Data delete() {
        String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/deletes.php";
        HttpURLConnection urlConnection = null;

        Data datos = this.getInputData();

        try {
            Data output = null;
            String params = "";

            // Miro a que tabla se realiza la consulta
            String consulta = datos.getString("consulta");
            switch(consulta) {
                case "Rutas":
                    params = "?consulta=" + consulta + "&idRuta=" + datos.getString("idRuta");
                    break;

                case "Editores":
                    params = "?consulta=" + consulta + "&idRuta=" + datos.getString("idRuta") + "&username=" + datos.getString("username");
                    break;

                case "Amigos":
                    params = "?consulta=" + consulta + "&username1=" + datos.getString("username1") + "&username2=" + datos.getString("username2");
                    break;

                case "Peticiones":
                    params = "?consulta=" + consulta + "&username=" + datos.getString("username");
                    break;

                case "RutasGuardadas":
                    params = "?consulta=" + consulta + "&idRuta=" + datos.getString("idRuta") + "&username=" + datos.getString("username");
                    break;

                case "Tokens":
                    params = "?consulta=" + consulta + "&username=" + datos.getString("username");
                    break;

                default:
                    break;
            }

            URL urlFinal = new URL(url + params);
            urlConnection = (HttpURLConnection) urlFinal.openConnection();

            int status = urlConnection.getResponseCode();
            System.out.println(status);
            if (status == 200) {
                output = new Data.Builder().putBoolean("resultado", true).build();
                return output;
            } else {
                output = new Data.Builder().putBoolean("resultado", false).build();
                return output;
            }

        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
