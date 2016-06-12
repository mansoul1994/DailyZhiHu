package com.mansoul.zhihu.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.mansoul.zhihu.R;
import com.mansoul.zhihu.domain.NewsTheme;
import com.mansoul.zhihu.global.MyApplication;
import com.mansoul.zhihu.global.NewsApi.Api;
import com.mansoul.zhihu.ui.activity.MainActivity;
import com.mansoul.zhihu.ui.activity.NewsContentActivity;
import com.mansoul.zhihu.ui.adapter.OtherNewsAdapter;
import com.mansoul.zhihu.utils.PrefUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mansoul on 16/6/7.
 */
public class OtherNewsFragment extends BaseFragment implements MainActivity.FragmentBackListener{

    private static String URL = null;
    private String title;
    @BindView(R.id.rv_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.tv_top)
    TextView mTitle;
    @BindView(R.id.iv_top)
    ImageView mTopImg;
    @BindView(R.id.srl_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    public OtherNewsFragment(String title, String id) {
        URL = Api.BASEURL + Api.THEMENEWS + id;
        this.title = title;
    }

    @Override
    public View initView() {
        View view = View.inflate(mActivity, R.layout.fragment_news, null);
        ButterKnife.bind(this, view);

        LinearLayoutManager llm = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);

        ((MainActivity) mActivity).mToolbar.setTitle(title);

//        ((MainActivity) mActivity).getSupportFragmentManager().popBackStack();
        mRecyclerView.setFocusable(true);



        return view;
    }

    @Override
    public void initData() {

        getDataFormServer();
        mSwipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorAccent),
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorAccent));
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDataFormServer();
            }
        });
    }

    private boolean isLoading = false;

    public void getDataFormServer() {

        if (!isLoading) {
            isLoading = true;
            mSwipeRefresh.setRefreshing(true);
            StringRequest request = new StringRequest(Request.Method.GET, URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            System.out.println(response);

                            //解析json数据
                            parseData(response);

                            mSwipeRefresh.setRefreshing(false);
                            isLoading = false;
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });
            request.setTag("lastNews");
            MyApplication.getRequestQueue().add(request);
        }
    }

    private void parseData(String response) {
        Gson gson = new Gson();
        NewsTheme newsTheme = gson.fromJson(response, NewsTheme.class);

        List<NewsTheme.StoriesBean> stories = newsTheme.getStories();

        initHeader(newsTheme);

        OtherNewsAdapter mAdapter = new OtherNewsAdapter(stories, mActivity);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new OtherNewsAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, String data) {
                //标记已读新闻
                String readIds = PrefUtils.getString(mActivity, "readIds", "");
                //已经标记的不需要重复标记
                if (!readIds.contains(data)) {
                    readIds = readIds + data + ",";
                    PrefUtils.setString(mActivity, "readIds", readIds);
                }

                //将被点击的item标记已读
                TextView title = (TextView) view.findViewById(R.id.tv_main_news);
                title.setTextColor(Color.GRAY);

                Intent intent = new Intent(mActivity, NewsContentActivity.class);
                intent.putExtra("newsId", data);
                mActivity.startActivity(intent);
            }
        });


    }

    private void initHeader(NewsTheme newsTheme) {
        mTitle.setText(newsTheme.getDescription());

        ImageLoader imageLoader = MyApplication.getImageLoader();
        ImageLoader.ImageListener listener = ImageLoader.getImageListener(
                mTopImg, R.mipmap.moren, R.mipmap.moren);
        imageLoader.get(newsTheme.getBackground(), listener);
    }



    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        if(activity instanceof MainActivity){
            ((MainActivity)activity).setOnBackListener(this);
            ((MainActivity)activity).setInterception(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(getActivity() instanceof MainActivity){
            ((MainActivity)getActivity()).setOnBackListener(null);
            ((MainActivity)getActivity()).setInterception(false);
        }
    }

    @Override
    public void onBackForward() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction(); //开启事务
        transaction.replace(R.id.fl_main, new MainNewsFragment());
        transaction.commit();
    }

}
