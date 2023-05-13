package com.example.yourney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Base64;

public class GrabarRuta extends AppCompatActivity implements SensorEventListener {
    private LocationService.LocationServiceBinder locationService;

    private Sensor contadorPasos;
    private SensorManager sensorManager;
    private int pasosHist = 0;
    private boolean iniciar = false;
    static String fotoDesc;

    private Handler h = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grabar_ruta);

        //DE PRIMERAS HABILITAMOS EL BOTÓN DE EMPEZAR Y DESHABILITAMOS EL DE PARAR
        Button parar_btn = (Button) findViewById(R.id.btnParar);
        Button empezar_btn = (Button) findViewById(R.id.btnEmpezar);
        empezar_btn.setEnabled(true);
        parar_btn.setEnabled(false);

        //AÑADIMOS LOS LISTENERS A CADA BOTÓN
        empezar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationService.grabarRuta();
                empezar_btn.setEnabled(false);
                parar_btn.setEnabled(true);
                iniciar = true;
            }
        });

        parar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView pasosNum = (TextView) findViewById(R.id.pasosNum);
                String pasos = pasosNum.getText().toString();
                String creador = "anegda";
                LocalDate currentdate = LocalDate.now();
                String fecha = Integer.toString(currentdate.getYear()) + "-" + Integer.toString(currentdate.getMonthValue()) + "-" + Integer.toString(currentdate.getDayOfMonth());


                //INTRODUCIMOS LA RUTA EN LA BD REMOTA
                Data datos = new Data.Builder()
                        .putString("accion", "insert")
                        .putString("consulta", "Rutas")
                        .putString("nombre", "Unnamed")
                        .putString("descripcion", "-")
                        .putDouble("duracion", locationService.getDuracion())
                        .putFloat("distancia", locationService.getDistancia())
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

                WorkManager.getInstance(GrabarRuta.this).getWorkInfoByIdLiveData(insertRuta.getId()).observe(GrabarRuta.this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        // Gestiono la respuesta de la peticion
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            //INTRODUCIMOS LAS UBICACIONES DE LA RUTA EN LA BD REMOTA
                            for (Location location : locationService.getLocations()){
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

                                WorkManager.getInstance(GrabarRuta.this).getWorkInfoByIdLiveData(insertUbi.getId()).observe((LifecycleOwner) new myLifeCycleOwner().getLifecycle(), new Observer<WorkInfo>() {
                                    @Override
                                    public void onChanged(WorkInfo workInfo) {
                                        Log.d("DAS", "ubicación insertada");
                                    }
                                });

                                WorkManager.getInstance(GrabarRuta.this).enqueue(insertUbi);
                            }
                        }
                    }
                });
                WorkManager.getInstance(GrabarRuta.this).enqueue(insertRuta);

                locationService.guardarRuta();
                empezar_btn.setEnabled(true);
                parar_btn.setEnabled(false);

                int idRuta = 1;
                startActivity(new Intent(GrabarRuta.this, DatosRuta.class).putExtra("idRuta", idRuta));
                finish();
            }
        });

        //PEDIR LOS PERMISOS DE LOCALIZACIÓN y ACTIVIDAD FÍSICA
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION}, 11);
        }

        //EMPEZAMOS EL SERVICIO DE LOCALIZACIÓN PARA QUE SE MANTENGA FUERA DE ESTA ACTIVIDAD
        startService(new Intent(this, LocationService.class));

        //CREAMOS LA CONEXIÓN AL SERVICIO
        ServiceConnection conexion = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                locationService = (LocationService.LocationServiceBinder) iBinder;

                //SI YA ESTÁ GRABANDO DESHABILITAMOS EL BOTON DE EMPEZAR Y HABILITAMOS EL DE PARAR
                if(locationService.grabando()){
                    Button empezar_btn = (Button) findViewById(R.id.btnParar);
                    Button parar_btn = (Button) findViewById(R.id.btnEmpezar);
                    empezar_btn.setEnabled(false);
                    parar_btn.setEnabled(true);
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (locationService != null) {
                            //OBTENEMOS DISTANCIA, DURACIÓN Y VELOCIDAD
                            float duracion = (float) locationService.getDuracion();
                            float distanciaKM = locationService.getDistancia();

                            long duracionSec = (long) duracion;
                            long hours = duracionSec / 3600;
                            long minutes = (duracionSec % 3600) / 60;
                            long seconds = duracionSec % 60;

                            float velocidad = 0;
                            if(duracion != 0) {
                                velocidad = distanciaKM / (duracion / 3600);
                            }

                            //APLICAMOS EL FORMATO
                            final String duracionFormato = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                            final String distanciaFormato = String.format("%.2f KM", distanciaKM);
                            final String velocidadFormato = String.format("%.2f KM/H", velocidad);

                            //ACTUALIZAMOS LOS TEXTVIEWS TRAS MEDIO SEGUNDO
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    TextView duracionNum = findViewById(R.id.duracionNum);
                                    TextView distanciaNum = findViewById(R.id.distanciaNum);
                                    TextView velocidadNum = findViewById(R.id.velocidadNum);
                                    TextView pasosNum = findViewById(R.id.pasosNum);
                                    duracionNum.setText(duracionFormato);
                                    distanciaNum.setText(distanciaFormato);
                                    velocidadNum.setText(velocidadFormato);
                                }
                            });

                            try {
                                Thread.sleep(500);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
            @Override
            public void onServiceDisconnected(ComponentName componentName) {locationService=null;}
        };
        bindService(new Intent(this, LocationService.class), conexion, Context.BIND_AUTO_CREATE);

        //OBTENEMOS LOS PASOS
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!=null){
            contadorPasos = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1) {
            Button parar_btn = (Button) findViewById(R.id.btnParar);
            Button empezar_btn = (Button) findViewById(R.id.btnEmpezar);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //PERMISOS ACEPTADOS
                empezar_btn.setEnabled(true);
                parar_btn.setEnabled(false);
                if(locationService!=null){
                    locationService.gpsDisponible();
                }
            } else {
                //PERMISOS DENEGADOS
                empezar_btn.setEnabled(false);
                parar_btn.setEnabled(false);
            }
            return;
        }
    }

    //NECESARIOS PARA CONTAR LOS PASOS Y LAS CALORIAS
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor == contadorPasos && iniciar){
            int pasos = (int) sensorEvent.values[0] - pasosHist;
            TextView pasosNum = (TextView) findViewById(R.id.pasosNum);
            pasosNum.setText(String.valueOf(pasos));

            int calorias = Math.round(pasos/1000 * 35);
            TextView caloriasNum = (TextView) findViewById(R.id.caloriasNum);
            caloriasNum.setText(String.valueOf(calorias));
        }else{
            pasosHist = (int) sensorEvent.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)!=null){
            sensorManager.registerListener(this, contadorPasos, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
}