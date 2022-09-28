package com.java.health.care.bed.activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

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
import com.java.health.care.bed.bean.Param;
import com.java.health.care.bed.bean.UnFinishedPres;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.model.BPDevicePacket;
import com.java.health.care.bed.model.DataReceiver;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.model.EstimateRet;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.presenter.MainPresenter;
import com.java.health.care.bed.service.DataReaderService;
import com.java.health.care.bed.service.WebSocketService;
import com.java.health.care.bed.widget.EcgCM22ShowView;
import com.java.health.care.bed.widget.EcgShowView;
import com.java.health.care.bed.widget.PPGShowView;
import com.java.health.care.bed.widget.RespShowView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * @author fsh
 * @date 2022/08/03 14:40
 * @Description 生命体征  蓝牙 缺少断开重连机制
 */
public class VitalSignsActivity extends BaseActivity implements DataReceiver, MainContract.View {
    private WebSocketService webSocketService;
    private String bleDeviceCm22Mac;
    private String bleDeviceCm19Mac;
    private String bleDeviceSpO2Mac;
    private String bleDeviceBPMac;
    private String bleDeviceTempMac;
    private String bleDeviceKYCMac;
    List<BleDevice> deviceListConnect = new ArrayList<>();
    public static final String TAG = VitalSignsActivity.class.getSimpleName();

    //1、要根据处方来判断显示哪些ble设备，2、根据蓝牙连接与否，来对这些设备颜色变更。
    @BindView(R.id.vital_ble_cm22)
    TextView bleCM22;//无创连续血压蓝牙
    @BindView(R.id.vital_ble_cm19)
    TextView bleCM19;
    @BindView(R.id.vital_ble_spo2)
    TextView bleSpo2;
    @BindView(R.id.vital_ble_bp)
    TextView bleBP;
    @BindView(R.id.vital_ble_temp)
    TextView bleTemp;

    @BindView(R.id.vital_bp)
    TextView bpText;
    @BindView(R.id.vital_heart_rate)
    TextView heartRateText;
    @BindView(R.id.vital_spo2)
    TextView spo2Text;
    @BindView(R.id.vital_resp)
    TextView respText;
    @BindView(R.id.vital_temp)
    TextView tempText;

    @BindView(R.id.patient_view_ecg_cm22)
    EcgCM22ShowView ecgViewCM22;
    @BindView(R.id.patient_view_ecg_cm19)
    EcgShowView ecgViewCM19;
    @BindView(R.id.patient_view_resp)
    RespShowView respView;
    @BindView(R.id.patient_view_ppg)
    PPGShowView ppgView;

    @BindView(R.id.vital_start_bp)
    Button vital_start_bp;

    @BindView(R.id.ll_layout_CM19)
    LinearLayout ll_layout_CM19;
    @BindView(R.id.ll_layout_CM22)
    LinearLayout ll_layout_CM22;

    @BindView(R.id.patient_view_time)
    TextView patient_view_time;

    @BindView(R.id.vital_start)
    Button vital_start;
    private Queue<Integer> dataQueueEcgCM19 = new LinkedList<>();
    private Queue<Integer> getDataQueueEcgCM22 = new LinkedList<>();
    private Queue<Integer> dataQueueResp = new LinkedList<>();
    private Queue<Integer> dataQueuePPG = new LinkedList<>();

    private Timer timerCM19;
    private Timer timerCM22;

    //断开重连
    private Timer timerBle;

    private int indexEcgCM19 = 0;
    private int indexResp = 0;
    private int[] shortsEcgCM19 = new int[5];
    private int[] shortsResp = new int[5];

    private int indexEcgCM22 = 0;
    private int indexPPG = 0;
    private int[] shortsEcgCM22 = new int[5];
    private int[] shortsPPG = new int[5];

    private int patientId;

    private int preId;

    private String preType;

    private int mMusicDuration;

    private MainPresenter presenter;

    private MyCountDownTimer mc;

    private int cmType;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_vitalsigns;
    }

    @Override
    protected void initView() {
        bleDeviceCm22Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_CM22_MAC);
        bleDeviceCm19Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_CM19_MAC);
        bleDeviceSpO2Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_SPO2_MAC);
        bleDeviceBPMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_QIANSHAN_MAC);
        bleDeviceTempMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_IRT_MAC);
        //虽然做生命体征检测，但是康养床蓝牙还是要连接的，因为康养床有呼叫功能，必须保持蓝牙连接
        bleDeviceKYCMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_KYC_MAC);
        goService(DataReaderService.class);

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        EventBus.getDefault().unregister(this);
        if (timerBle != null) {
            timerBle.cancel();
        }
        if (timerCM19 != null) {
            timerCM19.cancel();
        }
        if (timerCM22 != null) {
            timerCM22.cancel();
        }
    }



    @Override
    protected void initData() {
        EventBus.getDefault().register(this);
        DataTransmitter.getInstance().addDataReceiver(VitalSignsActivity.this);
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
        bindService(new Intent(this, WebSocketService.class), serviceConnection, BIND_AUTO_CREATE);
        presenter = new MainPresenter(this, this);

        //判断是无创连续血压CM22还是生命体征cm19,从处方列表跳转过来，1为cm19  2为cm22
        UnFinishedPres unFinishedPres = (UnFinishedPres) getIntent().getParcelableExtra(TAG);

        preId = unFinishedPres.getPreId();

        preType = unFinishedPres.getPreType();

        // 获取评估训练时长
        mMusicDuration = unFinishedPres.getDuration();

        cmType = unFinishedPres.getFlag();

        Log.d(TAG,"preId:"+preId+"==preType:"+preType+"==mMusicDuration:"+mMusicDuration+"==cmType:"+cmType);

        if (cmType == 1) {
            //cm19需要知道勾选了哪些设备,顶部显示设备的蓝牙名称
            Log.d(TAG, "TYPE====" + cmType);
            List<Param> paramList = unFinishedPres.getParam();
            //1-血压，2-血氧，3-体温 4-心电
            for (Param param : paramList) {
                String value = param.getValue();
                if (value.equals("1")) {
                    //血压
                    bleBP.setVisibility(View.VISIBLE);

                } else if (value.equals("2")) {
                    //血氧
                    bleSpo2.setVisibility(View.VISIBLE);
                } else if (value.equals("3")) {
                    //体温
                    bleTemp.setVisibility(View.VISIBLE);
                } else if (value.equals("4")) {
                    //心电cm19
                    bleCM19.setVisibility(View.VISIBLE);
                }
            }
            vital_start_bp.setVisibility(View.VISIBLE);
            ll_layout_CM19.setVisibility(View.VISIBLE);
            ll_layout_CM22.setVisibility(View.GONE);
        } else if (cmType == 2) {
            //无创连续血压
            Log.d(TAG, "TYPE====" + cmType);
            List<Param> paramList = unFinishedPres.getParam();
            //无需遍历上面的集合，就是cm22无创血压设备
            bleCM22.setVisibility(View.VISIBLE);
            vital_start_bp.setVisibility(View.GONE);
            ll_layout_CM19.setVisibility(View.GONE);
            ll_layout_CM22.setVisibility(View.VISIBLE);
        }

        timerBle = new Timer();
        //画心电图和呼吸ppg

        if (cmType == 1) {
            Log.d(TAG,"GO=====ON========CM19");
            //cm19
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
                    ecgViewCM19.showLine(shortsEcgCM19[indexEcgCM19] );
                    respView.showLine(shortsResp[indexResp]);
                    indexEcgCM19++;
                    indexResp++;


                }
            }, 100, 17);


        } else if (cmType == 2) {
            //cm22
            timerCM22 = new Timer();
            //cm22无创连续血压
            timerCM22.schedule(new TimerTask() {
                @Override
                public void run() {
                    //很重要，从队列里面取5个数据
                    //取数据的计算方法：采样率为200，定时器25ms绘制一次，（200/1000）*25ms =5个数据

                    for (int i = 0; i < 5; i++) {

                        Integer x = getDataQueueEcgCM22.poll();

                        Integer y = dataQueuePPG.poll();

                        if (x == null) {
                            continue;
                        }

                        if (y == null) {
                            continue;
                        }
                        shortsEcgCM22[i] = x;
                        shortsPPG[i] = y;
                    }


                    if (indexEcgCM22 >= shortsEcgCM22.length) {
                        indexEcgCM22 = 0;
                    }

                    if (indexPPG >= 0) {
                        indexPPG = 0;
                    }
                    ecgViewCM22.showLine(shortsEcgCM22[indexEcgCM22]);
                    ppgView.showLine(shortsPPG[indexPPG]);
                    indexEcgCM22++;
                    indexPPG++;

                }
            }, 100, 25);
        }
    }

    //弹窗提示
    private void showDialog(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("你确定要中断此次检测吗？")
                .positiveText("确定")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG,"MaterialDialog确定");
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
    @OnClick(R.id.vital_start)
    public void start() {
        if(vital_start.getText().toString().equals("开始")){
            scanBle();
        }else if(vital_start.getText().toString().equals("中断")){
            //todo 中断操作
            showDialog();
        }

    }

    //按键返回

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            showDialog();
        }
        return false;

    }

    //返回箭头
    @OnClick(R.id.back)
    public void back() {
        //todo
        showDialog();
//        finish();
    }

    @OnClick(R.id.vital_close)
    public void close() {
        if (webSocketService != null) {
            webSocketService.close();
        }
    }

    //手动点击开始测量血压
    @OnClick(R.id.vital_start_bp)
    public void startBp() {
        EventBus.getDefault().post(2);
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

                //这个里面是否需要连接，还需要根据处方给的设备情况
                if (bleDevice.getMac().equals(bleDeviceCm22Mac)) {
                    //判断TextView是否是显示状态，是显示状态才连接
                    if (bleCM22.getVisibility() == View.VISIBLE) {
                        connectBle(bleDevice);
                    }

                }

                if (bleDevice.getMac().equals(bleDeviceCm19Mac)) {
                    if (bleCM19.getVisibility() == View.VISIBLE) {
                        connectBle(bleDevice);
                    }

                }

                if (bleDevice.getMac().equals(bleDeviceSpO2Mac)) {
                    if (bleSpo2.getVisibility() == View.VISIBLE) {
                        connectBle(bleDevice);
                    }
                }
                if (bleDevice.getMac().equals(bleDeviceBPMac)) {
                    if (bleBP.getVisibility() == View.VISIBLE) {
                        connectBle(bleDevice);
                    }
                }
                if (bleDevice.getMac().equals(bleDeviceTempMac)) {
                    if (bleTemp.getVisibility() == View.VISIBLE) {
                        connectBle(bleDevice);
                    }
                }
//                if (bleDevice.getMac().equals(bleDeviceKYCMac)) {
//                    connectBle(bleDevice);
//                }

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
                //蓝牙连接失败
           /*     if(bleDevice.getName().contains(Constant.CM19)){
                    bleCM.setTextColor(getResources().getColor(R.color.black));
                    retryConnectBle(bleDevice);
                } else if (bleDevice.getName().contains(Constant.SPO2)) {
                    bleSpo2.setTextColor(getResources().getColor(R.color.black));
                    retryConnectBle(bleDevice);
                }else if(bleDevice.getName().contains(Constant.QIANSHAN)){
                    bleBP.setTextColor(getResources().getColor(R.color.black));
                    retryConnectBle(bleDevice);
                }else if(bleDevice.getName().contains(Constant.IRT)){
                    bleTemp.setTextColor(getResources().getColor(R.color.black));
                    retryConnectBle(bleDevice);
                }else if(bleDevice.getName().contains(Constant.CM22)){
                    blePress.setTextColor(getResources().getColor(R.color.black));
                    retryConnectBle(bleDevice);
                }*/
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onConnectSuccess:status:" + status);
                deviceListConnect.add(bleDevice);
                EventBus.getDefault().post(deviceListConnect); //连接成功之后，发送给DataReaderService，进行开启通知，或者写入操作
                //根据名称进行对设备文字颜色进行变更
                if (bleDevice.getName().contains(Constant.CM19)) {
                    bleCM19.setTextColor(getResources().getColor(R.color.ecgText));
                    retryNum = 1;
                } else if (bleDevice.getName().contains(Constant.SPO2)) {
                    bleSpo2.setTextColor(getResources().getColor(R.color.ecgText));
                    retryNum = 1;
                } else if (bleDevice.getName().contains(Constant.QIANSHAN)) {
                    bleBP.setTextColor(getResources().getColor(R.color.ecgText));
                    retryNum = 1;
                } else if (bleDevice.getName().contains(Constant.IRT)) {
                    bleTemp.setTextColor(getResources().getColor(R.color.ecgText));
                    retryNum = 1;
                } else if (bleDevice.getName().contains(Constant.CM22)) {
                    bleCM22.setTextColor(getResources().getColor(R.color.ecgText));
                    retryNum = 1;
                }
//                if(deviceListConnect.size()==1){
//                    //todo 通知可以开始测量血压和测量的时间频率 假如设置为2分钟一次
//                    EventBus.getDefault().post(2);
//                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        vital_start.setText("中断");
                    }
                });


                //把获取到的时间，进行展示，倒计时展示
                String timeStr = millisUntilFinishedToMin(Integer.valueOf(mMusicDuration)  * 1000);
                patient_view_time.setText(timeStr);
                //开启倒计时
                handler.sendEmptyMessageDelayed(3333, 1000);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onDisConnected:status:8" + status);
                /**
                 * 连接断开，特指连接后再断开的情况。在这里可以监控设备的连接状态，一旦连接断开，可以根据自身情况考虑对BleDevice对象进行重连操作。
                 * 需要注意的是，断开和重连之间最好间隔一段时间，否则可能会出现长时间连接不上的情况。
                 * 此外，如果通过调用disconnect(BleDevice bleDevice)方法，主动断开蓝牙连接的结果也会在这个方法中回调，
                 * 此时isActiveDisConnected将会是true。
                 */
                //蓝牙断开 状态为8
                if (bleDevice.getName().contains(Constant.CM19)) {
                    bleCM19.setTextColor(getResources().getColor(R.color.black));
                    if (retryNum < 5) {
                        retryConnectBle(bleDevice);
                    }
                } else if (bleDevice.getName().contains(Constant.SPO2)) {
                    bleSpo2.setTextColor(getResources().getColor(R.color.black));
                    if (retryNum < 5) {
                        retryConnectBle(bleDevice);
                    }

                } else if (bleDevice.getName().contains(Constant.QIANSHAN)) {
                    bleBP.setTextColor(getResources().getColor(R.color.black));
                    if (retryNum < 5) {
                        retryConnectBle(bleDevice);
                    }
                } else if (bleDevice.getName().contains(Constant.IRT)) {
                    bleTemp.setTextColor(getResources().getColor(R.color.black));
                    if (retryNum < 5) {
                        retryConnectBle(bleDevice);
                    }
                } else if (bleDevice.getName().contains(Constant.CM22)) {
                    bleCM22.setTextColor(getResources().getColor(R.color.black));
                    if (retryNum < 5) {
                        retryConnectBle(bleDevice);
                    }
                }
            }
        });

    }

    /**
     * 蓝牙断开，或者连接失败，进行重连操作
     * 需要开启一个定时器，进行三次重连操作，需要间隔一段时间进行
     */

    private int retryNum = 1;

    private void retryConnectBle(BleDevice bleDevice) {

        timerBle.schedule(new TimerTask() {
            @Override
            public void run() {
                connectBle(bleDevice);
                retryNum++;

            }
        }, 2000, 5000);


    }


    /**
     * 以下为数据接收器
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
                if (packet.heartRate > 0) {
                    heartRateText.setText(packet.heartRate + "");
                }

                if (packet.resp > 0) {
                    respText.setText(packet.resp + "");
                }

            }
        });
    }

    @Override
    public void onDataReceived(BPDevicePacket packet) {
        //这个里面是CM22设备，无创连续血压（心电和PPG）
        short[] ecg = packet.getsEcgData();
        short[] ppg = packet.getsPpgData();
        Log.d("onDataReceived=====", Arrays.toString(ecg));
        Log.d("onDataReceived", Arrays.toString(ppg));
        if (ecg.length != DevicePacket.ECG_IN_PACKET) {
            return;
        }
        for (int i = 0; i < 96; i++) {
            getDataQueueEcgCM22.add((int) ecg[i]);
            dataQueuePPG.add((int) ppg[i]);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (packet.getHeartRate() > 0 && packet.getHeartRate() < 300) {
                    heartRateText.setText(packet.getHeartRate() + "");
                }

                if (packet.getsSsPressDataData() > 0 && packet.getsSzPressDataData() > 0 && packet.getsSsPressDataData() < 300 && packet.getsSzPressDataData() < 300) {
                    bpText.setText(packet.getsSsPressDataData() + "/" + packet.getsSzPressDataData());
                }

            }
        });

    }

    @Override
    public void onDataReceived(byte[] packet) {
        //实时发送数据webSocket
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
                    Log.d("WebSocketService====", text);
                }
            });
        }

        @Override
        public void onOpen() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                tvMessage.setText("onOpen");
                    Log.d("WebSocketService====", "onOpen=====");
                }
            });
        }

        @Override
        public void onClosed() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                tvMessage.setText("onClosed");
                    Log.d("WebSocketService====", "onClosed====");
                }
            });
        }
    };


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        if (event instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) event;
            if (map != null) {

                if (map.containsKey(Constant.SPO2_DATA)) {
                    spo2Text.setText(map.get(Constant.SPO2_DATA) + "");
                }

                if (map.containsKey(Constant.IRT_DATA)) {
                    tempText.setText(map.get(Constant.IRT_DATA) + "");
                }

                Log.d("vital====1", map.get(Constant.BP_DATA) + "");
                if (map.containsKey(Constant.BP_DATA)) {
                    bpText.setText(map.get(Constant.BP_DATA) + "");
                }
                if (map.containsKey(Constant.BP_DATA_ERROR)) {
                    bpText.setText("测量失败");
                }
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

    }

    @Override
    public void setData(Object obj) {

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
            patient_view_time.setText(timeStr);
        }

        @Override
        public void onFinish() {
            //倒计时完成后，处理事物

            closeAll();

            //todo 语音提醒检测完成


            //调用接口，上传文件
            presenter.uploadFile(zipFiles(), "file_uploadReportLfs", String.valueOf(patientId), String.valueOf(preId), preType);


        }
    }

    //压缩文件
    private File zipFiles() {

        String dateNowStr = SPUtils.getInstance().getString(SP.KEY_ECG_FILE_TIME);
        patientId = SPUtils.getInstance().getInt(SP.PATIENT_ID);
        //原保存的文件路径
        String src = Environment.getExternalStorageDirectory().getPath() + "/HBed/data/" + patientId + "-" + dateNowStr;

        //将要压缩的文件zip
        String zip = Environment.getExternalStorageDirectory().getPath() + "/HBed/zipData/" + patientId + "-" + dateNowStr + ".zip";

        //判断zip文件是否存在并创建文件
        FileUtils.createOrExistsFile(zip);
        //压缩文件
        try {
            ZipUtils.zipFile(src, zip);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取压缩文件
        File file = FileUtils.getFileByPath(zip);
        return file;
    }

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

    //释放音频，移除handle中message
    private void closeAll() {
/*        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        if (perMediaPlayer != null) {
            perMediaPlayer.release();
        }

        if (bgMediaPlayer != null) {
            bgMediaPlayer.release();
        }

        handler.removeMessages(1111);
        handler.removeMessages(2222);
        handler.removeMessages(4444);*/

        if (null != mc) {
            mc.cancel();
            mc = null;
        }
        handler.removeMessages(3333);
        //关闭服务
        stopService(DataReaderService.class);


    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 3333) {
                //开启倒计时
                mc = new MyCountDownTimer(mMusicDuration * 1000, 1000);
                mc.start();
            }

        }
    };
}
