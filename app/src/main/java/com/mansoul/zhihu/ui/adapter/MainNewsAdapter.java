package com.mansoul.zhihu.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.mansoul.zhihu.R;
import com.mansoul.zhihu.domain.NewsLast;
import com.mansoul.zhihu.global.MyApplication;
import com.mansoul.zhihu.utils.HttpUtils;
import com.mansoul.zhihu.utils.PrefUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mansoul on 16/6/7.
 */
public class MainNewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private static final int NORMAL_ITEM = 0;
    private static final int GROUP_ITEM = 1;

    private LayoutInflater mLayoutInflater;
    private List<NewsLast.StoriesBean> mStories;
    private Context mContext;

    public MainNewsAdapter(List<NewsLast.StoriesBean> mStories, Context mContext) {
        this.mStories = mStories;
        this.mContext = mContext;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == NORMAL_ITEM) {
            View view = mLayoutInflater.inflate(R.layout.item_news_main, parent, false);
            NormalViewHolder holder = new NormalViewHolder(view);

            //3.设置监听()
            view.setOnClickListener(this);

            return holder;
        } else {
            return new TimeViewHolder(mLayoutInflater.inflate(R.layout.item_news_main_with_time, null));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        NormalViewHolder normalHolder = (NormalViewHolder) holder;

        //传递新闻ID
        String id = mStories.get(position).getId() + "";
        normalHolder.itemView.setTag(id);

        //设置标题
        normalHolder.title.setText(mStories.get(position).getTitle());

        //根据本地记录标记已读未读
        String readIdS = PrefUtils.getString(mContext, "readIds", "");
        if (readIdS.contains(id)) {
            normalHolder.title.setTextColor(Color.GRAY);
        } else {
            normalHolder.title.setTextColor(Color.BLACK);
        }

        ImageView img = normalHolder.imageView;

        String imgUrl = mStories.get(position).getImages().get(0);

        String tag = (String) img.getTag();

        if (!imgUrl.equals(tag)) {

            img.setTag(imgUrl);

            //设置图片
            HttpUtils.setImage(imgUrl, normalHolder.imageView);
//        setImg(imgUrl, normalHolder.imageView);
        }


    }

    @Override
    public int getItemCount() {
        return mStories.size();
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (String) v.getTag());
        }
    }


//    @Override
//    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
//    }

    //新闻标题
    class NormalViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.iv_main_news)
        ImageView imageView;
        @BindView(R.id.tv_main_news)
        TextView title;

        public NormalViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class TimeViewHolder extends NormalViewHolder {

        @BindView(R.id.tv_item_time)
        TextView time;

        public TimeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }


    public void setImg(String imgUrl, ImageView imageView) {
        ImageLoader imageLoader = MyApplication.getImageLoader();
        ImageLoader.ImageListener listener = ImageLoader.getImageListener(
                imageView, R.mipmap.moren, R.mipmap.moren);
        imageLoader.get(imgUrl, listener);
    }

    //1.定义接口
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, String data);
    }

    //2.声明接口变量
    private OnRecyclerViewItemClickListener mOnItemClickListener;

    //4.暴露接口回调
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
