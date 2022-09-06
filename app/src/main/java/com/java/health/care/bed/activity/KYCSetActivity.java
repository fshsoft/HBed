package com.java.health.care.bed.activity;

import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.model.BPDevicePacket;
import com.java.health.care.bed.model.DataReceiver;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.model.EstimateRet;
import com.java.health.care.bed.widget.MyEcgView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author fsh
 * @date 2022/08/15 15:03
 * @Description 康养床ble设置
 */
public class KYCSetActivity extends BaseActivity implements DataReceiver, CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.patient_view_signal)
    MyEcgView myEcgView;
    @BindView(R.id.patient_view_resp)
    MyEcgView myRespView;
    @BindView(R.id.kyc_heart_rate)
    TextView kyc_heart_rate;
    @BindView(R.id.kyc_resp_rate)
    TextView kyc_resp_rate;
    @BindView(R.id.kyc_spo2_data)
    TextView kyc_spo2_data;
    @BindView(R.id.kyc_irt_data)
    TextView kyc_irt_data;
    @BindView(R.id.kyc_bp_data)
    TextView kyc_bp_data;

    //switch
    @BindView(R.id.sound_wave_one)
    Switch sound_wave_one;
    @BindView(R.id.sound_wave_two)
    Switch sound_wave_two;
    @BindView(R.id.sound_wave_three)
    Switch sound_wave_three;
    @BindView(R.id.sound_wave_four)
    Switch sound_wave_four;

    @BindView(R.id.sweet_one)
    Switch sweet_one;
    @BindView(R.id.sweet_two)
    Switch sweet_two;
    @BindView(R.id.sweet_three)
    Switch sweet_three;
    @BindView(R.id.sweet_four)
    Switch sweet_four;

    @BindView(R.id.call)
    Switch call;
    //无创连续血压
    @BindView(R.id.patient_view_signal_cm22)
    MyEcgView myEcgViewCM22;
    @BindView(R.id.patient_view_ppg)
    MyEcgView myPPGView;
    @BindView(R.id.kyc_heart_rate_cm22)
    TextView kyc_heart_rate_cm22;
    @BindView(R.id.kyc_press)
    TextView kyc_press;
    private boolean startDraw = false;

    //只启动一个线程
    public ExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Override
    protected int getLayoutId() {
        return R.layout.activity_kyc_set;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        DataTransmitter.getInstance().addDataReceiver(this);

        sound_wave_one.setOnCheckedChangeListener(this);
        sound_wave_two.setOnCheckedChangeListener(this);
        sound_wave_three.setOnCheckedChangeListener(this);
        sound_wave_four.setOnCheckedChangeListener(this);
        sweet_one.setOnCheckedChangeListener(this);
        sweet_two.setOnCheckedChangeListener(this);
        sweet_three.setOnCheckedChangeListener(this);
        sweet_four.setOnCheckedChangeListener(this);
        call.setOnCheckedChangeListener(this);
    }

    @Override
    protected void initData() {

        myEcgView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //重绘完毕
                startDraw = false;
            }
        });
        myRespView.setRespColor();
        myRespView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //重绘完毕
                startDraw = false;
            }
        });


        myEcgViewCM22.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //重绘完毕
                startDraw = false;
            }
        });

        myPPGView.setRespColor();
        myPPGView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //重绘完毕
                startDraw = false;
            }
        });

    }

    private void refreshEcgData() {
        if (!startDraw) {
            startDraw = true;
            if(myEcgView!=null){
                myEcgView.refreshView();
            }
            if(myRespView!=null){
                myRespView.refreshView();
            }
            if(myEcgViewCM22!=null){
                myEcgViewCM22.refreshView();
            }
            if(myPPGView!=null){
                myPPGView.refreshView();
            }

        } else {
            startDraw = false;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()){
                //声波
                case R.id.sound_wave_one:
                    if(isChecked){
                        //FE0801010A14FF16  第一通道10分钟 开
                        EventBus.getDefault().post(Constant.OPEN_SOUND_WAVE_ONE_HALL + Constant.OPEN_SOUND_WAVE_TIME
                                + Constant.OPEN_SOUND_WAVE_LAST_ONE);
                    }else {
                        EventBus.getDefault().post(Constant.CLOSE_SOUND_WAVE_ONE_HALL);
                    }
                    break;
                case R.id.sound_wave_two:
                    if(isChecked){
                        //FE0801020A15FF16  第二通道10分钟
                        EventBus.getDefault().post(Constant.OPEN_SOUND_WAVE_TWO_HALL + Constant.OPEN_SOUND_WAVE_TIME
                                + Constant.OPEN_SOUND_WAVE_LAST_TWO);
                    }else {
                        EventBus.getDefault().post(Constant.CLOSE_SOUND_WAVE_TWO_HALL);
                    }
                    break;
                case R.id.sound_wave_three:
                    if(isChecked){
                        //FE0801030A16FF16  第三通道10分钟
                        EventBus.getDefault().post(Constant.OPEN_SOUND_WAVE_THREE_HALL + Constant.OPEN_SOUND_WAVE_TIME
                                + Constant.OPEN_SOUND_WAVE_LAST_THREE);
                    }else {
                        EventBus.getDefault().post(Constant.CLOSE_SOUND_WAVE_THREE_HALL);
                    }
                    break;
                case R.id.sound_wave_four:
                    if(isChecked){
                        //FE0801040A17FF16  第四通道10分钟
                        EventBus.getDefault().post(Constant.OPEN_SOUND_WAVE_FOUR_HALL + Constant.OPEN_SOUND_WAVE_TIME
                                + Constant.OPEN_SOUND_WAVE_LAST_FOUR);
                    }else {
                        EventBus.getDefault().post(Constant.CLOSE_SOUND_WAVE_FOUR_HALL);
                    }
                    break;

                    //香薰
                case R.id.sweet_one:
                    if(isChecked){
                        EventBus.getDefault().post(Constant.OPEN_SWEET_ONE_HALL + Constant.OPEN_SWEET_TIME +
                                Constant.SWEET_CODE_ONE + Constant.OPEN_SWEET_LAST);
                    }else {
                        EventBus.getDefault().post(Constant.CLOSE_SWEET_ONE_HALL);
                    }
                    break;
                case R.id.sweet_two:
                    if(isChecked){
                        EventBus.getDefault().post(Constant.OPEN_SWEET_TWO_HALL + Constant.OPEN_SWEET_TIME +
                                Constant.SWEET_CODE_TWO + Constant.OPEN_SWEET_LAST);
                    }else {
                        EventBus.getDefault().post(Constant.CLOSE_SWEET_TWO_HALL);
                    }
                    break;
                case R.id.sweet_three:
                    if(isChecked){
                        EventBus.getDefault().post(Constant.OPEN_SWEET_THREE_HALL + Constant.OPEN_SWEET_TIME +
                                Constant.SWEET_CODE_THREE + Constant.OPEN_SWEET_LAST);
                    }else {
                        EventBus.getDefault().post(Constant.CLOSE_SWEET_THREE_HALL);
                    }
                    break;
                case R.id.sweet_four:
                    if(isChecked){
                        EventBus.getDefault().post(Constant.OPEN_SWEET_FOUR_HALL + Constant.OPEN_SWEET_TIME +
                                Constant.SWEET_CODE_FOUR + Constant.OPEN_SWEET_LAST);
                    }else {
                        EventBus.getDefault().post(Constant.CLOSE_SWEET_FOUR_HALL);
                    }
                    break;
                    //呼叫
                case R.id.call:
                    if(isChecked){
                        EventBus.getDefault().post(Constant.CALL_ON);
                    }else {
                        EventBus.getDefault().post(Constant.CALL_OFF);
                    }
                    break;
                default:
                    break;
            }
    }




    @Override
    public void onDataReceived(DevicePacket packet) {
    /*    executorService.execute(new Runnable() {
            @Override
            public void run() {
                signalView.setDataEcg(packet);
            }
        });*/

       new Thread(new Runnable() {
            @Override
            public void run() {
                short[] ecgData = packet.secgdata;
                Log.d("TAG", Arrays.toString(ecgData));
                for (int i = 0; i < ecgData.length; i++) {
                    if (null != myEcgView) {
                        myEcgView.addOneData1((int) ecgData[i]);
                        myRespView.addOneData1((int) packet.irspData[i]);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshEcgData();

                                Log.d("fengshuai",packet.resp+"===="+packet.heartRate);
//                                kyc_heart_rate.setText("心率："+packet.heartRate+"次/分");
//                                kyc_resp_rate.setText("呼吸："+packet.resp+"次/分");

                            }
                        });
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDataReceived(BPDevicePacket packet) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                short[] ecgData = packet.getsEcgData();
                Log.d("TAG", Arrays.toString(ecgData));
                for (int i = 0; i < ecgData.length; i++) {
                    if (null != myEcgViewCM22) {
                        myEcgViewCM22.addOneData((int) ecgData[i]);
                        myPPGView.addOneData((int) packet.getsPpgData()[i]);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshEcgData();
                                int heart = packet.getHeartRate();
                                int szPress = packet.getsSzPressDataData();
                                int ssPress = packet.getsSsPressDataData();
                                if (heart != 1000) {
                                    kyc_heart_rate_cm22.setText("心率：" + heart + "次/分");
                                }
                                if (szPress != 1000 && ssPress != 1000) {
                                    kyc_press.setText("血压：" + ssPress + "/" + szPress + "mmHg");
                                }

                            }
                        });
                    }
                }
            }
        }).start();
    }

    @Override
    public void onDataReceived(byte[] packet) {

    }

    @Override
    public void onDataReceived(DevicePacket packet, int battery) {

    }

    @Override
    public void onDataReceived(EstimateRet ret) {

    }

    @Override
    public void onDeviceDisConnected() {

    }

    @Override
    public void onDeviceConnected(long startTime) {

    }

    @Override
    public void onStartToConnect() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        if(event instanceof Map){
            Map<String, Object> map = (Map<String, Object>) event;
            if (map != null) {
                if (map.containsKey(Constant.SPO2_DATA)) {
                    kyc_spo2_data.setText(map.get(Constant.SPO2_DATA) + "%SpO₂");
                }

                if (map.containsKey(Constant.IRT_DATA)) {
                    kyc_irt_data.setText(map.get(Constant.IRT_DATA) + "℃");
                }

                if (map.containsKey(Constant.BP_DATA)) {
                    kyc_bp_data.setText(map.get(Constant.BP_DATA) + "mmHg");
                }
                if (map.containsKey(Constant.BP_DATA_ERROR)) {
                    kyc_bp_data.setText("测量失败");
                }
            }
        }

    }



 /*   @Override
    protected void onPause() {
        super.onPause();
        BleManager.getInstance().disconnectAllDevice();
    }
*/
}
