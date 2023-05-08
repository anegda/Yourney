package com.example.yourney;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import java.util.Base64;

public class ImagenGaleria extends AppCompatActivity {
    private ScaleGestureDetector scaleGestureDetector;
    private float mScaleFactor = 1.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagen_galeria);

        String imgBlob = RecyclerViewAdapter.fotoElegidaBlob;
        ImageView imgView = findViewById(R.id.imgIV);

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        byte[] encodeByte = Base64.getDecoder().decode(imgBlob);
        Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        imgView.setImageBitmap(bitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        scaleGestureDetector.onTouchEvent(motionEvent);
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            ImageView imgView = findViewById(R.id.imgIV);
            imgView.setScaleX(mScaleFactor);
            imgView.setScaleY(mScaleFactor);
            return true;
        }
    }
}