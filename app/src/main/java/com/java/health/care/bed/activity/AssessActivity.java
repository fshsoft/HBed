package com.java.health.care.bed.activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.SPUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.base.BaseApplication;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.model.BPDevicePacket;
import com.java.health.care.bed.model.Music;
import com.java.health.care.bed.service.DataReaderService;
import com.java.health.care.bed.model.DataReceiver;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.model.EstimateRet;
import com.java.health.care.bed.service.WebSocketService;
import com.java.health.care.bed.util.ImageLoadUtils;
import com.java.health.care.bed.widget.EcgShowView;
import com.java.health.care.bed.widget.MyEcgView;
import com.java.health.care.bed.widget.RespShowView;
import com.java.health.care.bed.widget.TagValueTextView;
import com.plattysoft.leonids.ParticleSystem;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author fsh
 * @date 2022/08/11 09:20
 * @Description 评估界面
 */
public class AssessActivity extends BaseActivity implements DataReceiver {

    @BindView(R.id.assess_user)
    TextView assess_user;
    @BindView(R.id.assess_id)
    TextView assess_id;
    @BindView(R.id.assess_hxl)
    TextView assess_hxl;
    @BindView(R.id.assess_hxb)
    TextView assess_hxb;
    @BindView(R.id.assess_ll)
    LinearLayout assess_ll;
    private WebSocketService webSocketService;
    private static final String TAG = AssessActivity.class.getSimpleName();
    private View connectDeviceView;
    private TextView tvConnectDevice;
    private View breatheView;
    private TextView breatheType;//常规呼吸
    private TextView breatheStatus;//呼或者是吸
    private TextView breatheTime;//时间
    private TextView breatheDisconnect;//呼吸中断
    private TextView breatheRate;//呼吸频率
    private TextView tvHeartRate;//心率
    private TextView tvBreathScore;//和谐指数
    private ImageView ivBreathe;
    private LinearLayout testRlBreath;


    private EcgShowView ecgShowViewCM19;
    private RespShowView respShowView;

    private Timer timerCM19;

    private Queue<Integer> dataQueueEcgCM19 = new LinkedList<>();
    private Queue<Integer> dataQueueResp = new LinkedList<>();

    private int indexEcgCM19 = 0;
    private int indexResp = 0;
    private int[] shortsEcgCM19 = new int[5];
    private int[] shortsResp = new int[5];

    private String bleDeviceMac;
    List<BleDevice> deviceListConnect = new ArrayList<>();
    private Animation animation;
    private float initBreathRatio = 0.6f;
    private float initRate = 10;
    DecimalFormat df = new DecimalFormat("0.00");//格式化小数
    private MediaPlayer mediaPlayer;
    private MediaPlayer bgMediaPlayer;
    private MediaPlayer perMediaPlayer;
    private boolean stopFlag = true;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_assess;
    }

    @Override
    protected void initView() {
        addConnectDeviceView();
        goService(DataReaderService.class);
    }

    @Override
    protected void initData() {
        DataTransmitter.getInstance().addDataReceiver(this);
        bleDeviceMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_CM19_MAC);
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
        bindService(new Intent(this, WebSocketService.class), serviceConnection, BIND_AUTO_CREATE);

        //开始播放背景音乐
        if (bgMediaPlayer != null) {
            bgMediaPlayer.release();
        }
        bgMediaPlayer = MediaPlayer.create(AssessActivity.this,
                R.raw.def_comfort_1);
        bgMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer media) {
//                media.reset();
//                media.release();
                media.start();
            }
        });
        bgMediaPlayer.start();




    }

    @OnClick(R.id.hu)
    public void hu(){
        handler.sendEmptyMessage(1);
    }

    @OnClick(R.id.xi)
    public void xi(){
        handler.sendEmptyMessage(2);
    }

    /**
     * 开始前的状态
     */
    private void addConnectDeviceView() {
        connectDeviceView = LayoutInflater.from(this).inflate(R.layout.patient_view_access_init, null, false);
        ImageView imageView = connectDeviceView.findViewById(R.id.patient_breathe_iv);
        tvConnectDevice = connectDeviceView.findViewById(R.id.patient_access_connect_device);

        ImageLoadUtils.loadGifToImg(this, R.drawable.a3699, imageView);
        tvConnectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始，连接cm19设备
//
                scanBle();
                addBreathView();

            }
        });
        assess_ll.removeAllViews();
        assess_ll.addView(connectDeviceView);
    }

    private void scanBle() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {


            }

            @Override
            public void onScanStarted(boolean success) {
                Log.d(TAG, "bleDeviceMac:success:" + success);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {

                if (bleDevice.getMac().equals(bleDeviceMac)) {
                    connectBle(bleDevice);
                }
            }
        });
    }

    private void connectBle(BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d(TAG, "onStartConnect:");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                Log.d(TAG, "onConnectFail:exception:" + exception.toString());
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onConnectSuccess:status:" + status);
                //蓝牙设备CM19连接成功

                deviceListConnect.add(bleDevice);
                EventBus.getDefault().post(deviceListConnect);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onDisConnected:status:" + status);
            }
        });

    }

    /**
     * 蓝牙设备连接成功之后，开始后的状态
     */
    private void addBreathView() {
        breatheView = LayoutInflater.from(this).inflate(R.layout.patient_view_breath, null, false);
        testRlBreath = breatheView.findViewById(R.id.patient_ll_disconnect);
        ivBreathe = breatheView.findViewById(R.id.patient_view_breathe);
        ecgShowViewCM19 = breatheView.findViewById(R.id.patient_view_signal);
        respShowView = breatheView.findViewById(R.id.patient_view_resp);

        ImageLoadUtils.loadGifToImg(this, R.drawable.a3699, ivBreathe);
        breatheType = breatheView.findViewById(R.id.patient_view_breathe_type);
        breatheStatus = breatheView.findViewById(R.id.patient_view_breathe_status);
        breatheTime = breatheView.findViewById(R.id.patient_view_time);
        breatheDisconnect = breatheView.findViewById(R.id.patient_view_disconnect);
        breatheDisconnect.setText("中断");
        breatheDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //中断评估
            }
        });

        //添加绘制心电图和呼吸图
        drawDataTimer();

        tvHeartRate = breatheView.findViewById(R.id.patient_view_tv_hart_rate);
        tvBreathScore = breatheView.findViewById(R.id.patient_view_tv_breathe_score);

        //添加之前必须先移除里面的所有view
        assess_ll.removeAllViews();
        assess_ll.addView(breatheView);
    }

    //定时器，画图
    private void drawDataTimer(){
        timerCM19 = new Timer();

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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        if (perMediaPlayer != null) {
            perMediaPlayer.release();
        }

        if (bgMediaPlayer != null) {
            bgMediaPlayer.release();
        }
    }
    /**
     * 数据接收
     *
     * @param packet
     */
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
                    tvHeartRate.setText(packet.heartRate+"");
                }

            }
        });
    }

    @Override
    public void onDataReceived(BPDevicePacket packet) {

    }

    @Override
    public void onDataReceived(byte[] packet) {
        webSocketService.send(packet);
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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            webSocketService = ((WebSocketService.LocalBinder) service).getService();
            webSocketService.setWebSocketCallback(webSocketCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            webSocketService = null;
        }
    };
    private WebSocketService.WebSocketCallback webSocketCallback = new WebSocketService.WebSocketCallback() {
        @Override
        public void onMessage(final String text) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                tvMessage.setText(text);
                    Log.d("WebSocketService====",text);
                }
            });
        }

        @Override
        public void onOpen() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                tvMessage.setText("onOpen");
                    Log.d("WebSocketService====","onOpen=====");
                }
            });
        }

        @Override
        public void onClosed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                tvMessage.setText("onClosed");
                    Log.d("WebSocketService====","onClosed====");
                }
            });
        }
    };

    /**
     * 发射粒子动画
     */

    private void showParticleAnim() {
        ParticleSystem particleSystem = new ParticleSystem(this, 120, ContextCompat.getDrawable(this, R.drawable.circle), 8000);
        particleSystem.setSpeedModuleAndAngleRange(0f, 0.05f, 0, 360)
                .setRotationSpeed(360)//设置旋转
                .setAcceleration(0.00001f, 360)//设置加速度
                .setScaleRange(0.0f, 1.5f)//设置缩放范围
                .setFadeOut(5000)
//                .emit(testCenterContentLl,30);//从一个特定的角度开始发射粒子。如果在某一时刻这个数字超过了create上可用粒子的数量不会产生新的粒子
//                .emitWithGravity(testRlBreath, Gravity.CENTER,30);//此方法会自动循环播放粒子动画
                .oneShot(testRlBreath, 50);//此方法只会播放一次
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what ==1 ){
//                showParticleAnim();
                breatheStatus.setText("呼");
                animation = AnimationUtils.loadAnimation(AssessActivity.this, R.anim.anim_set);

                animation.setDuration((int) (60 / initRate * initBreathRatio * 1000));

                LinearInterpolator lin = new LinearInterpolator();
                animation.setInterpolator(lin);
                ivBreathe.startAnimation(animation);//放大放小
                if (animation != null && null != ivBreathe) {
                    ivBreathe.startAnimation(animation);
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                    }
                    mediaPlayer = MediaPlayer.create(AssessActivity.this,
                            R.raw.exhale);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer media) {
                            media.reset();
                            media.release();
                        }
                    });
                    mediaPlayer.start();

                    if (!stopFlag) {
                        handler.sendEmptyMessageDelayed(2,
                                (int) (60 / initRate * initBreathRatio * 1000));
                    }

                }
            }else if(msg.what ==2){
                breatheStatus.setText("吸");
                animation = AnimationUtils.loadAnimation(AssessActivity.this, R.anim.anim_set_back);

                animation.setDuration((int) (60 / initRate * (1 - initBreathRatio) * 1000));
                LinearInterpolator lin = new LinearInterpolator();
                animation.setInterpolator(lin);
                ivBreathe.startAnimation(animation); //放大放小
                if (animation != null && ivBreathe != null) {
                    ivBreathe.startAnimation(animation);
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                    }
                    mediaPlayer = MediaPlayer.create(AssessActivity.this,
                            R.raw.inhale);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer media) {
                            media.reset();
                            media.release();
                        }
                    });
                    mediaPlayer.start();

                    if (!stopFlag) {
                        handler.sendEmptyMessageDelayed(1, (int) (60 / initRate * (1 - initBreathRatio) * 1000));

                    }
                }
            }else if (msg.what == 4) {
                perMediaPlayer = MediaPlayer.create(AssessActivity.this, R.raw.inhaleexhaleesti);
                perMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                            @Override
                            public void onCompletion(MediaPlayer media) {
                                media.reset();
                                media.release();
                            }
                        });
                perMediaPlayer.start();
            }
        }
    };



}
