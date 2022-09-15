package com.java.health.care.bed.activity;

import android.bluetooth.BluetoothGatt;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.model.BPDevicePacket;
import com.java.health.care.bed.model.DataReceiver;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.model.EstimateRet;
import com.java.health.care.bed.service.DataReaderService;
import com.java.health.care.bed.service.WebSocketService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.OnClick;


/**
 * @author fsh
 * @date 2022/08/03 14:40
 * @Description  生命体征
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

    @Override
    protected int getLayoutId() {
        return R.layout.activity_vitalsigns;
    }

    @Override
    protected void initView() {
        bleDeviceCm22Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_CM22_MAC);
        bleDeviceCm19Mac=SPUtils.getInstance().getString(Constant.BLE_DEVICE_CM19_MAC);
        bleDeviceSpO2Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_SPO2_MAC);
        bleDeviceBPMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_QIANSHAN_MAC);
        bleDeviceTempMac =  SPUtils.getInstance().getString(Constant.BLE_DEVICE_IRT_MAC);
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

    }

    @Override
    protected void initData() {
        DataTransmitter.getInstance().addDataReceiver(VitalSignsActivity.this);
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
        bindService(new Intent(this, WebSocketService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @OnClick(R.id.vital_start)
    public void start(){
        scanBle();
    }
    @OnClick(R.id.vital_close)
    public void close(){
        if (webSocketService != null) {
            webSocketService.close();
        }
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

                if (bleDevice.getMac().equals(bleDeviceCm22Mac)) {
                    connectBle(bleDevice);
                }

                if(bleDevice.getMac().equals(bleDeviceCm19Mac)){
                    connectBle(bleDevice);
                }

                if(bleDevice.getMac().equals(bleDeviceSpO2Mac)){
                    connectBle(bleDevice);
                }
                if (bleDevice.getMac().equals(bleDeviceBPMac)) {
                    connectBle(bleDevice);

                }
                if(bleDevice.getMac().equals(bleDeviceTempMac)){
                    connectBle(bleDevice);
                }
                if(bleDevice.getMac().equals(bleDeviceKYCMac)){
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
     * 以下为数据接收器
     * @param packet
     */
    @Override
    public void onDataReceived(DevicePacket packet) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                short[] ecgData = packet.secgdata;
                Log.d(TAG, Arrays.toString(ecgData));
            }
        }).start();
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
}
