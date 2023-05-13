package com.example.yourney;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ElAdaptadorRecyclerAmigos extends RecyclerView.Adapter<ElAdaptadorRecyclerAmigos.RecyclerHolder> {
    private List<ItemListAmigo> items;
    private List<ItemListAmigo> originalItems;
    private RecyclerItemClick itemClick;
    private String user = "";

    /** El adaptador es el controlador del ViewHolder (RecyclerView) **/
    public ElAdaptadorRecyclerAmigos(List<ItemListAmigo> items, RecyclerItemClick itemClick) {
        this.items = items;
        this.itemClick = itemClick;
        this.originalItems = new ArrayList<>();
        originalItems.addAll(items);
    }

    @Override
    /** Creacion del viewHolder **/
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_amigo, null);
        return new RecyclerHolder(view);
    }

    @Override
    /** Colocamos los datos y gestionamos la seleccion de un elemento **/
    public void onBindViewHolder(@NonNull RecyclerHolder holder, int position) {
        final ItemListAmigo item = items.get(position);
        holder.imgPerfil.setImageResource(item.getFotoDePerfil());
        holder.tvNombre.setText(item.getNombre());
        holder.tvUsername.setText(item.getUsername());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClick.itemClick(item);
            }
        });
    }

    @Override
    /** getter del numero de items en el viewholder **/
    public int getItemCount() {
        return items.size();
    }

    /** Metodo que filtra los elementos de la lista
     * Extraido de: https://github.com/ElvisAlfaro/RecyclerView
     * Modificado y editado por Hugo Robles **/
    public void filter(final String strSearch) {
        if (strSearch.length() == 0) {
            items.clear();
            items.addAll(originalItems);
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                items.clear();
                List<ItemListAmigo> collect = originalItems.stream()
                        .filter(i -> i.getUsername().toLowerCase().contains(strSearch))
                        .collect(Collectors.toList());

                items.addAll(collect);
            }
            else {
                items.clear();
                for (ItemListAmigo i : originalItems) {
                    if (i.getUsername().toLowerCase().contains(strSearch)) {
                        items.add(i);
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    public class RecyclerHolder extends RecyclerView.ViewHolder {
        private ImageView imgPerfil;
        private TextView tvUsername;
        private TextView tvNombre;

        public RecyclerHolder(@NonNull View itemView_1) {
            super(itemView_1);
            imgPerfil = itemView.findViewById(R.id.fotoPerfilAmigo);
            tvUsername = itemView.findViewById(R.id.tvUsernameAmigo);
            tvNombre = itemView.findViewById(R.id.tvNombreAmigo);
        }
    }

    public interface RecyclerItemClick {
        void itemClick(ItemListAmigo item);
    }
}
