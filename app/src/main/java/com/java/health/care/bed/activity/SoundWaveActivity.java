package com.java.health.care.bed.activity;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.presenter.MainPresenter;
import com.java.health.care.bed.widget.CountDownProgressBar;

/**
 * @author fsh
 * @date 2022/08/25 16:57
 * @Description 声波理疗界面
 */
public class SoundWaveActivity extends BaseActivity implements MainContract.View {
    private MainPresenter presenter;
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
        /**
         *   *  "duration": 100,
         *      * 	"endTime": "2022-12-22 14:30:50",
         *      *  "preId": 3,
         *      * 	"preType": "SONIC_WAVE",
         *      * 	"startTime": "2022-12-22 14:25:50"
         */
        presenter = new MainPresenter(this, this);
        presenter.upExec(3,"SONIC_WAVE",100,"2022-12-22 14:25:50","2022-12-22 14:30:50");
    }

    @Override
    public void setCode(String code) {

    }

    @Override
    public void setMsg(String msg) {

    }

    @Override
    public void setInfo(String msg) {

    }

    @Override
    public void setObj(Object obj) {

    }

    @Override
    public void setData(Object obj) {

    }
}
