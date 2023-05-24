package com.example.yourney;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ElAdaptadorRecyclerEditor extends RecyclerView.Adapter<ElAdaptadorRecyclerEditor.RecyclerHolder>{
    private List<ItemListEditor> items;
    private List<ItemListEditor> originalItems;
    private ElAdaptadorRecyclerEditor.RecyclerItemClick itemClick;
    private String user = "";

    /** El adaptador es el controlador del ViewHolder (RecyclerView) **/
    public ElAdaptadorRecyclerEditor(List<ItemListEditor> items, ElAdaptadorRecyclerEditor.RecyclerItemClick itemClick) {
        this.items = items;
        this.itemClick = itemClick;
        this.originalItems = new ArrayList<>();
        originalItems.addAll(items);
    }

    @Override
    /** Creacion del viewHolder **/
    public RecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout_editor, null);
        return new RecyclerHolder(view);
    }

    @Override
    /** Colocamos los datos y gestionamos la seleccion de un elemento **/
    public void onBindViewHolder(@NonNull RecyclerHolder holder, int position) {
        final ItemListEditor item = items.get(position);

        byte [] encodeByte = Base64.decode(item.getFotoDePerfil(), Base64.DEFAULT);
        InputStream inputStream  = new ByteArrayInputStream(encodeByte);
        Bitmap bitmap  = BitmapFactory.decodeStream(inputStream);

        holder.imgPerfil.setImageBitmap(bitmap);
        holder.tvNombre.setText(item.getNombre());
        holder.tvUsername.setText(item.getUsername());

        holder.checkBoxEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClick.itemClick(item);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClick.itemClick(item);
                holder.checkBoxEditor.setChecked(!holder.checkBoxEditor.isChecked());
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
                List<ItemListEditor> collect = originalItems.stream()
                        .filter(i -> i.getUsername().toLowerCase().contains(strSearch))
                        .collect(Collectors.toList());

                items.addAll(collect);
            }
            else {
                items.clear();
                for (ItemListEditor i : originalItems) {
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
        private CheckBox checkBoxEditor;

        public RecyclerHolder(@NonNull View itemView_1) {
            super(itemView_1);
            imgPerfil = itemView.findViewById(R.id.fotoPerfilEditor);
            tvUsername = itemView.findViewById(R.id.tvUsernameEditor);
            tvNombre = itemView.findViewById(R.id.tvNombreEditor);
            checkBoxEditor = itemView.findViewById(R.id.checkBoxEditor);
        }
    }

    public interface RecyclerItemClick {
        void itemClick(ItemListEditor item);
    }
}
