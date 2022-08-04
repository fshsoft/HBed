package com.java.health.care.bed.device;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.util.ByteUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


/**
 * @author fsh
 * @date 2022/08/04 10:51
 * @Description 服务
 */
public class DataReaderService extends Service {

    public static final String TAG = DataReaderService.class.getSimpleName();

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "onStart()");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);

        Log.d(TAG, "onCreate()");


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Object event) {
        List<BleDevice> deviceList = (List<BleDevice>) event;
        if (deviceList != null) {
            for (BleDevice bleDevice : deviceList) {
                String bleName = bleDevice.getName();
                if (bleName.contains("CM19")) {
                    getCM19BleDevice(bleDevice);
                } else if (bleName.contains("SpO2")) {
                    getSpO2BleDevice(bleDevice);
                } else if (bleName.contains("QianShan")) {
                    getBPBleDevice(bleDevice);
                } else {

                }
            }
        }

    }

    /**
     * 收到CM19的bleDevice
     */
    private void getCM19BleDevice(BleDevice bleDevice) {
        BleManager.getInstance().notify(
                bleDevice,
                Constant.UUID_SERVICE_CM19,
                Constant.UUID_CHARA_CM19,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        Log.d(TAG, "打开通知成功");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Log.d(TAG, exception.toString() + "cm19打开通知失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.d(TAG, Arrays.toString(data));
                    }
                });
    }

    /**
     * 收到血氧bleDevice
     */
    private void getSpO2BleDevice(BleDevice bleDevice) {
        BleManager.getInstance().notify(
                bleDevice,
                Constant.UUID_SERVICE_SPO2,
                Constant.UUID_CHARA_SPO2,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        Log.d(TAG, "打开通知成功");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Log.d(TAG, exception.toString() + "打开通知失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.d(TAG + "SpO2=======", Arrays.toString(data));
                    }
                });
    }

    /**
     * 收到血压计bleDevice
     */
    private void getBPBleDevice(BleDevice bleDevice) {

        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        String data = df.format(new Date());
        String ready = Constant.READY_ONE + data + Constant.READY_TWO;

        BluetoothGatt mBluetoothGatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
        BluetoothGattService bluetoothGattService = mBluetoothGatt.getService(UUID.fromString(Constant.UUID_SERVICE_BP));
        BluetoothGattCharacteristic characteristic = bluetoothGattService.getCharacteristic(UUID.fromString(Constant.UUID_CHARA_NOTIFY_BP));
        characteristic.setValue(ByteUtil.HexString2Bytes(ready));
        characteristic.setValue(Constant.START);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "RETURN");
                return;
            }
        }
        mBluetoothGatt.writeCharacteristic(characteristic);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        Log.d(TAG, "GO ON");

        BleManager.getInstance().notify(
                bleDevice,
                Constant.UUID_SERVICE_BP,
                Constant.UUID_CHARA_NOTIFY_BP,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        // 打开通知操作成功
                        Log.d(TAG, "打开通知成功");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        // 打开通知操作失败
                        Log.d(TAG, exception.toString() + "打开通知失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        // 打开通知后，设备发过来的数据将在这里出现
                        Log.d(TAG + "bp=======", Arrays.toString(data));
                    }
                }

        );


    }


}
