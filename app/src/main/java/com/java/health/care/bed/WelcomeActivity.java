package com.java.health.care.bed;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.java.fsh.soft.common.MainActivity;
import com.java.health.care.bed.activity.PrescriptionActivity;

/**
 * @author fsh
 * @date 2022/07/29 14:00
 * @Description 欢迎页
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(WelcomeActivity.this, PrescriptionActivity.class));
            }
        },3000);


    }



}