package com.example.yourney;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Base64;

public class LocationService extends Service {
    private final IBinder binder = new LocationServiceBinder();

    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private long comienzo = 0;
    private long fin = 0;
    static String fotoDesc;

    @Override
    public IBinder onBind(Intent intent) {return binder;}

    @SuppressLint("ServiceCast")
    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();
        try {
            //OBTENER ACTUALIZACIONES CADA MEDIO SEGUNDO O CADA 10 METROS
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 500, 10, locationListener);
        } catch(SecurityException e) {
            Log.d("DAS", "Problemas con la localización");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //PARAR EL SERVICIO EL USUARIO HA CERRADO LA APLICACIÓN
        locationManager.removeUpdates(locationListener);
        locationManager = null;
        locationListener = null;
    }

    //EMPEZAR A GRABAR RUTA
    protected void grabarRuta(){
        locationListener.grabando = true;
        comienzo = SystemClock.elapsedRealtime();
    }

    //OBTENER DISTANCIA DESDE EL LISTENER
    protected float getDistancia(){return locationListener.getDistanciaTotal();};

    //OBTENER DURACIÓN EN SEGUNDOS DE LA RUTA
    protected double getDuracion(){
        double duracion = 0.0;
        if(comienzo != 0){
            long act = SystemClock.elapsedRealtime();
            if(fin !=0){
                act = fin;
            }
             return (act - comienzo) / 1000;
        }
        return duracion;
    }

    //GUARDAR RUTA y RESETEAR LOS PARAMETROS
    protected int guardarRuta(String pasos, String creador) {
        //OBTENEMOS LAS FECHA
        LocalDate currentdate = LocalDate.now();
        String fecha = Integer.toString(currentdate.getYear()) + "-" + Integer.toString(currentdate.getMonthValue()) + "-" + Integer.toString(currentdate.getDayOfMonth());

        //AQUÍ SE HACE LA LLAMADA A LA BD LOCAL
        DBHelper GestorBD = new DBHelper(getApplicationContext(), "Yourney", null, 1);
        SQLiteDatabase bd = GestorBD.getWritableDatabase();
        bd.execSQL("INSERT INTO Rutas ('nombre', 'descripcion', 'fotoDesc', 'duracion', 'distancia', 'pasos', 'dificultad', 'fecha', 'visibilidad', 'creador') VALUES ('unnamed', '', '',"+getDuracion()+","+getDistancia()+","+pasos+", 'facil', '"+fecha+"', 1,'"+creador+"')");
        Cursor c = bd.rawQuery("SELECT max(idRuta) FROM Rutas", null);
        c.moveToFirst();
        int idRuta = c.getInt(0);
        c.close();

        for (Location location : locationListener.getLocations()){
            bd.execSQL("INSERT INTO Ubicaciones ('idRuta', 'altitud', 'longitud', 'latitud') VALUES ("+idRuta+","+location.getAltitude()+","+location.getLongitude()+","+location.getLatitude()+")");
        }
        bd.close();

        //INTRODUCIMOS LA RUTA EN LA BD REMOTA
        Data datos = new Data.Builder()
                .putString("accion", "insert")
                .putString("consulta", "Rutas")
                .putString("nombre", "Unnamed")
                .putString("descripcion", "-")
                .putDouble("duracion", getDuracion())
                .putFloat("distancia", getDistancia())
                .putString("pasos", pasos)
                .putString("dificultad", "facil")
                .putString("fecha", fecha)
                .putInt("visibilidad", 1)
                .putString("creador", creador)
                .build();

        //POR AHORA ESTABLECEMOS COMO FOTO DE RUTA LA FOTO POR DEFECTO
        Bitmap img = ((BitmapDrawable) getResources().getDrawable(R.drawable.fondobosque)).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        fotoDesc = Base64.getEncoder().encodeToString(b);

        OneTimeWorkRequest insertRuta = new OneTimeWorkRequest.Builder(ConexionBD.class)
                .setInputData(datos)
                .build();

        WorkManager.getInstance(LocationService.this).getWorkInfoByIdLiveData(insertRuta.getId()).observe((LifecycleOwner) new myLifeCycleOwner().getLifecycle(), new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                // Gestiono la respuesta de la peticion
                if (workInfo != null && workInfo.getState().isFinished()) {
                    //INTRODUCIMOS LAS UBICACIONES DE LA RUTA EN LA BD REMOTA
                    for (Location location : locationListener.getLocations()){
                        Data datosUbi = new Data.Builder()
                                .putString("accion", "insert")
                                .putString("consulta", "Ubicaciones")
                                .putInt("idRuta", 1)
                                .putDouble("altitud", location.getAltitude())
                                .putDouble("latitud", location.getLatitude())
                                .putDouble("longitud", location.getLongitude())
                                .build();

                        OneTimeWorkRequest insertUbi = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                .setInputData(datosUbi)
                                .build();

                        WorkManager.getInstance(LocationService.this).getWorkInfoByIdLiveData(insertUbi.getId()).observe((LifecycleOwner) new myLifeCycleOwner().getLifecycle(), new Observer<WorkInfo>() {
                            @Override
                            public void onChanged(WorkInfo workInfo) {
                                Log.d("DAS", "ubicación insertada");
                            }
                        });

                        WorkManager.getInstance(LocationService.this).enqueue(insertUbi);
                    }
                }
            }
        });
        WorkManager.getInstance(LocationService.this).enqueue(insertRuta);

        //AQUI RESETEAMOS LOS PARÁMETROS
        locationListener.grabando = false;
        fin = SystemClock.elapsedRealtime();
        comienzo = 0;
        locationListener = new MyLocationListener();

        return idRuta;
    }

    //DEVUELVE UN BOOLEANO INDICANDO SI SE ESTA GRABANDO UNA RUTA
    protected boolean grabando() {return comienzo != 0;}

    //EL GPS ESTÁ ACTIVADO, EMPEZAR A OBTENER UBICACIONES
    protected void gpsDisponible() {
        try {
            locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 500, 10, locationListener);
        } catch(SecurityException e) {
            Log.d("DAS", "Problemas con la localización");;
        }
    }

    //CLASE BINDER QUE GUARDA LOS ATRIBUTOS y MÉTODOS QUE NOS PROPORCIONA EL SERVICIO
    public class LocationServiceBinder extends Binder {
        public float getDistancia() {return LocationService.this.getDistancia();}
        public double getDuracion() {return LocationService.this.getDuracion();}
        public boolean grabando() {return LocationService.this.grabando();}
        public void grabarRuta() {LocationService.this.grabarRuta();}
        public int guardarRuta(String pasos, String creador) {
            int idRuta = LocationService.this.guardarRuta(pasos, creador);
            return idRuta;
        }
        public void gpsDisponible() {LocationService.this.gpsDisponible();}
    }
}