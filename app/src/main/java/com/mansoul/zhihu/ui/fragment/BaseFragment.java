package com.mansoul.zhihu.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mansoul.zhihu.engine.HttpCacheManager;
import com.mansoul.zhihu.global.MyApplication;
import com.mansoul.zhihu.utils.HttpUtils;
import com.mansoul.zhihu.utils.MD5Encoder;
import com.mansoul.zhihu.utils.StringUtils;

import java.io.File;

/**
 * fragment基类
 * Created by Mansoul on 16/6/6.
 */
public abstract class BaseFragment extends Fragment {

    public Activity mActivity;
    private String tag;

    //fragment创建
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    //初始化fragment布局
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return initView();
    }

    //fragment依赖的activity的onCreate方法执行结束
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //初始化数据

        String url = getUrl();
        if (url != null) {
            if (HttpUtils.isNetworkAvailable(mActivity)) {
                getDataFormServer(url);
            } else {
                getCache(url);
            }
        }
        initData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity = null;
    }

    private boolean isLoading = false;

    public void getDataFormServer(final String url) {
        if (!isLoading) {
            isLoading = true;
            if (getSwipeRefreshLayout() != null) {
                getSwipeRefreshLayout().setRefreshing(true);
            }
//            setStateTrue();

            StringRequest request = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println(response);

                            //写缓存
                            File cacheFile = HttpCacheManager.getCacheFile(url); //缓存文件
                            if (cacheFile == null) {
                                HttpCacheManager.setCache(mActivity, url, response);
                            } else {
                                String cache = HttpCacheManager.getCache(url);
                                if (cache != null && !cache.equals(response)) {
                                    cacheFile.delete();
                                    HttpCacheManager.setCache(mActivity, url, response);
                                }
                            }

                            //解析json数据
                            parseData(response);

//                            setStateFalse();
                            if (getSwipeRefreshLayout() != null) {
                                getSwipeRefreshLayout().setRefreshing(false);
                            }
                            isLoading = false;
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });
            tag = getRequestTag(url);
            request.setTag(tag);
            MyApplication.getRequestQueue().add(request);
        }
    }

    public abstract void parseData(String response);

//    public abstract void setStateFalse();
//
//    public abstract void setStateTrue();

    public abstract SwipeRefreshLayout getSwipeRefreshLayout();

    public void getCache(String url) {
        if (getSwipeRefreshLayout() != null) {

            getSwipeRefreshLayout().setRefreshing(false);
        }
//        setStateFalse();
//        System.out.println("加载缓存了！！！");
        String cache = HttpCacheManager.getCache(url);

        if (!StringUtils.isEmpty(cache)) {
            parseData(cache);
        }
    }

    public String getRequestTag(String url) {
        try {
            return MD5Encoder.encode(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public abstract String getUrl();

    public abstract View initView();

    public abstract void initData();

    @Override
    public void onStop() {
        super.onStop();
        if (tag != null) {
            MyApplication.getRequestQueue().cancelAll(tag);
        }
    }
}
