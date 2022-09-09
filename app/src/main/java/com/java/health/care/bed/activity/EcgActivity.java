package com.java.health.care.bed.activity;

import android.util.Log;

import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.model.BPDevicePacket;
import com.java.health.care.bed.model.DataReceiver;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.model.EstimateRet;
import com.java.health.care.bed.widget.EcgView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author fsh
 * @date 2022/09/09 10:21
 * @Description
 */
public class EcgActivity extends BaseActivity implements DataReceiver {

    private List<Integer> dataList;

    private Queue<Integer> dataQueue;
    //只启动一个线程
    public ExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ecg;
    }

    @Override
    protected void initView() {
        DataTransmitter.getInstance().addDataReceiver(this);
    }

    @Override
    protected void initData() {
        simulator();
    }

    /**
     * 模拟心电发送，心电数据是一秒500个包，所以
     */
    private void simulator(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(EcgView.isRunning){
                    if(null!=dataQueue&&dataQueue.size() > 0){
                        EcgView.addEcgData0(dataQueue.poll());
//
                    }
                }
            }
        }, 0, 2);
    }

    @Override
    public void onDataReceived(DevicePacket packet) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                short[] ecgData = packet.secgdata;
                Log.d("ecg=====", Arrays.toString(ecgData));
                dataList = new ArrayList<>();
                for (short sh : ecgData){
                    if(sh>2500){
                        sh=2500;
                    }
                    dataList.add((int) sh +2500);
                }
                dataQueue = new LinkedList<>();
                dataQueue.addAll(dataList);


                Log.d("dataQueue====",dataQueue.size()+"");

            }
        });
    }

    @Override
    public void onDataReceived(BPDevicePacket packet) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                short[] ecgData = packet.getsEcgData();
                Log.d("ecg=====", Arrays.toString(ecgData));
                dataList = new ArrayList<>();
                for (short sh : ecgData){
                    dataList.add((int) sh +2500);
                }
                dataQueue = new LinkedList<>();
                dataQueue.addAll(dataList);


                Log.d("dataQueue====",dataQueue.size()+"");

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
}
