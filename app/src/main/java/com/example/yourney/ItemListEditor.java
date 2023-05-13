package com.example.yourney;

public class ItemListEditor {
    private String username;
    private String nombre;
    private String fotoDePerfil;
    public Boolean isChecked;

    public ItemListEditor(String username, String nombre, String fotoDePerfil, Boolean isChecked) {
        this.username = username;
        this.nombre = nombre;
        this.fotoDePerfil = fotoDePerfil;
        this.isChecked = isChecked;
    }

    public String getUsername() {
        return username;
    }

    public String getNombre() {
        return nombre;
    }

    public String getFotoDePerfil() {
        return fotoDePerfil;
    }

    public Boolean getIsChecked() {
        return isChecked;
    }
}
