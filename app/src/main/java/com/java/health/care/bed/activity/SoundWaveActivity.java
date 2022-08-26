package com.java.health.care.bed.activity;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.widget.CountDownProgressBar;

/**
 * @author fsh
 * @date 2022/08/25 16:57
 * @Description 声波理疗界面
 */
public class SoundWaveActivity extends BaseActivity {
    private CountDownProgressBar cpb_countdown;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_sound_wave;
    }

    @Override
    protected void initView() {
        Button btn_start = findViewById(R.id.btn_start);
        cpb_countdown = (CountDownProgressBar) findViewById(R.id.cpb_countdown);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cpb_countdown.setDuration(600*1000, new CountDownProgressBar.OnFinishListener() {
                    @Override
                    public void onFinish() {
                        Toast.makeText(SoundWaveActivity.this, "完成了", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    @Override
    protected void initData() {
    }
}
