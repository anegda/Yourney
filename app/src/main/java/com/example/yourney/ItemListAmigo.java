package com.example.yourney;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.io.Serializable;

public class ItemListAmigo implements Serializable {
    private String username;
    private String nombre;
    private Bitmap fotoDePerfil;

    public ItemListAmigo(String username, String nombre, Bitmap fotoDePerfil) {
        this.username = username;
        this.nombre = nombre;
        this.fotoDePerfil = fotoDePerfil;
    }

    public String getUsername() {
        return username;
    }

    public String getNombre() {
        return nombre;
    }

    public Bitmap getFotoDePerfil() {
        return fotoDePerfil;
    }
}