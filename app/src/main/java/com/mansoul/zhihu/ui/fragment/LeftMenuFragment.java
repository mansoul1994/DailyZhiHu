package com.mansoul.zhihu.ui.fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.mansoul.zhihu.R;
import com.mansoul.zhihu.cache.HttpCacheManager;
import com.mansoul.zhihu.domain.NewsItem;
import com.mansoul.zhihu.global.MyApplication;
import com.mansoul.zhihu.global.NewsApi.Api;
import com.mansoul.zhihu.ui.adapter.NewsItemAdapter;
import com.mansoul.zhihu.utils.HttpUtils;
import com.mansoul.zhihu.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 侧边栏
 * Created by Mansoul on 16/6/6.
 */
public class LeftMenuFragment extends BaseFragment {

    @BindView(R.id.tv_main)
    TextView tv_main;
    @BindView(R.id.lv_item)
    ListView lv_item;

    private OtherNewsFragment otherNewsFragment;
    private MainNewsFragment mainNewsFragment;

    private static final String NEWS_ITEM_URL = Api.BASEURL + Api.THEMES;

    private List<NewsItem.OthersBean> newsItemList;
    private NewsItemAdapter mAdapter;

    @Override
    public View initView() {
        View view = View.inflate(mActivity, R.layout.fragment_left_menu, null);
        ButterKnife.bind(this, view);


        return view;
    }

    @Override
    public void initData() {
        newsItemList = new ArrayList<>();

        boolean isNetworkAvailable = HttpUtils.isNetworkAvailable(mActivity);
        if (isNetworkAvailable) {
            //网络加载数据
            setData();

        } else {
            getCache(NEWS_ITEM_URL);
        }


    }

    public void setData() {
        StringRequest request = new StringRequest(Request.Method.GET, NEWS_ITEM_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //写缓存
                        File cacheFile = HttpCacheManager.getCacheFile(NEWS_ITEM_URL); //缓存文件
                        if (cacheFile == null) {
                            HttpCacheManager.setCache(mActivity, NEWS_ITEM_URL, response);
                        } else {
                            String cache = HttpCacheManager.getCache(NEWS_ITEM_URL);
                            if (cache != null && !cache.equals(response)) {
                                cacheFile.delete();
                                HttpCacheManager.setCache(mActivity, NEWS_ITEM_URL, response);
                            }
                        }

                        parseString(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        request.setTag("newsItem");
        MyApplication.getRequestQueue().add(request);
    }

    private void parseString(String response) {
        Gson gson = new Gson();
        NewsItem newsItem = gson.fromJson(response, NewsItem.class);
        newsItemList = newsItem.getOthers();

        initListView();
    }


    private void initListView() {
        mAdapter = new NewsItemAdapter(newsItemList, mActivity);
        lv_item.setAdapter(mAdapter);

        lv_item.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mActivity.onBackPressed();
                lv_item.setSelector(R.color.selector);
                tv_main.setBackgroundResource(android.R.color.white);

                openNewsFragment(newsItemList.get(position).getId() + "", newsItemList.get(position).getName());
            }
        });

        tv_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.onBackPressed();
                lv_item.setSelector(android.R.color.white);
                tv_main.setBackgroundResource(R.color.selector);

                mAdapter.notifyDataSetChanged();

                if (mainNewsFragment == null) {
                    mainNewsFragment = new MainNewsFragment();
                }

                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction(); //开启事务
                transaction.replace(R.id.fl_main, mainNewsFragment);
                transaction.commit();
            }
        });

    }

    private void openNewsFragment(String title, String id) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction(); //开启事务
        transaction.replace(R.id.fl_main, new OtherNewsFragment(id, title));
        transaction.commit();
    }

    public void getCache(String lastNews) {
        System.out.println("加载缓存了！！！");
        String cache = HttpCacheManager.getCache(lastNews);

        if (!StringUtils.isEmpty(cache)) {
            parseString(cache);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyApplication.getRequestQueue().cancelAll("newsItem");
    }
}
