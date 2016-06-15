package com.mansoul.zhihu.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.mansoul.zhihu.R;
import com.mansoul.zhihu.domain.NewsLast;
import com.mansoul.zhihu.global.MyApplication;
import com.mansoul.zhihu.ui.activity.NewsContentActivity;
import com.mansoul.zhihu.utils.HttpUtils;
import com.mansoul.zhihu.utils.LogUtils;
import com.mansoul.zhihu.utils.PrefUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.List;

/**
 * Created by Mansoul on 16/6/8.
 */
public class TopNewsPagerAdapter extends PagerAdapter {
    List<NewsLast.TopStoriesBean> top_stories;
    Activity mActivity;

    public TopNewsPagerAdapter(List<NewsLast.TopStoriesBean> top_stories, Activity mActivity) {
        this.top_stories = top_stories;
        this.mActivity = mActivity;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        position = position % top_stories.size();

        String imgUrl = top_stories.get(position).getImage();
        ImageView view = new ImageView(mActivity);
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);

        String tag = (String) view.getTag();

        if (!imgUrl.equals(tag)) {
            view.setTag(imgUrl);
            //设置图片
            HttpUtils.setImage(imgUrl, view);
        }

        final int finalPosition = position;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.i(top_stories.get(finalPosition).getId() + "");
                String newsId = top_stories.get(finalPosition).getId() + "";
                String title = top_stories.get(finalPosition).getTitle();

                PrefUtils.setString(mActivity, "readIds", newsId);

                Intent intent = new Intent(mActivity, NewsContentActivity.class);
                intent.putExtra("newsId", newsId);
                intent.putExtra("title", title);
                mActivity.startActivity(intent);
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
