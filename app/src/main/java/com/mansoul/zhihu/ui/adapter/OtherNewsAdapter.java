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
import com.mansoul.zhihu.domain.NewsTheme;
import com.mansoul.zhihu.global.MyApplication;
import com.mansoul.zhihu.utils.HttpUtils;
import com.mansoul.zhihu.utils.PrefUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mansoul on 16/6/7.
 */
public class OtherNewsAdapter extends RecyclerView.Adapter<OtherNewsAdapter.NormalViewHolder> implements View.OnClickListener {

    private final Context mContext;
    private LayoutInflater mLayoutInflater = null;
    private List<NewsTheme.StoriesBean> mStories;

    public OtherNewsAdapter(List<NewsTheme.StoriesBean> mStories, Context mContext) {
        this.mStories = mStories;
        if (mContext != null) {
            mLayoutInflater = LayoutInflater.from(mContext);
        }
        this.mContext = mContext;
    }

    @Override
    public NormalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mLayoutInflater != null) {

            View view = mLayoutInflater.inflate(R.layout.item_news_main, parent, false);
            NormalViewHolder holder = new NormalViewHolder(view);

            //3.设置监听()
            view.setOnClickListener(this);

            return holder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(NormalViewHolder holder, int position) {
        //传递新闻ID
        String id = mStories.get(position).getId() + "";
        holder.itemView.setTag(id);

        //设置标题
        holder.title.setText(mStories.get(position).getTitle());

        //根据本地记录标记已读未读
        String readIdS = PrefUtils.getString(mContext, "readIds", "");
        if (readIdS.contains(id)) {
            holder.title.setTextColor(Color.GRAY);
        } else {
            holder.title.setTextColor(Color.BLACK);
        }

        List<String> images = mStories.get(position).getImages();
        if (images != null) {
            String imgUrl = mStories.get(position).getImages().get(0);

            //显示图片
            HttpUtils.setImage(mContext, imgUrl, holder.imageView);

//            setImg(imgUrl, holder.imageView);
        } else {
            holder.imageView.setVisibility(View.GONE);
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
