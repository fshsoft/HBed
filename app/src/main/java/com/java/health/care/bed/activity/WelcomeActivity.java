package com.java.health.care.bed.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.constant.SP;


/**
 * @author fsh
 * @date 2022/08/02 13:58
 * @Description
 */
public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //判断是否有IP地址，是否有科室，病区，床号
                String ip = SPUtils.getInstance().getString(SP.IP_SERVER_ADDRESS);
                String dept = SPUtils.getInstance().getString(SP.DEPT_NUM);
                String region = SPUtils.getInstance().getString(SP.REGION_NUM);
                String bunk = SPUtils.getInstance().getString(SP.BUNK_NUM);
                if(ip.isEmpty() || dept.isEmpty() || region.isEmpty() || bunk.isEmpty()){
                    startActivity(new Intent(WelcomeActivity.this,SettingActivity.class));
                }else {
                    startActivity(new Intent(WelcomeActivity.this,PrescriptionActivity.class));
                }

                finish();
            }
        },3000);

    }

}
