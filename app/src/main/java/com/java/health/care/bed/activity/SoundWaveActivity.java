package com.java.health.care.bed.activity;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.bean.Param;
import com.java.health.care.bed.bean.UnFinishedPres;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.presenter.MainPresenter;
import com.java.health.care.bed.util.TimeUtil;
import com.java.health.care.bed.widget.CountDownProgressBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author fsh
 * @date 2022/08/25 16:57
 * @Description 声波理疗界面
 * 声波理疗key: back waist leg
 * 背部、腰部、腿部
 */
public class SoundWaveActivity extends BaseActivity implements MainContract.View {
    private MainPresenter presenter;
    private CountDownProgressBar cpb_countdown;
    public static final String TAG = SoundWaveActivity.class.getSimpleName();
    private int preId;
    private String startTime;
    private String endTime;
    //通道
    private int one,two,three,four;
    //时长
    private int duration;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_sound_wave;
    }

    @Override
    protected void initView() {
        Button btn_start = findViewById(R.id.btn_start);
        cpb_countdown = (CountDownProgressBar) findViewById(R.id.cpb_countdown);

        UnFinishedPres unFinishedPres = (UnFinishedPres) getIntent().getParcelableExtra(TAG);
        preId = unFinishedPres.getPreId();
        duration = unFinishedPres.getDuration();
        List<Param> paramList = unFinishedPres.getParam();
        int length = paramList.size();
        for(int i = 0;i<length;i++){
            Param param = paramList.get(i);
            if(param.getKey().trim().equals("back")){
                one = 1;
            }else if(param.getKey().trim().equals("waist")){
                two = 2;
            }else if(param.getKey().trim().equals("leg")){
                three = 3;
            }
        }
        cpb_countdown.setDuration(duration);

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开通道
                String dur = Integer.toHexString(duration/60);
                if(dur.length()==1){
                    dur = "0"+dur;
                }
                if(one!=0){
                    EventBus.getDefault().post(Constant.OPEN_SOUND_WAVE_ONE_HALL + dur
                            +Integer.toHexString(10+duration/60)+ Constant.OPEN_SOUND_WAVE_LAST);
                }
                if(two!=0){
                    EventBus.getDefault().post(Constant.OPEN_SOUND_WAVE_TWO_HALL +  dur
                            +Integer.toHexString(11+duration/60)+ Constant.OPEN_SOUND_WAVE_LAST);
                }
                if(three!=0){
                    EventBus.getDefault().post(Constant.OPEN_SOUND_WAVE_THREE_HALL +  dur
                            +Integer.toHexString(12+duration/60)+ Constant.OPEN_SOUND_WAVE_LAST);
                }
                /*if(four!=0){
                    EventBus.getDefault().post(Constant.OPEN_SOUND_WAVE_FOUR_HALL +  Integer.toHexString(duration/60)
                            +Integer.toHexString(13+duration/60)+ Constant.OPEN_SOUND_WAVE_LAST);
                }*/
                //记录开始时间
                Date d = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                startTime = sdf.format(d);
                cpb_countdown.setDuration(duration*1000, new CountDownProgressBar.OnFinishListener() {
                    @Override
                    public void onFinish() {
                        //记录结束时间
                        Date d = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        endTime = sdf.format(d);
                        presenter.upExec(preId,"SONIC_WAVE",duration,startTime,endTime);

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
        EventBus.getDefault().register(this);
        presenter = new MainPresenter(this, this);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {

    }
    @Override
    public void setCode(String code) {
        if(code.equals("200")){
            Toast.makeText(SoundWaveActivity.this, "理疗完成，报告上传成功", Toast.LENGTH_SHORT).show();
            goActivity(PrescriptionActivity.class);
            finish();
        }
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
