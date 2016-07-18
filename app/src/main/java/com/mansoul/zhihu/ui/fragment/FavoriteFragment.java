package com.mansoul.zhihu.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mansoul.zhihu.R;
import com.mansoul.zhihu.domain.NewsLast;
import com.mansoul.zhihu.domain.NewsTheme;
import com.mansoul.zhihu.engine.DBHelper;
import com.mansoul.zhihu.ui.activity.MainActivity;
import com.mansoul.zhihu.ui.activity.NewsContentActivity;
import com.mansoul.zhihu.ui.adapter.OtherNewsAdapter;
import com.mansoul.zhihu.utils.PrefUtils;
import com.mansoul.zhihu.utils.UIUtils;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 我的收藏
 * Created by Mansoul on 16/6/15.
 */
public class FavoriteFragment extends BaseFragment implements MainActivity.FragmentBackListener {

    @BindView(R.id.rv_view)
    RecyclerView mRecyclerView;

    @Override
    public View initView() {
        View view = UIUtils.inflate(R.layout.fragment_fav);
        ButterKnife.bind(this, view);

        LinearLayoutManager llm = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(llm);

        return view;
    }

    @Override
    public void initData() {
        //获取收藏的新闻
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<NewsTheme.StoriesBean> favNewsList = getFavNewsList();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) mActivity).mToolbar.setTitle(favNewsList.size() + " 条收藏");
                        setAdapter(favNewsList);
                    }
                });
            }
        }).start();


    }

    public List<NewsTheme.StoriesBean> getFavNewsList() {
        DBHelper dbHelper = new DBHelper(mActivity, "fav_news");

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query("fav_table", null, null, null, null, null, null, null);

//        Logger.i(cursor.getCount()+"");
        List<NewsTheme.StoriesBean> storiesBean = new ArrayList<>();
        NewsTheme.StoriesBean bean;

        while (cursor.moveToNext()) {


            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String image = cursor.getString(cursor.getColumnIndex("image"));

//            Logger.i(id + "," + title + "," + image);

            List<String> images = new ArrayList<>();
            images.add(image);

            bean = new NewsTheme.StoriesBean();
            bean.setId(id);
            bean.setTitle(title);
            bean.setImages(images);

            storiesBean.add(bean);
        }


        cursor.close();
        db.close();

        return storiesBean;
    }

    public void setAdapter(List<NewsTheme.StoriesBean> list) {
        OtherNewsAdapter mAdapter = new OtherNewsAdapter(list, mActivity);
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

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).setOnBackListener(this);
            ((MainActivity) activity).setInterception(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setOnBackListener(null);
            ((MainActivity) getActivity()).setInterception(false);
        }
    }

    @Override
    public void onBackForward() {
        ((MainActivity) mActivity).openMainNewsFragment();
    }


    @Override
    public void parseData(String response) {

    }

    @Override
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return null;
    }

    @Override
    public String getUrl() {
        return null;
    }

}
