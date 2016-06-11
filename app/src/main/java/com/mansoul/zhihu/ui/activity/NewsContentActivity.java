package com.mansoul.zhihu.ui.activity;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.mansoul.zhihu.global.MyApplication;
import com.mansoul.zhihu.global.NewsApi.Api;
import com.mansoul.zhihu.utils.HttpUtils;
import com.mansoul.zhihu.utils.LogUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsContentActivity extends AppCompatActivity {

    private String url;

    @BindView(R.id.image_view)
    ImageView mImage;
    @BindView(R.id.web_view)
    WebView mWebView;
    @BindView(R.id.tv_place)
    TextView mTvPlace;
    @BindView(R.id.tv_title)
    TextView mTvTitle;


    private Toolbar mToolbar;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_content);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolBarLayout);
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
        url = Api.BASEURL + Api.CONTENT + newsId;

        if (HttpUtils.isNetworkAvailable(this)) {
            //加载数据
            StringRequest request = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
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

    private void parseJson(String response) {
        Gson gson = new Gson();
        NewsContent newsContent = gson.fromJson(response, NewsContent.class);

        mTvTitle.setText(newsContent.getTitle());
        mTvPlace.setText(newsContent.getImage_source());

        String imgUrl = newsContent.getImage();

        //显示图片
//        ImageLoader imageLoader = ImageLoader.getInstance();
//        DisplayImageOptions options = new DisplayImageOptions.Builder()
//                .cacheInMemory(true)
//                .cacheOnDisk(true)
//                .build();
//        imageLoader.displayImage(newsContent.getImage(), mImage, options);
        HttpUtils.setImage(imgUrl, mImage);

        //加载网页内容
        String css = "<link rel=\"stylesheet\" href=\"file:///android_asset/css/news.css\" type=\"text/css\">";
        String html = "<html><head>" + css + "</head><body>" + newsContent.getBody() + "</body></html>";
        html = html.replace("<div class=\"img-place-holder\">", "");
        mWebView.loadDataWithBaseURL("x-data://base", html, "text/html", "UTF-8", null);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getRequestQueue().cancelAll("newsContent");
    }
}
