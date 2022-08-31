package com.java.health.care.bed.activity;

import android.util.Log;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.model.BPDevicePacket;
import com.java.health.care.bed.model.DataReceiver;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.model.EstimateRet;

import java.util.Arrays;


/**
 * @author fsh
 * @date 2022/08/03 14:40
 * @Description  生命体征
 */
public class VitalSignsActivity extends BaseActivity implements DataReceiver {

    public static final String TAG = VitalSignsActivity.class.getSimpleName();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_vitalsigns;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void initData() {
        DataTransmitter.getInstance().addDataReceiver(VitalSignsActivity.this);
    }


    /**
     * 以下为数据接收器
     * @param packet
     */
    @Override
    public void onDataReceived(DevicePacket packet) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                short[] ecgData = packet.secgdata;
                Log.d(TAG, Arrays.toString(ecgData));
            }
        }).start();
    }

    @Override
    public void onDataReceived(BPDevicePacket packet) {

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
}
