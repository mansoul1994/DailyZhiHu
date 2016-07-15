package com.mansoul.zhihu.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.mansoul.zhihu.global.MyApplication;
import com.mansoul.zhihu.global.NewsApi.Api;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

/**
 * Created by Mansoul on 16/6/5.
 */
public class HttpUtils {

    public static JSONObject jsonObject;

    //判断网络是否可用
    public static boolean isNetworkAvailable(Context context) {
        if (context != null) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if (networkInfo != null) {
                return networkInfo.isAvailable();
            }
        }

        return false;
    }

    public static void getRequest(String url, String tag, Response.Listener listener, Response.ErrorListener errorListener) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Api.BASEURL + url, listener, errorListener);
        jsonObjectRequest.setTag(tag);
        MyApplication.getRequestQueue().add(jsonObjectRequest);
    }

    //加载图片
    public static void setImage(String url, ImageView imageView) {
        //显示图片
        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        imageLoader.displayImage(url, imageView, options);
    }

    public static void setImage(Context context, String url, ImageView imageView) {
        //显示图片
        Glide
                .with(context)
                .load(url)
                .centerCrop()
                .into(imageView);
    }


}
