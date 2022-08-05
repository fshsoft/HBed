package com.java.health.care.bed.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * @author fsh
 * @date 2022/08/02 13:53
 * @Description
 */
public class InputPassWordActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_password;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    @OnClick(R.id.input_password_btn)
    public void inputPassword(){
        goActivity(SettingActivity.class);
    }


}