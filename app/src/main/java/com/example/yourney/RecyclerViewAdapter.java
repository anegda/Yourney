package com.example.yourney;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Base64;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.RecyclerViewHolder> {
    private final Context context;
    private final ArrayList<Integer> idImgList;
    private final ArrayList<String> imageBlobArrayList;
    private final ArrayList<String> listaOriginal;

    public static String fotoElegidaBlob;
    public static Integer idImgElegida;

    public RecyclerViewAdapter(Context context, ArrayList<Integer> idImgList, ArrayList<String> imagePathArrayList) {
        this.context = context;
        this.imageBlobArrayList = imagePathArrayList;
        this.idImgList = idImgList;
        listaOriginal = new ArrayList<>();
        listaOriginal.addAll(imagePathArrayList);
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.imagen_galeria, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String imgBlob = imageBlobArrayList.get(position);
        if (imgBlob != null) {
            byte[] encodeByte = Base64.getDecoder().decode(imgBlob);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            holder.imageIV.setImageBitmap(bitmap);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ImagenGaleria.class);
                    i.putExtra("imgBlob", "nananananana");
                    fotoElegidaBlob = imageBlobArrayList.get(position);
                    idImgElegida = idImgList.get(position);
                    context.startActivity(i);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return imageBlobArrayList.size();
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageIV;
        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIV = itemView.findViewById(R.id.imgIV);
        }
    }
}
