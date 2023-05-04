package com.example.yourney;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Rutas(" +
                "idRuta INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "nombre varchar(256) NOT NULL DEFAULT 'Ruta Sin Nombre'," +
                "descripcion varchar(256)," +
                "fotoDesc LONGBLOB," +
                "duracion BIGINT NOT NULL," +
                "distancia REAL NOT NULL," +
                "fecha DATETIME NOT NULL," +
                "visibilidad BOOLEAN NOT NULL DEFAULT 1," +
                "creador varchar(255));"
        );

        db.execSQL("CREATE TABLE Ubicaciones(" +
                " idUbi INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                " idRuta INTEGER NOT NULL," +
                " altitud REAL NOT NULL," +
                " longitud REAL NOT NULL," +
                " latitud REAL NOT NULL," +
                " CONSTRAINT fk1 FOREIGN KEY (idRuta) REFERENCES Rutas(idRuta) ON DELETE CASCADE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
