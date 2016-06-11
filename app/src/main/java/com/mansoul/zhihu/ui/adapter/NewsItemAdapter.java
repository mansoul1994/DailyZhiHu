package com.mansoul.zhihu.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.mansoul.zhihu.R;
import com.mansoul.zhihu.domain.NewsItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mansoul on 16/6/6.
 */
public class NewsItemAdapter extends BaseAdapter {

    private List<NewsItem> mItem;
    private Context mContext;

    public NewsItemAdapter(List<NewsItem> mItem, Context mContext) {
        this.mItem = mItem;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mItem.size();
    }

    @Override
    public Object getItem(int position) {
        return mItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_news_menu, null);
        }

        TextView tv_item = (TextView) convertView.findViewById(R.id.tv_item);
        tv_item.setText(mItem.get(position).getName());

        return convertView;
    }
}
