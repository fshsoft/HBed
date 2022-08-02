package com.java.health.care.bed.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.java.health.care.bed.R;

/**
 * @author fsh
 * @date 2022/08/02 13:58
 * @Description
 */
public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(WelcomeActivity.this,PrescriptionActivity.class));
                finish();
            }
        },3000);

    }
}
