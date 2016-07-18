package com.mansoul.zhihu.ui.fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mansoul.zhihu.R;
import com.mansoul.zhihu.domain.NewsItem;
import com.mansoul.zhihu.global.NewsApi.Api;
import com.mansoul.zhihu.ui.activity.MainActivity;
import com.mansoul.zhihu.ui.adapter.NewsItemAdapter;
import com.mansoul.zhihu.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 侧边栏
 * Created by Mansoul on 16/6/6.
 */
public class LeftMenuFragment extends BaseFragment {

    @BindView(R.id.tv_main)
    TextView tv_main;
    @BindView(R.id.lv_item)
    ListView lv_item;
    @BindView(R.id.ll_favorite)
    LinearLayout llFavorite;
    @BindView(R.id.ll_download)
    LinearLayout llDownload;

    private static final String NEWS_ITEM_URL = Api.BASEURL + Api.THEMES; //侧边栏菜单条目
    private List<NewsItem.OthersBean> newsItemList;
    private NewsItemAdapter mAdapter;
    private FavoriteFragment favoriteFragment;
    private FragmentManager mFm;

    @Override
    public View initView() {
        View view = View.inflate(mActivity, R.layout.fragment_left_menu, null);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void initData() {
        if (mFm == null) {
            mFm = getFragmentManager();
        }
        newsItemList = new ArrayList<>();
    }

    @Override
    public void parseData(String response) {
        if (response != null) {
            Gson gson = new Gson();
            NewsItem newsItem = gson.fromJson(response, NewsItem.class);
            newsItemList = newsItem.getOthers();

            initListView(newsItemList);
        }
    }

    @Override
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return null;
    }

    private void initListView(final List<NewsItem.OthersBean> item) {

        mAdapter = new NewsItemAdapter(item, mActivity);
        lv_item.setAdapter(mAdapter);

        lv_item.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mActivity.onBackPressed();
                lv_item.setSelector(R.color.selector);
                tv_main.setBackgroundResource(android.R.color.white);

                String newId = item.get(position).getId() + "";
                String title = item.get(position).getName();

                openOtherNewsFragment(newId, title);
            }
        });
    }

    //打开首页
    @OnClick(R.id.tv_main)
    public void mainFragment() {
        mActivity.onBackPressed();
        lv_item.setSelector(android.R.color.white);
        tv_main.setBackgroundResource(R.color.selector);
        mAdapter.notifyDataSetChanged();
        ((MainActivity) mActivity).openMainNewsFragment();
    }

    //打开收藏
    @OnClick(R.id.ll_favorite)
    public void openFav() {
        mActivity.onBackPressed();
        if (favoriteFragment == null) {
            favoriteFragment = new FavoriteFragment();
        }
        FragmentTransaction transaction = mFm.beginTransaction();
        transaction.replace(R.id.fl_main, favoriteFragment);
        transaction.commit();
    }

    //打开下载
    @OnClick(R.id.ll_download)
    public void openDownload() {

    }

    private void openOtherNewsFragment(String title, String id) {
        FragmentTransaction transaction = mFm.beginTransaction();
        transaction.replace(R.id.fl_main, new OtherNewsFragment(id, title));
        transaction.commit();
    }

    @Override
    public String getUrl() {
        return NEWS_ITEM_URL;
    }

}
