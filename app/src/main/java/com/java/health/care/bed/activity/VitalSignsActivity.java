package com.java.health.care.bed.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
 * @Description ????????????  ?????? ????????????????????????
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

    //??????????????????
    private boolean isConnectAndReadData = false;
    //?????????????????????????????????????????????????????????
    private int bleConnectNum =0;
    //1???????????????????????????????????????ble?????????2???????????????????????????????????????????????????????????????
    @BindView(R.id.vital_ble_cm22)
    TextView bleCM22;//????????????????????????
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

    //????????????
    private Timer timerBle;

    private int indexEcgCM19 = 0;
    private int indexResp = 0;
    private int[] shortsEcgCM19 = new int[3];
    private int[] shortsResp = new int[3];

    private int indexEcgCM22 = 0;
    private int indexPPG = 0;
    private int[] shortsEcgCM22 = new int[2];
    private int[] shortsPPG = new int[2];

    private int patientId;
    private int hospitalId;
    private int bunkId;

    private int regionId;

    private int preId;

    private String preType;

    private int mMusicDuration;

    private MainPresenter mainPresenter;

    private MyCountDownTimer mc;

    private int cmType;

    private ProgressDialog progressDialog;


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
        //?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        bleDeviceKYCMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_KYC_MAC);



    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeAll();
    }



    @Override
    protected void initData() {
        EventBus.getDefault().register(this);

        //??????DataReaderService??????????????????????????? ???????????????cm19??????????????????true  ??????????????????????????????false
        EventBus.getDefault().post(true);

        DataTransmitter.getInstance().addDataReceiver(VitalSignsActivity.this);
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
        bindService(new Intent(this, WebSocketService.class), serviceConnection, BIND_AUTO_CREATE);
        mainPresenter = new MainPresenter(this, this);

        patientId = SPUtils.getInstance().getInt(SP.PATIENT_ID);
        bunkId = SPUtils.getInstance().getInt(SP.BUNK_ID);
        regionId = SPUtils.getInstance().getInt(SP.REGION_ID);

        //???????????????????????????CM22??????????????????cm19,??????????????????????????????1???cm19  2???cm22
        UnFinishedPres unFinishedPres = (UnFinishedPres) getIntent().getParcelableExtra(TAG);

        preId = unFinishedPres.getPreId();

        preType = unFinishedPres.getPreType();

        // ????????????????????????
        mMusicDuration = unFinishedPres.getDuration();

        cmType = unFinishedPres.getFlag();

        Log.d(TAG,"preId:"+preId+"==preType:"+preType+"==mMusicDuration:"+mMusicDuration+"==cmType:"+cmType);

        if (cmType == 1) {
            //cm19?????????????????????????????????,?????????????????????????????????
            Log.d(TAG, "TYPE====" + cmType);
            List<Param> paramList = unFinishedPres.getParam();
            //1-?????????2-?????????3-?????? 4-??????
            for (Param param : paramList) {
                String value = param.getValue();
                if (value.equals("1")) {
                    //??????
                    bleBP.setVisibility(View.VISIBLE);

                } else if (value.equals("2")) {
                    //??????
                    bleSpo2.setVisibility(View.VISIBLE);
                } else if (value.equals("3")) {
                    //??????
                    bleTemp.setVisibility(View.VISIBLE);
                } else if (value.equals("4")) {
                    //??????cm19
                    bleCM19.setVisibility(View.VISIBLE);
                }
            }
            vital_start_bp.setVisibility(View.VISIBLE);
            ll_layout_CM19.setVisibility(View.VISIBLE);
            ll_layout_CM22.setVisibility(View.GONE);
        } else if (cmType == 2) {
            //??????????????????
            Log.d(TAG, "TYPE====" + cmType);
            List<Param> paramList = unFinishedPres.getParam();
            //????????????????????????????????????cm22??????????????????
            bleCM22.setVisibility(View.VISIBLE);
            vital_start_bp.setVisibility(View.GONE);
            ll_layout_CM19.setVisibility(View.GONE);
            ll_layout_CM22.setVisibility(View.VISIBLE);
        }

        timerBle = new Timer();
        //?????????????????????ppg

        if (cmType == 1) {
            Log.d(TAG,"GO=====ON========CM19");
            //cm19
            timerCM19 = new Timer();
            //cm19
            timerCM19.schedule(new TimerTask() {
                @Override
                public void run() {
                    //??????????????????????????????5?????????
                    //???????????????????????????????????????300????????????17ms??????????????????300/1000???*17ms =5.1?????????

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
                    ecgViewCM19.showLine(shortsEcgCM19[indexEcgCM19] );
                    respView.showLine(shortsResp[indexResp]);
                    indexEcgCM19++;
                    indexResp++;


                }
            }, 100, 10);


        } else if (cmType == 2) {
            //cm22
            timerCM22 = new Timer();
            //cm22??????????????????
            timerCM22.schedule(new TimerTask() {
                @Override
                public void run() {
                    //??????????????????????????????5?????????
                    //???????????????????????????????????????200????????????25ms??????????????????200/1000???*25ms =5?????????  5 5 5 25

                    for (int i = 0; i < 2; i++) {

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
            }, 100, 10);
        }

        //??????????????????
    /*    new Thread(new Runnable() {
            @Override
            public void run() {
                String zip = Environment.getExternalStorageDirectory().getPath() + "/HBed/zipData/"+ "1" + "-" + "20220928180120.zip";
                //????????????
                File file = FileUtils.getFileByPath(zip);

                //????????????
                presenter.uploadFile(file,"file_uploadReportLfs","1","15","RESONANCE");
            }
        }).start();*/
    }

    //????????????
    private void showDialog(){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("????????????????????????????????????")
                .positiveText("??????")
                .negativeText("??????")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG,"MaterialDialog??????");
                        //??????cm19??????
                        if(deviceListConnect!=null){
                            for (BleDevice bleDevice: deviceListConnect) {
                                BleManager.getInstance().disconnect(bleDevice);
                            }
                        }

                        //???????????????????????????
                        mainPresenter.uploadFile(zipFiles(), "file_uploadReportLfs", String.valueOf(patientId), String.valueOf(preId), preType);


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
        if(vital_start.getText().toString().equals("??????")){
            String rr = Environment.getExternalStorageDirectory().getPath()+"/H300L"+"/rr.txt";
            if(rr!=null){
                //????????????????????????????????????rr.txt??????
                FileUtils.delete(rr);
            }
            progressDialog = new ProgressDialog(this);
            progressDialog.show();
            scanBle();
        }else if(vital_start.getText().toString().equals("??????")){
            //todo ????????????
            if(isConnectAndReadData){
                showDialog();
            }else {
                finish();
            }

        }

    }

    //????????????

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(isConnectAndReadData){
                showDialog();
            }else {
                finish();
            }

        }
        return false;

    }

    //????????????
    @OnClick(R.id.back)
    public void back() {
        if(isConnectAndReadData){
            showDialog();
        }else {
            finish();
        }
    }

    @OnClick(R.id.vital_close)
    public void close() {
        if (webSocketService != null) {
            webSocketService.close();
        }
    }

    //??????????????????????????????
    @OnClick(R.id.vital_start_bp)
    public void startBp() {
        EventBus.getDefault().post(2);
    }

    private void scanBle() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                Log.d(TAG, "bleDeviceMac:success:" + "onScanFinished");

            }

            @Override
            public void onScanStarted(boolean success) {
                //onScanStarted(boolean success): ????????????????????????????????????????????????????????????????????????
                // ????????????????????????????????????????????????????????????????????????????????????????????????
                Log.d(TAG, "bleDeviceMac:success:" + "onScanStarted:"+success);
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
                Log.d(TAG, "bleDeviceMac:success:" + "onScanning");
                //????????????????????????????????????????????????????????????????????????
                if (bleDevice.getMac().equals(bleDeviceCm22Mac)) {
                    //??????TextView????????????????????????????????????????????????
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

    /**
     * ????????????????????? cm19??????????????????//   ?????????????????????cm22???????????????
     * @param bleDevice
     */
    private void connectBle(BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d(TAG, "onStartConnect:");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {

                progressDialog.dismiss();
//                showToast("??????????????????");
                Log.d(TAG, "onConnectFail:exception:" + exception.toString());
                //??????????????????
                if(bleDevice.getName().contains(Constant.CM19)){
                    if (retryNum < 3) {
                        retryConnectBle(bleDevice);
                    }else {
                        //??????cm19????????????????????????????????????
                        handler.sendEmptyMessage(4444);
                    }
                } else if (bleDevice.getName().contains(Constant.SPO2)) {
                    if (retryNum < 3) {
                        retryConnectBle(bleDevice);
                    }
                }else if(bleDevice.getName().contains(Constant.QIANSHAN)){
                    if (retryNum < 3) {
                        retryConnectBle(bleDevice);
                    }
                }else if(bleDevice.getName().contains(Constant.IRT)){
                    if (retryNum < 3) {
                        retryConnectBle(bleDevice);
                    }
                }else if(bleDevice.getName().contains(Constant.CM22)){
                    if (retryNum < 3) {
                        retryConnectBle(bleDevice);
                    }else {
                        //??????cm22????????????????????????????????????
                        handler.sendEmptyMessage(4444);
                    }
                }
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                bleConnectNum++;
                progressDialog.dismiss();
                Log.d(TAG, "onConnectSuccess:status:" + status);
                deviceListConnect.add(bleDevice);
                EventBus.getDefault().post(deviceListConnect); //??????????????????????????????DataReaderService??????????????????????????????????????????
                //???????????????????????????????????????????????????
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
//                    //todo ?????????????????????????????????????????????????????? ???????????????2????????????
//                    EventBus.getDefault().post(2);
//                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        vital_start.setText("??????");
                    }
                });


            /*    //??????????????????????????????????????????????????????
                String timeStr = millisUntilFinishedToMin(Integer.valueOf(mMusicDuration)  * 1000);
                patient_view_time.setText(timeStr);*/
                //???????????????
                if(bleConnectNum==1){
                    handler.sendEmptyMessageDelayed(3333, 1000);
                }

            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onDisConnected:status:8" + status);
                bleConnectNum--;

                /**
                 * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????BleDevice???????????????????????????
                 * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                 * ???????????????????????????disconnect(BleDevice bleDevice)???????????????????????????????????????????????????????????????????????????
                 * ??????isActiveDisConnected?????????true???
                 */
                //???????????? ?????????8
                if (bleDevice.getName().contains(Constant.CM19)) {
                    bleCM19.setTextColor(getResources().getColor(R.color.black));

                    retryConnectBle(bleDevice);

                } else if (bleDevice.getName().contains(Constant.SPO2)) {
                    bleSpo2.setTextColor(getResources().getColor(R.color.black));

                    retryConnectBle(bleDevice);


                } else if (bleDevice.getName().contains(Constant.QIANSHAN)) {
                    bleBP.setTextColor(getResources().getColor(R.color.black));

                    retryConnectBle(bleDevice);

                } else if (bleDevice.getName().contains(Constant.IRT)) {
                    bleTemp.setTextColor(getResources().getColor(R.color.black));

                    retryConnectBle(bleDevice);

                } else if (bleDevice.getName().contains(Constant.CM22)) {
                    bleCM22.setTextColor(getResources().getColor(R.color.black));

                    retryConnectBle(bleDevice);

                }
            }
        });

    }

    /**
     * ??????????????????????????????????????????????????????
     * ???????????????????????????????????????????????????????????????????????????????????????
     */

    private int retryNum = 1;

    private void retryConnectBle(BleDevice bleDevice) {
        if(null!=timerBle){
            timerBle.schedule(new TimerTask() {
                @Override
                public void run() {
                    connectBle(bleDevice);
                    retryNum++;

                }
            }, 2000, 5000);

        }

    }


    /**
     * ????????????????????????
     *
     * @param packet
     */
    @Override
    public void onDataReceived(DevicePacket packet) {
        //???????????????CM19??????????????????????????????????????????
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
        //???????????????CM22???????????????????????????????????????PPG???
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
        isConnectAndReadData= true;
        //??????????????????webSocket
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
                    bpText.setText("????????????");
                }
            }
        }else if(event instanceof Integer){
            int num = (int) event;
            mainPresenter.sendMessage(regionId,bunkId,num,patientId);
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
        //??????????????????
        boolean isSuccess = (boolean) obj;
        if(isSuccess==true){
            showToast("??????????????????");

            finish();
        }
    }

    @Override
    public void setData(Object obj) {

    }

    /**
     * ???????????????????????????????????????
     */
    public class MyCountDownTimer extends CountDownTimer {


        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            //?????????????????????????????????
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            String timeStr = millisUntilFinishedToMin(millisUntilFinished);
            patient_view_time.setText(timeStr);
        }

        @Override
        public void onFinish() {
            //?????????????????????????????????

//            closeAll();

            //todo ????????????????????????


            //???????????????????????????
            mainPresenter.uploadFile(zipFiles(), "file_uploadReportLfs", String.valueOf(patientId), String.valueOf(preId), preType);


        }
    }

    //????????????
    private File zipFiles() {
        patientId = SPUtils.getInstance().getInt(SP.PATIENT_ID);

        String startTime = SPUtils.getInstance().getString(SP.KEY_ECG_FILE_TIME);

        //????????????????????????
        String src = Environment.getExternalStorageDirectory().getPath()+"/HBed/data/"+patientId+"-"+startTime;

        File file1 = new File(src+"/lifeData.data");
        //file2????????????rr.txt
        File file2 = new File(Environment.getExternalStorageDirectory().getPath()+"/H300L"+"/rr.txt");
        if(file2.exists()){
            file2.mkdir();
        }
        List<File> fileList = new ArrayList<>(2);
        fileList.add(file1);
        fileList.add(file2);

        //?????????????????????zip ????????????????????????hospitalId_doctorId_patientId_startTime_preId_reportType reportType?????????2
        String zip = Environment.getExternalStorageDirectory().getPath() +
                "/HBed/zipData/"+ patientId +  "_" + preId + "_" + startTime +"_" +preType + ".zip";
//                "/HBed/zipData/"+ + patientId + "_" + preId + "_" + preType+"_2_LIFE_" + ".zip";

        //??????zip?????????????????????????????????
        FileUtils.createOrExistsFile(zip);
        //????????????
        try {
//            ZipUtils.zipFile(src,zip);

            ZipUtils.zipFiles(fileList,FileUtils.getFileByPath(zip));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //??????????????????
        File file = FileUtils.getFileByPath(zip);
        return file;
    }

    /**
     * ???????????????
     */
    private String millisUntilFinishedToMin(long millisUntilFinished) {
        StringBuffer sb = new StringBuffer();
        int min = (int) (millisUntilFinished / 60 / 1000);
        int s = (int) ((millisUntilFinished % (60 * 1000)) / 1000);
//        MyLogUtils.d(TAG,"?????????????????????:"+s);
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

    //?????????????????????handle???message
    private void closeAll() {
        BleManager.getInstance().disconnectAllDevice();
        unbindService(serviceConnection);
        EventBus.getDefault().unregister(this);
        if (timerBle != null) {
            timerBle.cancel();
            timerBle=null;
        }
        if (timerCM19 != null) {
            timerCM19.cancel();
            timerCM19 =null;
        }
        if (timerCM22 != null) {
            timerCM22.cancel();
            timerCM22 = null;
        }
        if (null != mc) {
            mc.cancel();
            mc = null;
        }
        //???????????????
        handler.removeMessages(3333);


    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 3333) {
                //???????????????
                mc = new MyCountDownTimer(mMusicDuration * 1000, 1000);
                mc.start();
            }else if(msg.what==4444){
                if (null != mc) {
                    mc.cancel();
                    mc = null;
                }
            }

        }
    };
}
