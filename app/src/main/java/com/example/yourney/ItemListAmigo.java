package com.example.yourney;

import java.io.Serializable;

public class ItemListAmigo implements Serializable {
    private String username;
    private String nombre;
    private int fotoDePerfil;

    public ItemListAmigo(String username, String nombre, int fotoDePerfil) {
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

    public int getFotoDePerfil() {
        return fotoDePerfil;
    }
}