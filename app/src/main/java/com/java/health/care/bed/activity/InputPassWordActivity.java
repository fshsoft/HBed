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
 * @date 2022/08/02 13:53
 * @Description
 */
public class InputPassWordActivity extends AppCompatActivity {
    private Unbinder unbinder;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        unbinder= ButterKnife.bind(this);

    }

    @OnClick(R.id.input_password_btn)
    public void inputPassword(){
        startActivity(new Intent(InputPassWordActivity.this,SettingActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
