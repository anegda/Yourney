package com.example.yourney;

import android.location.Location;
import android.location.LocationListener;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class MyLocationListener implements LocationListener {
    ArrayList<Location> locations;
    boolean grabando;

    //CREAMOS LA CONSTRUCTORA
    public MyLocationListener(){
        locations = new ArrayList<Location>();
        grabando = false;
    }

    //PARA OBTENER LA LISTA DE LOCALIZACIONES
    public ArrayList<Location> getLocations(){return locations;}

    //PARA OBTENER LA DISTANCIA RECORRIDA DESDE PRINCIPIO A FIN (en kilometros)
    public float getDistanciaTotal(){
        if(locations.size()==0){
            return 0;
        } else{
            return locations.get(0).distanceTo(locations.get(locations.size()-1)) / 1000;
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        //Añadir la nueva localización a la lista si en ese momento se esta grabando la ruta
        if(grabando){
            locations.add(location);
        }
    }
}
