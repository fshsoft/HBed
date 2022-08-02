package com.java.health.care.bed.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.java.health.care.bed.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * @author fsh
 * @date 2022/08/02 13:55
 * @Description
 */
public class SettingActivity extends AppCompatActivity {
    private Unbinder unbinder;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        unbinder= ButterKnife.bind(this);
    }

    @OnClick(R.id.set_ble_set_rl)
    public void setBle(){
        startActivity(new Intent(SettingActivity.this,BleSettingActivity.class));
    }

    @OnClick(R.id.set_about_us_rl)
    public void aboutUs(){

    }

    @OnClick(R.id.set_version_update_rl)
    public void updateVersion(){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
