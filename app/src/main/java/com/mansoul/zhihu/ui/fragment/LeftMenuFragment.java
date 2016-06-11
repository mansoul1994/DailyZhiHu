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
import com.mansoul.zhihu.R;
import com.mansoul.zhihu.domain.NewsItem;
import com.mansoul.zhihu.global.MyApplication;
import com.mansoul.zhihu.global.NewsApi.Api;
import com.mansoul.zhihu.ui.adapter.NewsItemAdapter;
import com.mansoul.zhihu.utils.HttpUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    private static final String NEWS_ITEM_URL = Api.BASEURL + Api.THEMES;

    private List<NewsItem> newsItemList;
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

        }


    }

    public void setData() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, NEWS_ITEM_URL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseJson(response);
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

    private void parseJson(JSONObject response) {

        try {
            JSONArray ja = response.getJSONArray("others");
            for (int i = 0; i < ja.length(); i++) {
                NewsItem newsItem = new NewsItem();
                JSONObject jo = ja.getJSONObject(i);
                newsItem.setId(jo.getString("id"));
                newsItem.setName(jo.getString("name"));
                newsItemList.add(newsItem);
            }

            initListView();

        } catch (JSONException e) {
            e.printStackTrace();
        }
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

                openNewsFragment(newsItemList.get(position).getId(), newsItemList.get(position).getName());
            }
        });

        tv_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.onBackPressed();
                lv_item.setSelector(android.R.color.white);
                tv_main.setBackgroundResource(R.color.selector);

                mAdapter.notifyDataSetChanged();

                FragmentManager fm = getFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction(); //开启事务
                transaction.replace(R.id.fl_main, new MainNewsFragment());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyApplication.getRequestQueue().cancelAll("newsItem");
    }
}
