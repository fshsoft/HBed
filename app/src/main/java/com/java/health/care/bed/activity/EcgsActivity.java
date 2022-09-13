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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author fsh
 * @date 2022/09/09 14:53
 * @Description
 */
public class EcgsActivity extends AppCompatActivity implements DataReceiver {
    private Queue<Short> dataQueue = new LinkedList<>();

    EcgShowView ecgView;

    private List<Short> list  = new ArrayList<>();

    private int index = 0 ;

    private short[] shorts = new short[15];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgs);
        DataTransmitter.getInstance().addDataReceiver(this);
        ecgView = findViewById(R.id.patient_view_show);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(dataQueue==null ||dataQueue.size()<32){
                    return;
                }
                //很重要，从队列里面取15个数据
                //取数据的计算方法：采样率为300，定时器50ms绘制一次，（300/1000）*50 =15ms
                for(int i=0;i<15;i++){

                    shorts[i] = dataQueue.poll();
                }


                if(index>=shorts.length){
                    index = 0 ;
                }

                ecgView.showLine((shorts[index]));
                index++;



            }
        },1000,50);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onDataReceived(DevicePacket packet) {

        short[] sh = packet.secgdata;
        Log.d("aaron====5555",Arrays.toString(sh));
        if(sh.length != DevicePacket.ECG_IN_PACKET){
            Log.d("aaron====5555","return");
            return;
        }
        for(int i=0;i<3;i++){
            for (int j = i * 32; j < (i + 1) * 32; j++) {
                dataQueue.add(sh[j]);
            }
        }


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
