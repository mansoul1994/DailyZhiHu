package com.mansoul.zhihu.ui.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.mansoul.zhihu.R;
import com.mansoul.zhihu.cache.HttpCacheManager;
import com.mansoul.zhihu.domain.NewsLast;
import com.mansoul.zhihu.global.MyApplication;
import com.mansoul.zhihu.global.NewsApi.Api;
import com.mansoul.zhihu.ui.activity.MainActivity;
import com.mansoul.zhihu.ui.activity.NewsContentActivity;
import com.mansoul.zhihu.ui.adapter.MainNewsAdapter;
import com.mansoul.zhihu.ui.adapter.TopNewsPagerAdapter;
import com.mansoul.zhihu.ui.view.MainNewsScrollView;
import com.mansoul.zhihu.utils.HttpUtils;
import com.mansoul.zhihu.utils.LogUtils;
import com.mansoul.zhihu.utils.PrefUtils;
import com.mansoul.zhihu.utils.StringUtils;
import com.mansoul.zhihu.utils.ToastUtil;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 主界面内容
 * Created by Mansoul on 16/6/6.
 */
public class MainNewsFragment extends BaseFragment {

    private static final String LAST_NEWS = Api.BASEURL + Api.LATESTNEWS;
    private String data;

    @BindView(R.id.rv_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.srl_refresh)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.scrollview)
    MainNewsScrollView mScrollView;
    @BindView(R.id.fl_container)
    FrameLayout frameLayout;

    private ViewPager mViewPager;
    private TextView mTopNewsTitle;
    private LinearLayout mContainer;

    private Handler handler;
    private List<NewsLast.StoriesBean> mStories;
    private MainNewsAdapter mAdapter;

    @Override
    public View initView() {
        View view = View.inflate(mActivity, R.layout.fragment_main, null);
        ButterKnife.bind(this, view);

        mViewPager = (ViewPager) view.findViewById(R.id.view_pager);
        mTopNewsTitle = (TextView) view.findViewById(R.id.tv_top_news);
        mContainer = (LinearLayout) view.findViewById(R.id.ll_container);

        ((MainActivity) mActivity).mToolbar.setTitle("今日热闻");

        LinearLayoutManager llm = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);

        handler = new Handler();


        return view;
    }

    @Override
    public void initData() {

        if (HttpUtils.isNetworkAvailable(mActivity)) {
            getDataFormServer(LAST_NEWS);
        } else {
            getCache(LAST_NEWS);
        }


        mSwipeRefresh.setColorSchemeColors(
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorAccent),
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.colorAccent));
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (HttpUtils.isNetworkAvailable(mActivity)) {
                    getDataFormServer(LAST_NEWS);
                }
                mSwipeRefresh.setRefreshing(false);
            }
        });

        mScrollView.setOnScrollBottomListener(new MainNewsScrollView.ScrollBottomListener() {
            @Override
            public void scrollBottom() {
                if (!isLoadMore) {
                    LogUtils.i("loadMore");
                    loadMore();
                }
            }
        });
    }

    boolean isLoadMore = false;

    private void loadMore() {
        isLoadMore = true;
        String url = Api.BASEURL + Api.BEFORE + data;

        if (HttpUtils.isNetworkAvailable(mActivity)) {

            getMoreData(url);
        } else {
            getMoreCache(url);
        }
    }

    private void getMoreData(final String url) {
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
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

                        parseMoreData(response);
                        isLoadMore = false;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ToastUtil.getInstance().showToast("没有更多数据了!");
                    }
                });
        request.setTag("beforeNews");
        MyApplication.getRequestQueue().add(request);
    }

    private void parseMoreData(String response) {
        Gson gson = new Gson();
        NewsLast newsLast = gson.fromJson(response, NewsLast.class);
        List<NewsLast.StoriesBean> stories = newsLast.getStories();

        mStories.addAll(stories);
        mAdapter.notifyDataSetChanged();

        data = newsLast.getDate();
    }

    private boolean isLoading = false;

    public void getDataFormServer(final String url) {

        if (!isLoading) {
            isLoading = true;
            mSwipeRefresh.setRefreshing(true);

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

    private void parseData(String json) {
        Gson gson = new Gson();
        NewsLast newsLast = gson.fromJson(json, NewsLast.class);
        mStories = newsLast.getStories();
        List<NewsLast.TopStoriesBean> top_stories = newsLast.getTop_stories();

        //加载以前新闻data
        data = newsLast.getDate();

        //初始化头布局
        initHeader(top_stories);

        mAdapter = new MainNewsAdapter(mStories, mActivity);
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new MainNewsAdapter.OnRecyclerViewItemClickListener() {
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


        System.out.println(newsLast.getDate());
    }

    //轮播图控制
    private boolean isLoop = true;

    private void initHeader(final List<NewsLast.TopStoriesBean> top_stories) {
        TopNewsPagerAdapter adapter = new TopNewsPagerAdapter(top_stories, mActivity);
        mViewPager.setAdapter(adapter);

        mViewPager.setCurrentItem(top_stories.size() * 100000);
        mTopNewsTitle.setText(top_stories.get(0).getTitle());

        //动态添加指示器(小点)
        mContainer.removeAllViews();
        for (int i = 0; i < top_stories.size(); i++) {
            ImageView imageView = new ImageView(mActivity);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            if (i == 0) {
                imageView.setImageResource(R.drawable.indicator_selected);
            } else {
                imageView.setImageResource(R.drawable.indicator_normal);

                params.leftMargin = dip2px(3);
            }

            imageView.setLayoutParams(params);

            mContainer.addView(imageView);
        }

        //手指滑动viewpager停止轮播, 松开自动轮播;
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
//                        LogUtils.i("停止轮播");
                        isLoop = false;
                        break;
                    case MotionEvent.ACTION_UP:
                        LogUtils.i("开始轮播");
                        isLoop = true;
                        break;
                }
                return false;
            }
        });

        //开启自动轮播
        Task task = new Task();
        task.start();

        initViewPager(top_stories);

    }

    private int prePosition;

    //轮播图,初始化指示器
    private void initViewPager(final List<NewsLast.TopStoriesBean> top_stories) {


        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                position = position % top_stories.size();

                mTopNewsTitle.setText(top_stories.get(position).getTitle());

                ImageView imageView = (ImageView) mContainer.getChildAt(position);
                imageView.setImageResource(R.drawable.indicator_selected);

                //前一个设置为为选中
                ImageView preView = (ImageView) mContainer.getChildAt(prePosition);
                preView.setImageResource(R.drawable.indicator_normal);

                prePosition = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    //轮播任务
    class Task implements Runnable {
        public void start() {
            //移除以前发送的所有消息, 以免影响现在的消息
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(this, 7000);
        }


        @Override
        public void run() {
            if (isLoop) {
                int currentItem = mViewPager.getCurrentItem();
                currentItem++;
                mViewPager.setCurrentItem(currentItem);
            }
            if (handler != null) {
                handler.postDelayed(this, 7000);
            }
        }
    }

    public void getCache(String lastNews) {
        mSwipeRefresh.setRefreshing(false);
        System.out.println("加载缓存了！！！");
        String cache = HttpCacheManager.getCache(lastNews);

        if (!StringUtils.isEmpty(cache)) {
            parseData(cache);
        }
    }

    public void getMoreCache(String lastNews) {
        mSwipeRefresh.setRefreshing(false);
        System.out.println("加载缓存了！！！");
        String cache = HttpCacheManager.getCache(lastNews);
        isLoadMore = false;

        if (!StringUtils.isEmpty(cache)) {
            parseMoreData(cache);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivity = null;
        handler = null;
        MyApplication.getRequestQueue().cancelAll("lastNews");
        MyApplication.getRequestQueue().cancelAll("beforeNews");
    }

    public int dip2px(float dip) {
        float density = getResources().getDisplayMetrics().density; //获取手机密度
        return (int) (dip * density + 0.5f);
    }
}
