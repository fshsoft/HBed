package com.java.health.care.bed.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.java.health.care.bed.R;
import com.java.health.care.bed.model.BPDevicePacket;
import com.java.health.care.bed.model.DataReceiver;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.model.EstimateRet;
import com.java.health.care.bed.widget.EcgShowView;
import com.java.health.care.bed.widget.RespShowView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author fsh
 * @date 2022/09/09 14:53
 * @Description
 */
public class EcgsActivity extends AppCompatActivity implements DataReceiver {
    private Queue<Integer> dataQueue = new LinkedList<>();
    private Queue<Integer> dataQueueResp = new LinkedList<>();

    private Timer timer;
    EcgShowView ecgView;
    RespShowView respView;

    private int index = 0 ;
    private int indexResp = 0;

    private int[] shorts = new int[5];
    private int[] shortsResp = new int[15];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgs);
        DataTransmitter.getInstance().addDataReceiver(this);
        ecgView = findViewById(R.id.patient_view_show);
        respView = findViewById(R.id.patient_view_show_resp);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //很重要，从队列里面取15个数据
                //取数据的计算方法：采样率为300，定时器50ms绘制一次，（320/1000）*50ms =16

                for(int i=0;i<5;i++){

                    Integer x = dataQueue.poll();
                    if(x==null){
                        continue;
                    }
                    shorts[i] = x;
                }


                    if(index>=shorts.length){
                        index = 0 ;
                    }

                    ecgView.showLine((shorts[index]));
                    Log.d("ecgView=======",shorts[index]+"");
                    index++;


            }
        },100,20);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Override
    public void onDataReceived(DevicePacket packet) {

        short[] ecg = packet.secgdata;
//        int[] resp = packet.irspData;



            if(ecg.length != DevicePacket.ECG_IN_PACKET){
                return;
            }

            for(int i=0;i<96;i++){
                dataQueue.add((int) ecg[i]);
            }

    }


    @Override
    public void onDataReceived(BPDevicePacket packet) {
        short[] ecg = packet.getsEcgData();
//        int[] resp = packet.irspData;


        if(ecg.length != DevicePacket.ECG_IN_PACKET){
            return;
        }

        for(int i=0;i<96;i++){
            dataQueue.add((int) ecg[i]);
        }
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
