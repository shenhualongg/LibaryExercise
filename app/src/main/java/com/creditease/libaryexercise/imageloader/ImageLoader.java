package com.creditease.libaryexercise.imageloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ImageLoader {

    public static final int MEASSGE_RESULT = 100;

    private static ImageLoader instance;
    private static int IO_BUFFER_SIZE = 8 * 1024; // 8k
    private static LruCache<String, Bitmap> memoryCache;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_COUNT = CPU_COUNT + 1;
    private static final int MAX_COUNT = CPU_COUNT * 2 + 1;
    private static final int ALIVE_PERIOD = 10;

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            CORE_COUNT,
            MAX_COUNT,
            ALIVE_PERIOD,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>());

    private Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            LoadResult result = (LoadResult) msg.obj;
            ImageView imageView = result.getImageView();
            Bitmap bitmap = result.getBitmap();
            imageView.setImageBitmap(bitmap);
        }
    };

    public static ImageLoader build() {
        if (instance == null) {
            instance = new ImageLoader();
        }
        return instance;
    }

    private ImageLoader() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        memoryCache = new LruCache<String, Bitmap>(maxMemory / 8) {
            @Override
            protected int sizeOf(@NonNull String key, @NonNull Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }

    public Bitmap loadBitmap(String imageUrl) {
        Bitmap bitmap = loadBitmapFromCache(imageUrl);
        if (bitmap != null) {
            return bitmap;
        }
        return downloadBitmapFromUrl(imageUrl);
    }

    public void bindBitmap(final String imageUrl, final ImageView imageView) {
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(imageUrl);
                if (bitmap != null) {
                    Message message = handler.obtainMessage(MEASSGE_RESULT, new LoadResult(imageView, bitmap, imageUrl));
                    message.sendToTarget();
                }
            }
        });
    }

    private Bitmap loadBitmapFromCache(String imageUrl) {
        String key = hashKeyFromUrl(imageUrl);
        return memoryCache.get(key);
    }

    private Bitmap downloadBitmapFromUrl(String imageUrl) {
        Bitmap bitmap = null;
        HttpURLConnection httpURLConnection = null;
        BufferedInputStream bufferedInputStream = null;

        try {
            URL url = new URL(imageUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            bufferedInputStream = new BufferedInputStream(httpURLConnection.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(bufferedInputStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        if (bitmap != null) {
            addToMemoryCache(imageUrl, bitmap);
        }
        return bitmap;
    }

    private void addToMemoryCache(String imageUrl, Bitmap bitmap) {
        memoryCache.put(imageUrl, bitmap);
    }

    private String hashKeyFromUrl(String url) {
        String cacheKey;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(url.getBytes());
            cacheKey = bytesToHexString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
