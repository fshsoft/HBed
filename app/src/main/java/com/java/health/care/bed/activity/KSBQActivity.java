package com.java.health.care.bed.activity;

import android.widget.Button;

import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;


import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author fsh
 * @date 2022/09/01 14:33
 * @Description  科室病区选择
 */
public class KSBQActivity extends BaseActivity {
    @BindView(R.id.mButton)
    Button mButton;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ksbq;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    @OnClick(R.id.mButton)
    public void btnOnClick(){

    }

}
