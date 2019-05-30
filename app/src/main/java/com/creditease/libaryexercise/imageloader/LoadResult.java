package com.creditease.libaryexercise.imageloader;

import android.graphics.Bitmap;
import android.widget.ImageView;

import java.io.Serializable;

public class LoadResult {

    private ImageView imageView;
    private Bitmap bitmap;
    private String url;

    public LoadResult(ImageView imageView, Bitmap bitmap, String url) {
        this.imageView = imageView;
        this.bitmap = bitmap;
        this.url = url;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getUrl() {
        return url;
    }
}
