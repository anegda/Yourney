package com.example.yourney;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

public class LocationService extends Service implements SensorEventListener {
    private final IBinder binder = new LocationServiceBinder();
    private LocationManager locationManager;
    private MyLocationListener locationListener;
    private long comienzo = 0;
    private long fin = 0;
    private SensorManager sensorManager;
    private Sensor contadorPasos;
    private int pasosHist = 0;
    private int pasosAct = 0;
    private double caloriasAct=0.0;
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

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!=null){
            contadorPasos = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
        sensorManager.registerListener(this, contadorPasos, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //PARAR EL SERVICIO EL USUARIO HA CERRADO LA APLICACIÓN
        locationManager.removeUpdates(locationListener);
        locationManager = null;
        locationListener = null;
    }

    //NECESARIOS PARA CONTAR LOS PASOS Y LAS CALORIAS
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d("DAS", String.valueOf(sensorEvent));
        if(sensorEvent.sensor == contadorPasos && comienzo!=0){
            pasosAct = (int) sensorEvent.values[0] - pasosHist;
            caloriasAct = pasosAct/1000.0 *35;
        }else{
            pasosHist = (int) sensorEvent.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
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
        //AQUI RESETEAMOS LOS PARÁMETROS
        locationListener.grabando = false;
        fin = 0;
        comienzo = 0;
        pasosAct = 0;
        pasosHist = 0;
        caloriasAct = 0;
        locationListener = new MyLocationListener();
        sensorManager.unregisterListener(this, contadorPasos);
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

    //DEVUELVE EL ARRAY DE RUTAS DEL LISTENER
    protected ArrayList<Location> getLocations(){
        return locationListener.getLocations();
    }

    //DEVUELVE EL NÚMERO DE PASOS
    protected int getPasosAct(){return pasosAct;}

    //DEVUELVE EL NÚMERO DE CALORIAS
    protected double getCaloriasAct(){return caloriasAct;}

    //CLASE BINDER QUE GUARDA LOS ATRIBUTOS y MÉTODOS QUE NOS PROPORCIONA EL SERVICIO
    public class LocationServiceBinder extends Binder {
        public float getDistancia() {return LocationService.this.getDistancia();}
        public double getDuracion() {return LocationService.this.getDuracion();}
        public boolean grabando() {return LocationService.this.grabando();}
        public void grabarRuta() {LocationService.this.grabarRuta();}
        public void guardarRuta() {LocationService.this.guardarRuta();}
        public void gpsDisponible() {LocationService.this.gpsDisponible();}
        public ArrayList<Location> getLocations(){return LocationService.this.getLocations();}
        public int getPasosAct(){return LocationService.this.getPasosAct();}
        public double getCaloriasAct(){return LocationService.this.getCaloriasAct();}
    }
}