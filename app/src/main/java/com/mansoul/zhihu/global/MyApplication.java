package com.mansoul.zhihu.global;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.mansoul.zhihu.cache.LruImageCache;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

/**
 * Created by Mansoul on 16/6/5.
 */
public class MyApplication extends Application {

    private static RequestQueue mQueue;
    private static Context mContext;
    private static com.android.volley.toolbox.ImageLoader mImageLoader;

    @Override
    public void onCreate() {
        super.onCreate();

        initImageLoader(getApplicationContext());

        mContext = getApplicationContext();

    }

    private void initImageLoader(Context context) {
        File cacheDir = StorageUtils.getCacheDirectory(context);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(3)
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCache(new UnlimitedDiskCache(cacheDir))
//                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);
    }

    public static Context getContext() {
        return mContext;
    }

    public static RequestQueue getRequestQueue() {
        if (mQueue == null) {
            mQueue = Volley.newRequestQueue(getContext());

        }
        return mQueue;
    }

    public static com.android.volley.toolbox.ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            mImageLoader = new com.android.volley.toolbox.ImageLoader(getRequestQueue(), LruImageCache.getInstance());
        }
        return mImageLoader;
    }

}
