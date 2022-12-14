package com.java.health.care.bed.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import android.view.KeyEvent;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    private ProgressDialog progressDialog;
    public static final String TAG = DrillActivity.class.getSimpleName();
    private WebSocketService webSocketService;
    private View connectDeviceView;
    private TextView tvConnectDevice,patient_time;
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
    private int[] shortsEcgCM19 = new int[3];
    private int[] shortsResp = new int[3];

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

    private int bunkId;

    private int regionId;

    private int patientId;

    private int preId;

    private String preType;

    private int hospitalId;

    private int doctorId;

    private int mMusicDuration;

    private String src,zip;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_drill;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        DataTransmitter.getInstance().addDataReceiver(this);
        EventBus.getDefault().register(this);
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

    //呼叫
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        if(event instanceof Integer){
            int num = (int) event;
            bunkId = SPUtils.getInstance().getInt(SP.BUNK_ID);
            regionId = SPUtils.getInstance().getInt(SP.REGION_ID);
            presenter.sendMessage(regionId,bunkId,num,patientId);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        patientId = SPUtils.getInstance().getInt(SP.PATIENT_ID);
        //评估里面 有四个阶段，每个阶段类型不一样，value:自由呼吸-1， 6次/分钟   9次/分钟  12次/分钟
        UnFinishedPres unFinishedPres = (UnFinishedPres) getIntent().getParcelableExtra(TAG);

        paramList = unFinishedPres.getParam();

        preId = unFinishedPres.getPreId();

        doctorId = unFinishedPres.getDoctorId();

        preType = unFinishedPres.getPreType();

        // 获取评估训练时长
        mMusicDuration = unFinishedPres.getDuration();

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

        Log.d(TAG,"preId:"+preId+"==preType:"+preType+"==mMusicDuration:"+mMusicDuration);

        drill_id.setText("训练ID："+preId);
        drill_user.setText("姓名："+SPUtils.getInstance().getString(SP.PATIENT_NAME));
        drill_hxl.setText("呼吸率："+resprateValue+"次/分钟");
        drill_hxb.setText("吸气呼气比："+respratioValue);

        addConnectDeviceView();
    }


    /**
     * 开始前的状态
     */
    private void addConnectDeviceView() {
        connectDeviceView = LayoutInflater.from(this).inflate(R.layout.patient_view_access_init, null, false);
        ImageView imageView = connectDeviceView.findViewById(R.id.patient_breathe_iv);
        tvConnectDevice = connectDeviceView.findViewById(R.id.patient_access_connect_device);
        patient_time = connectDeviceView.findViewById(R.id.patient_time);
        patient_time.setText(mMusicDuration/60+"");
        ImageLoadUtils.loadGifToImg(this, R.drawable.a3699, imageView);
        tvConnectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(DrillActivity.this);
                progressDialog.show();
                //开始，连接cm19设备
                scanBle();


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
                if(success==false){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scanBle();
                        }
                    },5000);
                }
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
                progressDialog.dismiss();
                Log.d(TAG, "onConnectFail:exception:" + exception.toString());
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();
                addBreathView();
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
                showDialog();
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


    @OnClick(R.id.back)
    public void onClickBack(){
        showDialog();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            showDialog();
        }

        return false;
    }

    //弹窗提示
    private void showDialog(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("你确定要中断此次训练吗？")
                .positiveText("确定")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG,"MaterialDialog确定");
                        //调用接口，上传文件
                        Date d = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String endTime = sdf.format(d);
                        presenter.uploadFile(zipFiles(endTime), "file_uploadReportLfs", String.valueOf(patientId), String.valueOf(preId), preType);


                    }
                })
                .build();


        if (dialog.getTitleView() != null) {
            dialog.getTitleView().setTextSize(25);
        }

        if (dialog.getActionButton(DialogAction.POSITIVE) != null) {
            dialog.getActionButton(DialogAction.POSITIVE).setTextSize(25);
        }

        if(dialog.getActionButton(DialogAction.NEGATIVE)!=null){
            dialog.getActionButton(DialogAction.NEGATIVE).setTextSize(25);
        }

        dialog.show();
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

                for (int i = 0; i < 3; i++) {

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
        }, 100, 10);

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
                    tvBreathScore.setText((int)packet.scoreNew+"");
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
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String endTime = sdf.format(d);
            //倒计时完成后，处理事物

//            closeAll();

            //todo 调用接口，上传文件

            //文件上传
            //原接口
//            presenter.uploadFileCPR(zipFiles());
            //现接口
            presenter.uploadFile(zipFiles(endTime), "file_uploadReportLfs", String.valueOf(patientId), String.valueOf(preId), preType);


        }
    }

    //压缩文件
    private File zipFiles(String endTime){

        String startTime = SPUtils.getInstance().getString(SP.KEY_ECG_FILE_TIME);
        patientId = SPUtils.getInstance().getInt(SP.PATIENT_ID);
        hospitalId = SPUtils.getInstance().getInt(SP.HOSPITAL_ID);
        //原保存的文件路径
        src = Environment.getExternalStorageDirectory().getPath()+"/HBed/data/"+patientId+"-"+startTime;
        File file1 = new File(src+"/"+"ecgData.ecg");
        File file2 = new File(src+"/"+"respData.resp");
        File file3 = new File(src+"/"+"scoreData.score");

        File file1Ecg = new File(src+"/"+startTime+".ecg");
        file1.renameTo(file1Ecg);

        File file2Resp = new File(src+"/"+endTime+".resp");
        file2.renameTo(file2Resp);

        List<File> fileList = new ArrayList<>(3);
        fileList.add(file1Ecg);
        fileList.add(file2Resp);
        fileList.add(file3);
        //将要压缩的文件zip 文件名称依次为：hospitalId_doctorId_patientId_startTime_preId_reportType reportType默认写2
        //自主神经是1，心肺谐振是2

        //将要压缩的文件zip 文件名称依次为：hospitalId_doctorId_patientId_startTime_preId_reportType reportType默认写2
        zip = Environment.getExternalStorageDirectory().getPath() +
                "/HBed/zipData/"+ patientId +  "_" + preId + "_" + startTime +"_" +2 + ".zip";
//                "/HBed/zipData/"+hospitalId + "_" + doctorId + "_" + patientId + "_" + startTime + "_" + preId + "_" + 2+"_CPR_" + ".zip";

        //判断zip文件是否存在并创建文件
        FileUtils.createOrExistsFile(zip);
        //压缩文件
        try {
//            ZipUtils.zipFile(src,zip);

            ZipUtils.zipFiles(fileList,FileUtils.getFileByPath(zip));
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
    }

    @Override
    public void setMsg(String msg) {

    }

    @Override
    public void setInfo(String msg) {

    }

    @Override
    public void setObj(Object obj) {
     /*   //完成处方接口
        boolean isSuccess = (boolean) obj;
        if(isSuccess ==true){
            FileUtils.delete(zip);
            goActivity(PrescriptionActivity.class);
        }*/
        //文件上传成功
        boolean isSuccess = (boolean) obj;
        if(isSuccess==true){
            showToast("上传文件成功");

            finish();
        }
    }

    @Override
    public void setData(Object obj) {
       /* String code = (String) obj;
        if(code.equals("000000")){
            presenter.presFinish(patientId,preId,preType);
        }*/
    }
}
