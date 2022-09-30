package com.java.health.care.bed.activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ZipUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.base.BaseApplication;
import com.java.health.care.bed.bean.Param;
import com.java.health.care.bed.bean.UnFinishedPres;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.model.BPDevicePacket;
import com.java.health.care.bed.model.DataReceiver;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.model.EstimateRet;
import com.java.health.care.bed.model.Music;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.presenter.MainPresenter;
import com.java.health.care.bed.service.DataReaderService;
import com.java.health.care.bed.service.WebSocketService;
import com.java.health.care.bed.util.ImageLoadUtils;
import com.java.health.care.bed.widget.EcgShowView;
import com.java.health.care.bed.widget.MyEcgView;
import com.java.health.care.bed.widget.RespShowView;
import com.java.health.care.bed.widget.TagValueTextView;
import com.plattysoft.leonids.ParticleSystem;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
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
 * @date 2022/08/11 09:21
 * @Description 训练界面
 */
public class DrillActivity extends BaseActivity implements DataReceiver, MainContract.View {
    @BindView(R.id.drill_user)
    TextView drill_user;
    @BindView(R.id.drill_id)
    TextView drill_id;
    @BindView(R.id.drill_hxl)
    TextView drill_hxl;
    @BindView(R.id.drill_hxb)
    TextView drill_hxb;
    @BindView(R.id.drill_ll)
    LinearLayout drill_ll;

    public static final String TAG = DrillActivity.class.getSimpleName();
    private WebSocketService webSocketService;
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
    private MyCountDownTimer mc;
    //获取到数据，通知开始倒计时
    private boolean isGotData = false;

    private MainPresenter presenter;

    private List<Param> paramList;

    private int patientId;

    private int preId;

    private String preType;

    private int mMusicDuration;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_assess;
    }

    @Override
    protected void initView() {
        addConnectDeviceView();
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



        presenter = new MainPresenter(this, this);

        //通知DataReaderService现在是搞的生命体征 主要是区分cm19是做生命体征true  还是心肺谐振评估训练false
        EventBus.getDefault().post(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //评估里面 有四个阶段，每个阶段类型不一样，value:自由呼吸-1， 6次/分钟   9次/分钟  12次/分钟
        UnFinishedPres unFinishedPres = (UnFinishedPres) getIntent().getParcelableExtra(TAG);

        paramList = unFinishedPres.getParam();

        preId = unFinishedPres.getPreId();

        preType = unFinishedPres.getPreType();
        // 获取评估训练时长
//        mMusicDuration = Integer.valueOf("10") * 60 * 1000;
        mMusicDuration = unFinishedPres.getDuration();

        Log.d(TAG,"preId:"+preId+"==preType:"+preType+"==mMusicDuration:"+mMusicDuration);
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
        drill_ll.removeAllViews();
        drill_ll.addView(connectDeviceView);
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
                stopFlag = false;
                deviceListConnect.add(bleDevice);
                EventBus.getDefault().post(deviceListConnect);



                //把获取到的时间，进行展示，倒计时展示
                String timeStr = millisUntilFinishedToMin(Integer.valueOf(mMusicDuration)  * 1000);
                breatheTime.setText(timeStr);

                //开始播放背景音乐
                if (bgMediaPlayer != null) {
                    bgMediaPlayer.release();
                }
                bgMediaPlayer = MediaPlayer.create(DrillActivity.this,
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

                //开启倒计时
                handler.sendEmptyMessageDelayed(3333,1000);


                //设备连接之后，
                //训练，只有一个阶段，需要知道 呼吸频率，呼气吸气比
                /**
                 * 必须要通过接口得知，类型和时长，总时长
                 * resprate, 呼吸频率
                 * respratio,呼气吸气比
                 */
                String resprateValue = null;
                String respratioValue = null;
                for(Param param : paramList) {
                    if(param.getKey().equals("resprate")){
                        //赋值呼吸频率
                        resprateValue = param.getValue();
                    }else if(param.getKey().equals("respratio")){
                        respratioValue = param.getValue();
                    }
                }

                //呼吸频率
                if(null!=resprateValue){
                    initRate = Integer.parseInt(resprateValue);
                }

                //计算吸气呼吸比initBreathRatio的值，默认是1:1.5  initBreathRatio为0.6f
                //吸气：呼气 = 1：hu 这种格式
                if(null!= respratioValue){
                    String[] resp =  respratioValue.split(":");
                    float xi = Float.parseFloat(resp[0]);
                    float hu = Float.parseFloat(resp[1]);

                    //xi 都是1
                    initBreathRatio =  (hu/xi)/(1+hu/xi);
                }

                handler.sendEmptyMessage(4444);
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
        tvBreathScore = breatheView.findViewById(R.id.patient_view_tv_score);

        //添加之前必须先移除里面的所有view
        drill_ll.removeAllViews();
        drill_ll.addView(breatheView);
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
        closeAll();
    }
    /**
     * 数据接收
     *
     * @param packet
     */
    @Override
    public void onDataReceived(DevicePacket packet) {

        if(!isGotData){
            handler.sendEmptyMessage(3);
            isGotData = true;
        }

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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if(packet.scoreNew>0){
                    tvBreathScore.setText(packet.scoreNew+"");
                }

            }
        });
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

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what ==1111 ){
//                showParticleAnim();
                breatheStatus.setText("呼");
                animation = AnimationUtils.loadAnimation(DrillActivity.this, R.anim.anim_set);

                animation.setDuration((int) (60 / initRate * initBreathRatio * 1000));

                LinearInterpolator lin = new LinearInterpolator();
                animation.setInterpolator(lin);
                ivBreathe.startAnimation(animation);//放大放小
                if (animation != null && null != ivBreathe) {
                    ivBreathe.startAnimation(animation);
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                    }
                    mediaPlayer = MediaPlayer.create(DrillActivity.this,
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
                        handler.sendEmptyMessageDelayed(2222,
                                (int) (60 / initRate * initBreathRatio * 1000));
                    }

                }
            }else if(msg.what ==2222){
                breatheStatus.setText("吸");
                animation = AnimationUtils.loadAnimation(DrillActivity.this, R.anim.anim_set_back);

                animation.setDuration((int) (60 / initRate * (1 - initBreathRatio) * 1000));
                LinearInterpolator lin = new LinearInterpolator();
                animation.setInterpolator(lin);
                ivBreathe.startAnimation(animation); //放大放小
                if (animation != null && ivBreathe != null) {
                    ivBreathe.startAnimation(animation);
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                    }
                    mediaPlayer = MediaPlayer.create(DrillActivity.this,
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
                        handler.sendEmptyMessageDelayed(1111, (int) (60 / initRate * (1 - initBreathRatio) * 1000));

                    }
                }
            }else if(msg.what==3333){
                //开启倒计时
                mc = new MyCountDownTimer(mMusicDuration*1000, 1000);
                mc.start();


            }


            else if (msg.what == 4444) {
                perMediaPlayer = MediaPlayer.create(DrillActivity.this, R.raw.inhaleexhaleesti);
                perMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer media) {
                        media.reset();
                        media.release();
                        handler.sendEmptyMessage(1111);
                    }
                });
                perMediaPlayer.start();
            }
        }
    };

    /**
     * 倒计时使用
     */
    private String millisUntilFinishedToMin(long millisUntilFinished) {
        StringBuffer sb = new StringBuffer();
        int min = (int) (millisUntilFinished / 60 / 1000);
        int s = (int) ((millisUntilFinished % (60 * 1000)) / 1000);
//        MyLogUtils.d(TAG,"取余剩下的秒数:"+s);
        if (min > 0) {
            if (min < 10) {
                sb.append("0");
            }
            sb.append(min);
            sb.append(":");
        } else {
            sb.append("00:");
        }
        if (s < 10) {
            sb.append("0");
        }
        sb.append(s);
        return sb.toString();
    }




    /**
     * 测评结束的逻辑，时间倒计时
     */
    public class MyCountDownTimer extends CountDownTimer {


        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            //参数：总时长，间隔时长
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            String timeStr = millisUntilFinishedToMin(millisUntilFinished);
            Log.d(TAG, "MyCountDownTimer====" + timeStr);
            breatheTime.setText(timeStr);
        }

        @Override
        public void onFinish() {
            //倒计时完成后，处理事物

            closeAll();

            //todo 调用接口，上传文件

            //文件上传
            presenter.uploadFile(zipFiles(),"file_uploadReportLfs", String.valueOf(patientId), String.valueOf(preId),preType);

        }
    }

    //压缩文件
    private File zipFiles(){

        String dateNowStr = SPUtils.getInstance().getString(SP.KEY_ECG_FILE_TIME);
        patientId = SPUtils.getInstance().getInt(SP.PATIENT_ID);
        //原保存的文件路径
        String src = Environment.getExternalStorageDirectory().getPath()+"/HBed/data/"+patientId+"-"+dateNowStr;

        //将要压缩的文件zip
        String zip = Environment.getExternalStorageDirectory().getPath() + "/HBed/zipData/"+patientId+"-"+dateNowStr + ".zip";

        //判断zip文件是否存在并创建文件
        FileUtils.createOrExistsFile(zip);
        //压缩文件
        try {
            ZipUtils.zipFile(src,zip);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取压缩文件
        File file = FileUtils.getFileByPath(zip);
        return file;
    }


    //释放音频，移除handle中message
    private void closeAll(){
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        if (perMediaPlayer != null) {
            perMediaPlayer.release();
        }

        if (bgMediaPlayer != null) {
            bgMediaPlayer.release();
        }

        if (null != mc) {
            mc.cancel();
            mc = null;
        }

        handler.removeMessages(1111);
        handler.removeMessages(2222);
        handler.removeMessages(3333);
        handler.removeMessages(4444);


        //断开cm19蓝牙
        if(deviceListConnect!=null){
            for (BleDevice bleDevice: deviceListConnect) {
                BleManager.getInstance().disconnect(bleDevice);
            }
        }
    }


    @Override
    public void setCode(String code) {
        if(code.equals("200")){
            goActivity(PrescriptionActivity.class);

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
