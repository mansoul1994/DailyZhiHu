package com.mansoul.zhihu.ui.fragment;

import android.app.Activity;
import android.view.View;

import com.mansoul.zhihu.R;
import com.mansoul.zhihu.ui.activity.MainActivity;
import com.mansoul.zhihu.utils.UIUtils;

/**
 * 我的收藏
 * Created by Mansoul on 16/6/15.
 */
public class FavoriteFragment extends BaseFragment implements MainActivity.FragmentBackListener{

    @Override
    public View initView() {
        View view = UIUtils.inflate(R.layout.fragment_fav);

        ((MainActivity) mActivity).mToolbar.setTitle("1 条收藏");

        return view;
    }

    @Override
    public void initData() {

    }

    @Override
    public void parseData(String response) {

    }

    @Override
    public void setStateFalse() {

    }

    @Override
    public void setStateTrue() {

    }

    @Override
    public String getUrl() {
        return null;
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
}
