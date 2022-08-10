package com.java.health.care.bed.activity;

import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;

import butterknife.OnClick;


/**
 * @author fsh
 * @date 2022/08/02 13:55
 * @Description
 */
public class SettingActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_set;
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {

    }

    @OnClick(R.id.set_ble_set_rl)
    public void setBle(){
        goActivity(BleSettingActivity.class);
    }

    @OnClick(R.id.set_about_us_rl)
    public void aboutUs(){

    }

    @OnClick(R.id.set_version_update_rl)
    public void updateVersion(){

    }

}
