package com.mansoul.zhihu.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mansoul.zhihu.R;
import com.mansoul.zhihu.global.MyApplication;
import com.mansoul.zhihu.global.NewsApi.Api;
import com.mansoul.zhihu.utils.HttpUtils;
import com.mansoul.zhihu.utils.ToastUtil;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {

    private static final String START_IMG = Api.BASEURL + Api.START_IMG;

    @BindView(R.id.iv_start)
    ImageView mImageView;
    @BindView(R.id.tv_text)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        initStartImg();

        enterMainActivity();
    }

    private void enterMainActivity() {
        initAnim();
    }

    private void initAnim() {
        //缩放动画
        ScaleAnimation animScale = new ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animScale.setDuration(3000);
        animScale.setFillAfter(false);

        //渐变动画
//        AlphaAnimation animAlpha = new AlphaAnimation(1f, 1f);
//        animAlpha.setDuration(3000);// 动画时间
//        animAlpha.setFillAfter(true);// 保持动画结束状态

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(animScale);
//        set.addAnimation(animAlpha);

        set.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mImageView.startAnimation(set);
    }

    /**
     * 判断网络是否可用
     * 1.可用->网上更新图片
     * 2.不可用->缓存的图片
     */
    private void initStartImg() {
//        File dir = getFilesDir(); //获取的是 data/data/files的路径
//        File imgFile = new File(dir, "start.jpg");
        boolean isNetworkAvailable = HttpUtils.isNetworkAvailable(this);

        if (isNetworkAvailable) {
            System.out.println("网络可用");
            //从网上更新图片
            getDataFormServer(START_IMG);

        } else {
            //加载缓存
            ToastUtil.getInstance().showToast("你当前的网络不可用");
            mImageView.setImageResource(R.drawable.start);
        }
    }

    public void getDataFormServer(final String url) {

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String imgUrl = response.getString("img");
                            String text = response.getString("text");

                            setImage(imgUrl, text);

//                            HttpCacheManager.setCache(url, response.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        request.setShouldCache(true);
        request.setTag("getImgUrl");
        MyApplication.getRequestQueue().add(request);
    }

    private void setImage(String imgUrl, String text) {
        mTextView.setText(text);

        //显示图片
        HttpUtils.setImage(this, imgUrl, mImageView);
//        ImageLoader imageLoader = MyApplication.getImageLoader();
//        ImageLoader.ImageListener listener = ImageLoader.getImageListener(
//                mImageView, R.mipmap.moren, R.mipmap.moren);
//        imageLoader.get(imgUrl, listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyApplication.getRequestQueue().cancelAll("getImgUrl");
    }

    @Override
    public void onBackPressed() {

    }
}

