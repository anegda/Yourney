package com.example.yourney;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.simple.JSONArray;
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
        Log.d("DAS", String.valueOf(datos));

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
            case "selectUsuario":
                System.out.println("***** SELECT USUARIO *****");
                Data outputSelectUsuario = selectUsuario();
                return Result.success(outputSelectUsuario);
            case "select":
                System.out.println("***** SELECT *****");
                Data outputSelect = select();
                return Result.success(outputSelect);
            case "delete":
                System.out.println("***** DELETE *****");
                Data outputDelete = delete();
                return Result.success(outputDelete);
            case "notifPeticion":
                System.out.println("***** NOTIFICACION AMISTAD *****");
                Data outputPeticion = notifPeticion();
                return Result.success(outputPeticion);
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

            switch (consulta) {
                case "Usuarios":

                    params.put("username", datos.getString("username"));
                    params.put("nombre", datos.getString("nombre"));
                    params.put("apellidos", datos.getString("apellidos"));
                    params.put("password", datos.getString("password"));
                    params.put("email", datos.getString("email"));
                    params.put("fotoPerfil", RegisterActivity2.fotoen64);

                    System.out.println(params.get("username"));
                    System.out.println(params.get("nombre"));
                    System.out.println(params.get("apellidos"));
                    System.out.println(params.get("password"));
                    System.out.println(params.get("email"));
                    System.out.println(RegisterActivity2.fotoen64.length());

                    break;

                case "Rutas":

                    params.put("nombre", datos.getString("nombre"));
                    params.put("descripcion", datos.getString("descripcion"));
                    params.put("fotoDesc", "prueba2");
                    params.put("duracion", datos.getDouble("duracion", 0));
                    params.put("distancia", datos.getFloat("distancia", 0));
                    params.put("pasos", datos.getString("pasos"));
                    params.put("dificultad", datos.getString("dificultad"));
                    params.put("fecha", datos.getString("fecha"));
                    params.put("visibilidad", datos.getInt("visibilidad", 1));
                    params.put("creador", datos.getString("creador"));

                   break;

                case "Ubicaciones":

                    params.put("idRuta", datos.getInt("idRuta", 0));
                    params.put("altitud", datos.getDouble("altitud", 0));
                    params.put("longitud", datos.getDouble("longitud", 999));
                    params.put("latitud", datos.getDouble("latitud", 999));

                    break;

                case "Editores":

                    params.put("idRuta", datos.getInt("idRuta", 0));
                    params.put("username", datos.getString("username"));

                    break;

                case "Amigos":

                    params.put("username1", datos.getString("username1"));
                    params.put("username2", datos.getString("username2"));

                    break;

                case "Peticiones":

                    params.put("username1", datos.getString("username1"));
                    params.put("username2", datos.getString("username2"));
                    params.put("mensaje", datos.getString("mensaje"));
                    params.put("estado", datos.getInt("estado",0));

                    break;

                case "Imagenes":

                    params.put("idRuta", datos.getInt("idRuta", 0));
                    params.put("username", datos.getString("username"));
                    params.put("imgBlob", GaleriaFotosRuta.fotoNueva);

                    break;

                case "RutasGuardadas":

                    params.put("idRuta", datos.getInt("idRuta", 0));
                    params.put("username", datos.getString("username"));

                    break;

                case "Tokens":

                    params.put("username", datos.getString("username"));
                    params.put("token", datos.getString("token"));


                default:
                    break;
            }

            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(params.toString());
            out.close();

            int status = urlConnection.getResponseCode();
            System.out.println(status);
            if (status == 200 || status == 500) {
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

                    params.put("username", datos.getString("username"));
                    params.put("nombreNew", datos.getString("nombreNew"));
                    params.put("apellidosNew", datos.getString("apellidosNew"));
                    params.put("passwordNew", datos.getString("passwordNew"));
                    params.put("emailNew", datos.getString("emailNew"));
                    params.put("fotoPerfil", datos.getString("fotoPerfil"));

                    out.print(params.toString());
                    out.close();

                    break;

                case "Rutas":

                    params.put("idRuta", datos.getInt("idRuta", 0));
                    params.put("nombre", datos.getString("nombre"));
                    params.put("descripcion", datos.getString("descripcion"));
                    params.put("dificultad", datos.getString("dificultad"));
                    params.put("fotoDesc", EditarRuta.fotoDescriptiva);
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
            System.out.println(consulta);

            // Diferentes parametros para cada consulta
            switch(consulta) {
                case "InfoRuta":
                    params = "?consulta=" + consulta + "&idRuta=" + Integer.toString(datos.getInt("idRuta", 0));
                    break;

                case "UbisRuta":
                    params = "?consulta=" + consulta + "&idRuta=" + Integer.toString(datos.getInt("idRuta", 0));
                    break;

                case "UltimaRuta":
                    params = "?consulta=" + consulta + "&username=" + datos.getString("username");
                    break;

                case "RutasGuardadas2":
                    params = "?consulta=" + consulta + "&username=" + datos.getString("username");
                    break;

                default:
                    break;
            }

            URL urlFinal = new URL(url + params);
            urlConnection = (HttpURLConnection) urlFinal.openConnection();

            int status = urlConnection.getResponseCode();
            Log.d("DAS", String.valueOf(status));
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

                Log.d("DAS", resultado);
                if (!resultado.equals("Sin resultado")) {
                    switch(consulta) {
                        case "InfoRuta":
                            JSONParser parser = new JSONParser();
                            JSONArray jsonResultado = (JSONArray) parser.parse(resultado);
                            JSONObject fila = (JSONObject) jsonResultado.get(0);

                            output = new Data.Builder()
                                    .putString("Nombre", (String) fila.get("Nombre"))
                                    .putString("Descripcion", (String) fila.get("Descripcion"))
                                    .putFloat("Duracion", Float.parseFloat((String) fila.get("Duracion")))
                                    .putFloat("Distancia", Float.parseFloat((String) fila.get("Distancia")))
                                    .putInt("Pasos", Integer.parseInt((String) fila.get("Pasos")))
                                    .putString("Dificultad", (String) fila.get("Dificultad"))
                                    .putString("Fecha", (String) fila.get("Fecha"))
                                    .putInt("Visibilidad", (Integer) Integer.parseInt((String) fila.get("Visibilidad")))
                                    .putString("Creador", (String) fila.get("Creador"))
                                    .putString("resultado", "exito")
                                    .build();
                            //VerRuta.fotoDesc = (String) fila.get("FotoDesc");
                            return output;
                        case "UbisRuta":
                        case "UltimaRuta":
                        case "RutasGuardadas2":
                            output = new Data.Builder().putString("resultado", resultado).build();
                            return output;
                        default:
                            break;
                    }
                    return output;
                }else{
                    output = new Data.Builder().putString("resultado", resultado).build();
                    return output;
                }
            }
            output = new Data.Builder().putString("resultado", "Sin resultado").build();
            return output;
        } catch(IOException | ParseException e) {
            throw new RuntimeException(e);
        }

    }

    public Data selectUsuario() {
        String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selectUsuarios.php";
        HttpURLConnection urlConnection = null;

        Data datos = this.getInputData();

        try {
            Data output = null;

            String params = "?consulta=" + datos.getString("consulta") + "&username=" + datos.getString("username");

            // Añado la contraseña en caso de que se este logueando
            if (datos.getString("consulta").equals("Login")) {
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
                System.out.println(resultado);
                input.close();

                if (!resultado.equals("Sin resultado")) {

                    // La consulta ha devuelto la informacion de un usuario

                    if (datos.getString("consulta").equals("Login")) {

                        output = new Data.Builder().putString("resultado", resultado).build();
                        return output;

                    } else if (datos.getString("consulta").equals("Usuarios")) {

                        // Formo el JSON de resultado SIN la imagen
                        JSONParser parser = new JSONParser();
                        JSONObject json = (JSONObject) parser.parse(resultado);

                        output = new Data.Builder()
                                .putString("username", (String) json.get("Username"))
                                .putString("nombre", (String) json.get("Nombre"))
                                .putString("apellidos", (String) json.get("Apellidos"))
                                .putString("password", (String) json.get("Password"))
                                .putString("email", (String) json.get("Email"))
                                .putString("resultado", "exito")
                                .build();

                        // Pongo la imagen donde toca (ajustes del perfil)??

                        return output;
                    }

                } else {

                    // La consulta no ha devuelto ningun usuario

                    output = new Data.Builder().putString("resultado", resultado).build();
                    return output;
                }
            }

            return output;

        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Data select() {
        String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/selects.php";
        HttpURLConnection urlConnection = null;

        Data datos = this.getInputData();

        try {
            Data output = null;
            String consulta = datos.getString("consulta");
            String params = "?consulta=" + consulta + "&username=" + datos.getString("username");
            if(consulta.equals("Editores")){
                params = params + "&idRuta=" + Integer.toString(datos.getInt("idRuta",0));
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
                System.out.println(resultado);
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
                    params = "?consulta=" + consulta + "&idRuta=" + datos.getInt("idRuta", 0) + "&username=" + datos.getString("username");
                    break;

                case "Imagenes":
                    System.out.println("IMAGENES");
                    System.out.println(datos.getInt("idImg", 0));
                    params = "?consulta=" + consulta + "&idImg=" + datos.getInt("idImg", 0);
                    break;

                case "Tokens":
                    params = "?consulta=" + consulta + "&username=" + datos.getString("username") + "&token=" + datos.getString("token");
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

    public Data notifPeticion() {
        String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/mmerino028/WEB/notifPeticion.php";
        HttpURLConnection urlConnection = null;

        Data datos = this.getInputData();

        try {
            Data output = null;

            String params = "?emisor=" + datos.getString("emisor") + "&receptor=" + datos.getString("receptor") + "&mensaje=" + datos.getString("mensaje");

            URL urlFinal = new URL(url + params);
            urlConnection = (HttpURLConnection) urlFinal.openConnection();

            int status = urlConnection.getResponseCode();
            System.out.println(status);
            if (status == 200) {
                // La peticion ha tenido exito
                output = new Data.Builder().putString("resultado", "exito").build();
                return output;
            } else {
                // La peticion no ha tenido exito
                output = new Data.Builder().putString("resultado", "sin exito").build();
                return output;
            }

        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
