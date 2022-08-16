package com.java.health.care.bed.activity;

import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.constant.Constant;

import org.greenrobot.eventbus.EventBus;

import butterknife.OnClick;

/**
 * @author fsh
 * @date 2022/08/15 15:03
 * @Description 康养床ble设置
 */
public class KYCSetActivity extends BaseActivity {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_kyc_set;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
//        DataTransmitter.getInstance().addDataReceiver(SoundWaveSetActivity.this);
    }

    @OnClick(R.id.sound_wave_one_on)
    public void soundWaveOneOn(){
        //FE0801010A14FF16  第一通道10分钟
        EventBus.getDefault().post(Constant.OPEN_SOUND_WAVE_ONE_HALL+Constant.OPEN_SOUND_WAVE_TIME
                +Constant.OPEN_SOUND_WAVE_LAST_ONE);
    }

    @OnClick(R.id.sound_wave_one_off)
    public void soundWaveOneOff(){
        EventBus.getDefault().post(Constant.CLOSE_SOUND_WAVE_ONE_HALL);
    }

    @OnClick(R.id.sound_wave_two_on)
    public void soundWaveTwoOn(){
        //FE0801020A15FF16  第二通道10分钟
        EventBus.getDefault().post(Constant.OPEN_SOUND_WAVE_TWO_HALL+Constant.OPEN_SOUND_WAVE_TIME
                +Constant.OPEN_SOUND_WAVE_LAST_TWO);
    }
    @OnClick(R.id.sound_wave_two_off)
    public void soundWaveTwoOff(){
        EventBus.getDefault().post(Constant.CLOSE_SOUND_WAVE_TWO_HALL);
    }

    @OnClick(R.id.sound_wave_three_on)
    public void soundWaveThreeOn(){
        //FE0801030A16FF16  第三通道10分钟
        EventBus.getDefault().post(Constant.OPEN_SOUND_WAVE_THREE_HALL+Constant.OPEN_SOUND_WAVE_TIME
                +Constant.OPEN_SOUND_WAVE_LAST_THREE);
    }
    @OnClick(R.id.sound_wave_three_off)
    public void soundWaveThreeOff(){
        EventBus.getDefault().post(Constant.CLOSE_SOUND_WAVE_THREE_HALL);
    }

    @OnClick(R.id.sound_wave_four_on)
    public void soundWaveFourOn(){
        //FE0801040A17FF16  第四通道10分钟
        EventBus.getDefault().post(Constant.OPEN_SOUND_WAVE_FOUR_HALL+Constant.OPEN_SOUND_WAVE_TIME
                +Constant.OPEN_SOUND_WAVE_LAST_FOUR);
    }
    @OnClick(R.id.sound_wave_four_off)
    public void soundWaveFourOff(){
        EventBus.getDefault().post(Constant.CLOSE_SOUND_WAVE_FOUR_HALL);
    }

    //香薰
    @OnClick(R.id.sweet_on)
    public void sweetOn(){
        EventBus.getDefault().post(Constant.OPEN_SWEET_ONE_HALL+Constant.OPEN_SWEET_TIME+
                Constant.SWEET_CODE+Constant.OPEN_SWEET_LAST);
    }

    @OnClick(R.id.sweet_off)
    public void sweetOff(){
        EventBus.getDefault().post(Constant.CLOSE_SWEET_ONE_HALL);
    }

    //呼叫
    @OnClick(R.id.call_on)
    public void callOn(){
        EventBus.getDefault().post(Constant.CALL_ON);
    }

    @OnClick(R.id.call_off)
    public void callOff(){
        EventBus.getDefault().post(Constant.CALL_OFF);
    }

}
