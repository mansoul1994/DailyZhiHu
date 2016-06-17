package com.mansoul.zhihu.ui.activity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.mansoul.zhihu.R;
import com.mansoul.zhihu.domain.NewsContent;
import com.mansoul.zhihu.engine.DBHelper;
import com.mansoul.zhihu.global.MyApplication;
import com.mansoul.zhihu.global.NewsApi.Api;
import com.mansoul.zhihu.utils.HttpUtils;
import com.mansoul.zhihu.utils.PrefUtils;
import com.mansoul.zhihu.utils.ToastUtil;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

public class NewsContentActivity extends AppCompatActivity {

    @BindView(R.id.image_view)
    ImageView mImageView;
    @BindView(R.id.web_view)
    WebView mWebView;
    @BindView(R.id.tv_place)
    TextView mTvPlace;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.collapsingToolBarLayout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    private String title;
    private int id;
    private String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_content);
        ButterKnife.bind(this);

        initView();

        initData();

    }

    private void initView() {
        //toolbar使用
        setSupportActionBar(mToolbar);

        mCollapsingToolbarLayout.setTitle(" ");

        //设置返回键
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 开启DOM storage API 功能
        mWebView.getSettings().setDomStorageEnabled(true);
        // 开启database storage API功能
        mWebView.getSettings().setDatabaseEnabled(true);
        // 开启Application Cache功能
        mWebView.getSettings().setAppCacheEnabled(true);
    }

    private void initData() {

        String newsId = getIntent().getStringExtra("newsId");
//        String title = getIntent().getStringExtra("title");
        String url = Api.BASEURL + Api.CONTENT + newsId;

        if (HttpUtils.isNetworkAvailable(this)) {
            //加载数据
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Logger.json(response);
                            parseJson(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });
            request.setTag("newsContent");
            MyApplication.getRequestQueue().add(request);

        } else {
            //加载缓存

        }
    }

    private void savaData() {

    }

    private void parseJson(String response) {
        Gson gson = new Gson();
        NewsContent newsContent = gson.fromJson(response, NewsContent.class);

        title = newsContent.getTitle();
        id = newsContent.getId();
        if (newsContent.getImages() != null) {
            image = newsContent.getImages().get(0);
        }


        mTvTitle.setText(title);
        mTvPlace.setText(newsContent.getImage_source());

        String imgUrl = newsContent.getImage();

        if (imgUrl != null) {
            HttpUtils.setImage(imgUrl, mImageView);
        } else if (image != null) {
            HttpUtils.setImage(image, mImageView);
        }

        //加载网页内容
        String css = "<link rel=\"stylesheet\" href=\"file:///android_asset/css/news.css\" type=\"text/css\">";
        String html = "<html><head>" + css + "</head><body>" + newsContent.getBody() + "</body></html>";
        html = html.replace("<div class=\"img-place-holder\">", "");
        mWebView.loadDataWithBaseURL("x-data://base", html, "text/html", "UTF-8", null);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.news_content, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            showShare();
            return true;
        }
        if (id == R.id.action_fav) {
            //是否收藏来进行收藏操作
            //标记已收藏
            String favIds = PrefUtils.getString(this, "favIds", "");

            String ids = id + "";
            if (!favIds.contains(ids)) {
                //收藏
                favIds = favIds + ids + ",";
                PrefUtils.setString(this, "favIds", favIds);
                favoriteNews();
            } else {
                //取消收藏

            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //收藏新闻
    private void favoriteNews() {
        DBHelper dbHelper = new DBHelper(this, "fav_news");
        //得到可写的数据库
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        //添加到数据库
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("title", title);
        values.put("image", image);
        db.insert("fav_table", null, values);

        db.close();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getRequestQueue().cancelAll("newsContent");
    }

    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(title);
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl("http://sharesdk.cn");
        // text是分享文本，所有平台都需要这个字段
        oks.setText(title + "(分享自@知乎日报 App)");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://sharesdk.cn");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://sharesdk.cn");

        // 启动分享GUI
        oks.show(this);
    }
}
