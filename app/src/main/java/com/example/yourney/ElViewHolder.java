package com.example.yourney;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ElViewHolder extends RecyclerView.ViewHolder {
    public TextView nombre;
    public TextView descripcion;
    public ImageView laimagen;
    String user;

    /**
     * Para gestionar la interacción del usuario.
     * Hay que poder acceder a ese control de la selección desde
     * la clase que extiende ViewHolder
     **/
    public boolean[] seleccion;

    public ElViewHolder(@NonNull View itemView, String User) {
        super(itemView);
        nombre = itemView.findViewById(R.id.tvNombreRuta);
        descripcion = itemView.findViewById(R.id.tvDescrRuta);
        laimagen = itemView.findViewById(R.id.imagenRuta);
        user = User;

        /**listener del itemView para interraccionar con las imagenes al ser clickadas**/
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }

}

