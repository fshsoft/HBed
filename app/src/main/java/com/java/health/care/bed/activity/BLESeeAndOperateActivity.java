package com.java.health.care.bed.activity;

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
import com.java.health.care.bed.widget.EcgShowView;
import com.java.health.care.bed.widget.PPGShowView;
import com.java.health.care.bed.widget.RespShowView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;

/**
 * @author fsh
 * @date 2022/08/15 15:03
 * @Description 康养床ble设置
 */
public class BLESeeAndOperateActivity extends BaseActivity implements DataReceiver, CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.patient_view_ecg_cm19)
    EcgShowView ecgShowViewCM19;
    @BindView(R.id.patient_view_resp)
    RespShowView respShowView;
    @BindView(R.id.patient_view_ecg_cm22)
    EcgShowView ecgShowViewCM22;
    @BindView(R.id.patient_view_ppg)
    PPGShowView ppgShowView;

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

    @BindView(R.id.kyc_heart_rate_cm22)
    TextView kyc_heart_rate_cm22;
    @BindView(R.id.kyc_press)
    TextView kyc_press;

    private Timer timerCM19;
    private Timer timerCM22;

    private Queue<Integer> dataQueueEcgCM19 = new LinkedList<>();
    private Queue<Integer> dataQueueResp = new LinkedList<>();

    private Queue<Integer> dataQueueEcgCM22 = new LinkedList<>();
    private Queue<Integer> dataQueuePPG = new LinkedList<>();

    private int indexEcgCM19 = 0;
    private int indexResp = 0;
    private int[] shortsEcgCM19 = new int[5];
    private int[] shortsResp = new int[5];

    private int indexEcgCM22 = 0;
    private int indexPPG = 0;
    private int[] shortsEcgCM22 = new int[5];
    private int[] shortsPPG = new int[5];

    //判断是cm19设备 还是cm22设备
    private boolean flag ;
    //只启动一个线程
    public ExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ble_see_operate;
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
        timerCM19 = new Timer();
        timerCM22 = new Timer();
        //cm19
         timerCM19.schedule(new TimerTask() {
            @Override
            public void run() {
                //很重要，从队列里面取5个数据
                //取数据的计算方法：采样率为300，定时器17ms绘制一次，（300/1000）*17ms =5.1个数据

                for (int i = 0; i < 5; i++) {

                    Integer x = dataQueueEcgCM19.poll();

                    Integer y = dataQueueResp.poll();

                    if (x == null) {
                        continue;
                    }

                    if(y ==null){
                        continue;
                    }
                    shortsEcgCM19[i] = x;
                    shortsResp[i] =y;
                }


                if (indexEcgCM19 >= shortsEcgCM19.length) {
                    indexEcgCM19 = 0;
                }

                if(indexResp >=0){
                    indexResp = 0;
                }
                ecgShowViewCM19.showLine(shortsEcgCM19[indexEcgCM19] );
                respShowView.showLine(shortsResp[indexResp]);
                indexEcgCM19++;
                indexResp++;


            }
        }, 100, 16);


        //无创连续血压
        timerCM22.schedule(new TimerTask() {
            @Override
            public void run() {
                //很重要，从队列里面取5个数据
                //取数据的计算方法：采样率为200，定时器25ms绘制一次，（200/1000）*25ms =5个数据

                for (int i = 0; i < 5; i++) {

                    Integer x = dataQueueEcgCM22.poll();

                    Integer y = dataQueuePPG.poll();

                    if (x == null) {
                        continue;
                    }

                    if(y ==null){
                        continue;
                    }
                    shortsEcgCM22[i] = x;
                    shortsPPG[i] =y;
                }


                if (indexEcgCM22 >= shortsEcgCM22.length) {
                    indexEcgCM22 = 0;
                }

                if(indexPPG>=0){
                    indexPPG = 0;
                }
                ecgShowViewCM22.showLine(shortsEcgCM22[indexEcgCM22] );
                ppgShowView.showLine(shortsPPG[indexPPG]);
                indexEcgCM22++;
                indexPPG++;

            }
        }, 100, 25);

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
        //这个里面是CM19设备，心电设备（心电和呼吸）
        short[] ecg = packet.secgdata;
        int[] resp = packet.irspData;

        if (ecg.length != DevicePacket.ECG_IN_PACKET) {
            return;
        }

        for (int i = 0; i < 96; i++) {
            dataQueueEcgCM19.add((int) ecg[i]);
            dataQueueResp.add(resp[i]);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (packet.heartRate >0) {
                    kyc_heart_rate.setText("心率："+packet.heartRate);
                }else {
                    kyc_heart_rate.setText("心率："+"--");
                }

                if (packet.resp > 0) {
                    kyc_resp_rate.setText("呼吸："+packet.resp + "");
                }else {
                    kyc_resp_rate.setText("呼吸："+"--");
                }

            }
        });
    }

    @Override
    public void onDataReceived(BPDevicePacket packet) {
        //这个里面是CM22设备，无创连续血压（心电和PPG）
        short[] ecg = packet.getsEcgData();
        short[] ppg = packet.getsPpgData();
        if (ecg.length != DevicePacket.ECG_IN_PACKET) {
            return;
        }
        for (int i = 0; i < 96; i++) {
            dataQueueEcgCM22.add((int) ecg[i]);
            dataQueuePPG.add((int) ppg[i]);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (packet.getHeartRate() >0) {
                    kyc_heart_rate_cm22.setText("心率："+packet.getHeartRate());
                }else {
                    kyc_heart_rate_cm22.setText("心率："+"--");
                }

                if (packet.getsSsPressDataData() > 0 && packet.getsSzPressDataData()>0) {
                    kyc_press.setText("血压："+packet.getsSsPressDataData() + "/"+ packet.getsSzPressDataData());
                }else {
                    kyc_press.setText("血压："+"--/--");
                }

            }
        });

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
        if(timerCM19!=null){
            timerCM19.cancel();
        }
        if(timerCM22!=null){
            timerCM22.cancel();
        }
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


}
