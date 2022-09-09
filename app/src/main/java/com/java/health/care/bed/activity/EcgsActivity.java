package com.java.health.care.bed.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.model.BPDevicePacket;
import com.java.health.care.bed.model.DataReceiver;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.model.EstimateRet;
import com.java.health.care.bed.widget.WaveUtil;
import com.java.health.care.bed.widget.WaveView;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author fsh
 * @date 2022/09/09 14:53
 * @Description
 */
public class EcgsActivity extends AppCompatActivity implements DataReceiver {
    private WaveUtil waveUtil1, waveUtil2;

    private WaveView wave_view1, wave_view2;

    private SeekBar seekBar;

//    float data = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecgs);
        DataTransmitter.getInstance().addDataReceiver(this);
        waveUtil1 = new WaveUtil();
        waveUtil2 = new WaveUtil();

        wave_view1 = findViewById(R.id.wave_view1);
        wave_view2 = findViewById(R.id.wave_view2);
        seekBar = findViewById(R.id.seek_bar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.i("seek_bar progress is", i + "");
                if (i == 0)
                    return;
                wave_view1.setWaveLineWidth(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        waveUtil1.showWaveDatas(1, wave_view1);
        waveUtil2.showWaveDatas(1, wave_view2);
//        findViewById(R.id.tv_wave1).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                data = new Random().nextFloat()*(20f)-10f;
//                Log.i("data is --------------", data + "");
//                wave_view1.showLine(data);//取得是-10到10间的浮点数
//            }
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        waveUtil1.stop();
        waveUtil2.stop();
    }
    private Queue<Short> bufferedEcg = new LinkedList<>();
    private Queue<Short> bufferedEcgStore = new LinkedList<>();
    @Override
    public void onDataReceived(DevicePacket packet) {

        waveUtil1.showWaveData(packet.secgdata,wave_view1);


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
