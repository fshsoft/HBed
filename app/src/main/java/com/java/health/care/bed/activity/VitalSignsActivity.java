package com.java.health.care.bed.activity;

import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
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
import com.java.health.care.bed.model.BPDevicePacket;
import com.java.health.care.bed.model.DataReceiver;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.model.EstimateRet;
import com.java.health.care.bed.service.DataReaderService;
import com.java.health.care.bed.service.WebSocketService;
import com.java.health.care.bed.widget.EcgShowView;
import com.java.health.care.bed.widget.PPGShowView;
import com.java.health.care.bed.widget.RespShowView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
public class VitalSignsActivity extends BaseActivity implements DataReceiver {
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
    @BindView(R.id.vital_ble_press)
    TextView blePress;//无创连续血压蓝牙
    @BindView(R.id.vital_ble_cm)
    TextView bleCM;
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

    @BindView(R.id.patient_view_ecg)
    EcgShowView ecgView;
    @BindView(R.id.patient_view_resp)
    RespShowView respView;
    @BindView(R.id.patient_view_ppg)
    PPGShowView ppgView;

    private Queue<Integer> dataQueue = new LinkedList<>();
    private Queue<Integer> dataQueueResp = new LinkedList<>();

    private Timer timer;
    private Timer timerBle;
    private int index = 0;
    private int indexResp = 0;
    private int[] shorts = new int[5];
    private int[] shortsResp = new int[5];


    //处方类型 生命体征检测1；无创连续血压2
    private int preType =1;
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
        //判断是无创连续血压CM22还是生命体征cm19,从处方列表跳转过来，1为cm19  2为cm22
        UnFinishedPres unFinishedPres = (UnFinishedPres) getIntent().getParcelableExtra(TAG);
        int type = unFinishedPres.getFlag();
        if(type == 1){
            //cm19需要知道勾选了哪些设备
            Log.d(TAG,"TYPE===="+type);
            List<Param> paramList = unFinishedPres.getParam();
            //1-血压，2-血氧，3-体温 4-心电
            for(Param param : paramList){
                String value = param.getValue();
                if(value.equals("1")){
                    //血压
                    bleBP.setVisibility(View.VISIBLE);

                }else if(value.equals("2")){
                    //血氧
                    bleSpo2.setVisibility(View.VISIBLE);
                }else if(value.equals("3")){
                    //体温
                    bleTemp.setVisibility(View.VISIBLE);
                }else if(value.equals("4")){
                    //心电cm19
                    bleCM.setVisibility(View.VISIBLE);
                }
            }

        }else if(type==2){
            //无创连续血压
            Log.d(TAG,"TYPE===="+type);
            List<Param> paramList = unFinishedPres.getParam();
            //无需遍历上面的集合，就是cm22无创血压设备
            blePress.setVisibility(View.VISIBLE);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
        EventBus.getDefault().unregister(this);
        if(timerBle!=null){
            timerBle.cancel();
        }
        if(timer!=null){
            timer.cancel();
        }

    }

    @OnClick(R.id.back)
    public void back(){
        finish();
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
        timerBle = new Timer();
        //画心电图和呼吸ppg
        timer = new Timer();
         /* timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //很重要，从队列里面取5个数据
                //取数据的计算方法：采样率为300，定时器17ms绘制一次，（300/1000）*17ms =5.1个数据

                for (int i = 0; i < 5; i++) {

                    Integer x = dataQueue.poll();

                    Integer y = dataQueueResp.poll();

                    if (x == null) {
                        continue;
                    }

                    if(y ==null){
                        continue;
                    }
                    shorts[i] = x;
                    shortsResp[i] =y;
                }


                if (index >= shorts.length) {
                    index = 0;
                }

                if(indexResp >=0){
                    indexResp = 0;
                }
                ecgView.showLine(shorts[index] );
                respView.showLine(shortsResp[indexResp]);
                index++;
                indexResp++;


            }
        }, 100, 17);
*/
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //很重要，从队列里面取5个数据
                //取数据的计算方法：采样率为200，定时器25ms绘制一次，（200/1000）*25ms =5个数据

                for (int i = 0; i < 5; i++) {

                    Integer x = dataQueue.poll();

                    Integer y = dataQueueResp.poll();

                    if (x == null) {
                        continue;
                    }

                    if(y ==null){
                        continue;
                    }
                    shorts[i] = x;
                    shortsResp[i] =y;
                }


                if (index >= shorts.length) {
                    index = 0;
                }

                if(indexResp >=0){
                    indexResp = 0;
                }
                ecgView.showLine(shorts[index] );
                ppgView.showLine(shortsResp[indexResp]);
                index++;
                indexResp++;

            }
        }, 100, 25);
    }

    @OnClick(R.id.vital_start)
    public void start() {
        scanBle();
    }

    @OnClick(R.id.vital_close)
    public void close() {
        if (webSocketService != null) {
            webSocketService.close();
        }
    }

    @OnClick(R.id.vital_start_bp)
    public void startBp(){
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
                    if(blePress.getVisibility()== View.VISIBLE){
                        connectBle(bleDevice);
                    }

                }

                if (bleDevice.getMac().equals(bleDeviceCm19Mac)) {
                    if(bleCM.getVisibility()== View.VISIBLE){
                        connectBle(bleDevice);
                    }

                }

                if (bleDevice.getMac().equals(bleDeviceSpO2Mac)) {
                    if(bleSpo2.getVisibility()== View.VISIBLE){
                        connectBle(bleDevice);
                    }
                }
                if (bleDevice.getMac().equals(bleDeviceBPMac)) {
                    if(bleBP.getVisibility()== View.VISIBLE){
                        connectBle(bleDevice);
                    }
                }
                if (bleDevice.getMac().equals(bleDeviceTempMac)) {
                    if(bleTemp.getVisibility()== View.VISIBLE){
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
                if(bleDevice.getName().contains(Constant.CM19)){
                    bleCM.setTextColor(getResources().getColor(R.color.ecgText));
                    retryNum =1;
                } else if (bleDevice.getName().contains(Constant.SPO2)) {
                    bleSpo2.setTextColor(getResources().getColor(R.color.ecgText));
                    retryNum =1;
                }else if(bleDevice.getName().contains(Constant.QIANSHAN)){
                    bleBP.setTextColor(getResources().getColor(R.color.ecgText));
                    retryNum =1;
                }else if(bleDevice.getName().contains(Constant.IRT)){
                    bleTemp.setTextColor(getResources().getColor(R.color.ecgText));
                    retryNum =1;
                }else if(bleDevice.getName().contains(Constant.CM22)){
                    blePress.setTextColor(getResources().getColor(R.color.ecgText));
                    retryNum =1;
                }
//                if(deviceListConnect.size()==1){
//                    //todo 通知可以开始测量血压和测量的时间频率 假如设置为2分钟一次
//                    EventBus.getDefault().post(2);
//                }
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
                if(bleDevice.getName().contains(Constant.CM19)){
                    bleCM.setTextColor(getResources().getColor(R.color.black));
                    if(retryNum<5){
                        retryConnectBle(bleDevice);
                    }
                } else if (bleDevice.getName().contains(Constant.SPO2)) {
                    bleSpo2.setTextColor(getResources().getColor(R.color.black));
                    if(retryNum<5){
                        retryConnectBle(bleDevice);
                    }

                }else if(bleDevice.getName().contains(Constant.QIANSHAN)){
                    bleBP.setTextColor(getResources().getColor(R.color.black));
                    if(retryNum<5){
                        retryConnectBle(bleDevice);
                    }
                }else if(bleDevice.getName().contains(Constant.IRT)){
                    bleTemp.setTextColor(getResources().getColor(R.color.black));
                    if(retryNum<5){
                        retryConnectBle(bleDevice);
                    }
                }else if(bleDevice.getName().contains(Constant.CM22)){
                    blePress.setTextColor(getResources().getColor(R.color.black));
                    if(retryNum<5){
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

    private int retryNum =1;
    private void retryConnectBle(BleDevice bleDevice){

        timerBle.schedule(new TimerTask() {
            @Override
            public void run() {
                connectBle(bleDevice);
                retryNum++;

            }
        },2000,5000);



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

//        Log.d("aaron====987",Arrays.toString(ecg));
//        Log.d("aaron====789",Arrays.toString(resp));

        if (ecg.length != DevicePacket.ECG_IN_PACKET) {
            return;
        }

        for (int i = 0; i < 96; i++) {
            dataQueue.add((int) ecg[i]);
            dataQueueResp.add(resp[i]);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (packet.heartRate >0) {
                    heartRateText.setText(packet.heartRate + "");
                }else {
                    heartRateText.setText("--");
                }

                if (packet.resp > 0) {
                    respText.setText(packet.resp + "");
                }else {
                    respText.setText("--");
                }

            }
        });
    }

    @Override
    public void onDataReceived(BPDevicePacket packet) {
        //这个里面是CM22设备，无创连续血压（心电和PPG）
        short[] ecg = packet.getsEcgData();
        short[] ppg = packet.getsPpgData();
        Log.d("onDataReceived=====",Arrays.toString(ecg));
        Log.d("onDataReceived",Arrays.toString(ppg));
        if (ecg.length != DevicePacket.ECG_IN_PACKET) {
            return;
        }
        for (int i = 0; i < 96; i++) {
            dataQueue.add((int) ecg[i]);
            dataQueueResp.add((int) ppg[i]);
        }



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
        if(event instanceof Map){
            Map<String, Object> map = (Map<String, Object>) event;
            if (map != null) {

                if (map.containsKey(Constant.SPO2_DATA)) {
                    spo2Text.setText(map.get(Constant.SPO2_DATA) + "");
                }

                if (map.containsKey(Constant.IRT_DATA)) {
                    tempText.setText(map.get(Constant.IRT_DATA) + "");
                }

                Log.d("vital====1",map.get(Constant.BP_DATA)+"");
                if (map.containsKey(Constant.BP_DATA)) {
                    bpText.setText(map.get(Constant.BP_DATA) + "");
                }
                if (map.containsKey(Constant.BP_DATA_ERROR)) {
                    bpText.setText("测量失败");
                }
            }
        }

    }

}
