package com.example.yourney;

import java.io.Serializable;

public class ItemListRuta implements Serializable {
    private String id, titulo, descripcion, imgResource;

    public ItemListRuta(String id, String titulo, String descripcion, String imgResource) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.imgResource = imgResource;
    }

    public String getId() {
        return id;
    }
    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getImgResource() {
        return imgResource;
    }
}