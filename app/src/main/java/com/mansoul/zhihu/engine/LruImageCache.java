package com.mansoul.zhihu.engine;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * Created by Mansoul on 16/6/5.
 */
public class LruImageCache implements ImageLoader.ImageCache {

    private LruImageCache() {
    }

    private static LruImageCache instance;

    public static LruImageCache getInstance() {
        if (instance == null) {
            instance = new LruImageCache();
        }
        return instance;
    }

    private int maxSize = (int) (Runtime.getRuntime().maxMemory() / 8);

    private LruCache<String, Bitmap> cacheMap = new LruCache<String, Bitmap>(maxSize) {
        //每张图片的大小
        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes() * value.getHeight();
        }
    };

    @Override
    public Bitmap getBitmap(String url) {

        Bitmap bitmap = cacheMap.get(url);

        return bitmap;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        cacheMap.put(url, bitmap);
    }
}
