package com.example.yourney;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

public class LocationService extends Service {
    private final IBinder binder = new LocationServiceBinder();

    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private long comienzo = 0;
    private long fin = 0;

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
    protected void guardarRuta() {
        //AQUÍ SE HACE LA LLAMADA A LA BD
        DBHelper GestorBD = new DBHelper(getApplicationContext(), "Yourney", null, 1);
        SQLiteDatabase bd = GestorBD.getWritableDatabase();
        bd.execSQL("INSERT INTO Rutas ('nombre', 'descripcion', 'fotoDesc', 'duracion', 'distancia', 'fecha', 'visibilidad', 'creador') VALUES ('rutaPrueba', 'prueba', 'dasdsad',"+getDuracion()+","+getDistancia()+",'2023-05-01',1,'anegda')");

        for (Location location : locationListener.getLocations()){
            bd.execSQL("INSERT INTO Ubicaciones ('idRuta', 'altitud', 'longitud', 'latitud') VALUES ('1',"+location.getAltitude()+","+location.getLongitude()+","+location.getLatitude()+")");
        }

        //AQUI RESETEAMOS LOS PARÁMETROS
        locationListener.grabando = false;
        fin = SystemClock.elapsedRealtime();
        comienzo = 0;
        locationListener = new MyLocationListener();
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
        public void guardarRuta() {LocationService.this.guardarRuta();}
        public void gpsDisponible() {LocationService.this.gpsDisponible();}
    }
}