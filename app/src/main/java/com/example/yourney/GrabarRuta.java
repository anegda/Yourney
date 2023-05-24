package com.example.yourney;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.DefaultTaskExecutor;
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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.Base64;

import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.yourney.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PolylineOptions;

public class GrabarRuta extends FragmentActivity implements OnMapReadyCallback {
    private LocationService.LocationServiceBinder locationService;
    static String fotoDesc;
    private Runnable runnable;
    private ServiceConnection conexion;
    private Button btn_grabando;
    private Handler handler = new Handler();
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private TextView tv_grabando;
    private TextView tv_duracion_num;
    private TextView tv_esperando;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grabar_ruta);

        locationService=null;

        //DE PRIMERAS HABILITAMOS EL BOTÓN DE EMPEZAR Y DESHABILITAMOS EL DE PARAR
        Button parar_btn = (Button) findViewById(R.id.btnParar);
        Button empezar_btn = (Button) findViewById(R.id.btnEmpezar);
        empezar_btn.setEnabled(true);
        empezar_btn.setBackgroundResource(R.drawable.round_btn_verde);
        parar_btn.setEnabled(false);

        // DESHABILITAR EL TEXTVIEW QUE DICE GRABANDO RUTA Y AÑADIR PARPADEO
        btn_grabando = findViewById(R.id.grabando);
        btn_grabando.setVisibility(View.GONE);
        tv_grabando = findViewById(R.id.tvGrabando);
        tv_grabando.setVisibility(View.GONE);
        tv_duracion_num = findViewById(R.id.duracionNum);
        tv_duracion_num.setVisibility(View.GONE);
        tv_esperando  = findViewById(R.id.tvEsperando);

        //AÑADIMOS LOS LISTENERS A CADA BOTÓN
        empezar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationService.grabarRuta();
                empezar_btn.setEnabled(false);
                empezar_btn.setBackgroundResource(R.drawable.round_btn_gris);
                parar_btn.setEnabled(true);
                btn_grabando.setVisibility(View.VISIBLE);
                parar_btn.setBackgroundResource(R.drawable.round_btn_verde);
                tv_grabando.setVisibility(View.VISIBLE);
                tv_duracion_num.setVisibility(View.VISIBLE);
                tv_esperando.setVisibility(View.GONE);

                btn_grabando = findViewById(R.id.grabando);
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (btn_grabando.getVisibility() == View.VISIBLE) {
                            btn_grabando.setVisibility(View.INVISIBLE);
                        } else {
                            btn_grabando.setVisibility(View.VISIBLE);
                        }
                        handler.postDelayed(this, 1000); // Cambia la duración a tu preferencia
                    }
                };

                handler.postDelayed(runnable, 1000); // Cambia la duración a tu preferencia
            }
        });

        parar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView pasosNum = (TextView) findViewById(R.id.pasosNum);
                String pasos = pasosNum.getText().toString();
                Sesion sesion = new Sesion(GrabarRuta.this);
                String creador = sesion.getUsername();
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
                        .putInt("visibilidad", 0)
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
                            Data output = workInfo.getOutputData();
                            if (output.getBoolean("resultado", false)) {

                                // Obtengo el id de la ruta que acabamos de insertar
                                Data datos = new Data.Builder()
                                        .putString("accion", "selectRuta")
                                        .putString("consulta", "UltimaRuta")
                                        .putString("username", sesion.getUsername())
                                        .build();

                                OneTimeWorkRequest selectUltimaRuta = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                        .setInputData(datos)
                                        .build();

                                System.out.println("GrabarRuta");

                                WorkManager.getInstance(GrabarRuta.this).getWorkInfoByIdLiveData(selectUltimaRuta.getId()).observe(GrabarRuta.this, new Observer<WorkInfo>() {
                                    @Override
                                    public void onChanged(WorkInfo workInfo) {
                                        if (workInfo != null && workInfo.getState().isFinished()) {
                                            Data output = workInfo.getOutputData();
                                            if (!output.getString("resultado").equals("Sin resultado")) {

                                                JSONParser parser = new JSONParser();
                                                try {
                                                    JSONArray jsonResultado = (JSONArray) parser.parse(output.getString("resultado"));

                                                    JSONObject row = (JSONObject) jsonResultado.get(0);
                                                    String idRuta = (String) row.get("MAX(IdRuta)");

                                                    //INTRODUCIMOS A NUESTRO USUARIO COMO EDITOR
                                                    Data datosEditor = new Data.Builder()
                                                            .putString("accion", "insert")
                                                            .putString("consulta", "Editores")
                                                            .putInt("idRuta", Integer.parseInt(idRuta))
                                                            .putString("username", creador)
                                                            .build();
                                                    OneTimeWorkRequest insertEditor = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                                            .setInputData(datosEditor)
                                                            .build();
                                                    WorkManager.getInstance(GrabarRuta.this).getWorkInfoByIdLiveData(insertEditor.getId()).observe(GrabarRuta.this, new Observer<WorkInfo>() {
                                                        @Override
                                                        public void onChanged(WorkInfo workInfo) {
                                                            Log.d("DAS", "editor insertado");
                                                        }
                                                    });
                                                    WorkManager.getInstance(GrabarRuta.this).enqueue(insertEditor);

                                                    //INTRODUCIMOS LAS UBICACIONES DE LA RUTA EN LA BD REMOTA
                                                    Log.d("DAS", String.valueOf(locationService.getLocations()));
                                                    for (Location location : locationService.getLocations()) {
                                                        Data datosUbi = new Data.Builder()
                                                                .putString("accion", "insert")
                                                                .putString("consulta", "Ubicaciones")
                                                                .putInt("idRuta", Integer.parseInt(idRuta))
                                                                .putDouble("altitud", location.getAltitude())
                                                                .putDouble("latitud", location.getLatitude())
                                                                .putDouble("longitud", location.getLongitude())
                                                                .build();

                                                        OneTimeWorkRequest insertUbi = new OneTimeWorkRequest.Builder(ConexionBD.class)
                                                                .setInputData(datosUbi)
                                                                .build();

                                                        WorkManager.getInstance(GrabarRuta.this).getWorkInfoByIdLiveData(insertUbi.getId()).observe(GrabarRuta.this, new Observer<WorkInfo>() {
                                                            @Override
                                                            public void onChanged(WorkInfo workInfo) {
                                                                Log.d("DAS", "ubicación insertada");
                                                            }
                                                        });

                                                        WorkManager.getInstance(GrabarRuta.this).enqueue(insertUbi);
                                                    }
                                                    locationService.guardarRuta();
                                                    unbindService(conexion);
                                                    Intent intent = new Intent(GrabarRuta.this, EditarRuta.class);
                                                    intent.putExtra("idRuta", Integer.parseInt(idRuta));
                                                    intent.putExtra("parent", "GrabarRuta");
                                                    startActivity(intent);
                                                    finish();

                                                } catch (ParseException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            }
                                        }
                                    }
                                });
                                WorkManager.getInstance(GrabarRuta.this).enqueue(selectUltimaRuta);
                            }
                        }
                    }
                });
                WorkManager.getInstance(GrabarRuta.this).enqueue(insertRuta);

                empezar_btn.setEnabled(true);
                parar_btn.setEnabled(false);
            }
        });

        //PEDIR LOS PERMISOS DE LOCALIZACIÓN y ACTIVIDAD FÍSICA
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION}, 11);
        }

        //EMPEZAMOS EL SERVICIO DE LOCALIZACIÓN PARA QUE SE MANTENGA FUERA DE ESTA ACTIVIDAD
        startService(new Intent(this, LocationService.class));

        //CREAMOS LA CONEXIÓN AL SERVICIO
        conexion = new ServiceConnection() {
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

                            int pasosAct = locationService.getPasosAct();
                            double caloriasAct = locationService.getCaloriasAct();

                            //APLICAMOS EL FORMATO
                            final String duracionFormato = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                            final String distanciaFormato = String.format("%.2f KM", distanciaKM);
                            final String velocidadFormato = String.format("%.2f KM/H", velocidad);
                            final String pasosFormato = Integer.toString(pasosAct);
                            DecimalFormat decimalFormat = new DecimalFormat("0.00");
                            String caloriasFormato = decimalFormat.format(caloriasAct);

                            //ACTUALIZAMOS LOS TEXTVIEWS TRAS MEDIO SEGUNDO
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    TextView duracionNum = findViewById(R.id.duracionNum);
                                    TextView distanciaNum = findViewById(R.id.distanciaNum);
                                    TextView velocidadNum = findViewById(R.id.velocidadNum);
                                    TextView pasosNum = findViewById(R.id.pasosNum);
                                    TextView caloriasNum = findViewById(R.id.caloriasNum);
                                    duracionNum.setText(duracionFormato);
                                    distanciaNum.setText(distanciaFormato);
                                    velocidadNum.setText(velocidadFormato);
                                    pasosNum.setText(pasosFormato);
                                    caloriasNum.setText(caloriasFormato);

                                    //OBTENEMOS EL MAPA Y CREAMOS LA RUTA
                                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                            .findFragmentById(R.id.map);
                                    if(mapFragment!=null) {
                                        mapFragment.getMapAsync(GrabarRuta.this);
                                    }
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

        //OBTENEMOS EL MAPA Y CREAMOS LA RUTA
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        PolylineOptions line = new PolylineOptions().clickable(false);
        LatLng firstLoc = null;
        if (locationService!=null) {
            for (Location location : locationService.getLocations()) {
                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                line.add(loc);
                if (firstLoc == null) {
                    firstLoc = loc;
                }
            }

            float zoom = 15.0f;
            if (firstLoc != null) {
                mMap.addMarker(new MarkerOptions().position(firstLoc).title(getString(R.string.comienzo)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstLoc, zoom));
            }
            mMap.addPolyline(line);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(R.drawable.logocolor).setTitle(getString(R.string.salir_grabar)).setMessage(getString(R.string.salir_grabar2))
                .setPositiveButton(getString(R.string.si), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        locationService.guardarRuta();
                        unbindService(conexion);
                        startActivity(new Intent(GrabarRuta.this, MainActivity.class));
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }
}