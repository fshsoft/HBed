package com.java.health.care.bed.device;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
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
import java.util.Arrays;
import java.util.List;


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
                if (bleName.contains(Constant.CM19)) {
                    getCM19BleDevice(bleDevice);
                } else if (bleName.contains(Constant.SPO2)) {
                    getSpO2BleDevice(bleDevice);
                } else if (bleName.contains(Constant.QIANSHAN)) {
                    writeBPBleDevice(bleDevice);
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
                Constant.UUID_CHARA_CM19_NOTIFY,
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
                Constant.UUID_CHARA_SPO2_NOTIFY,
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
     * 收到血压计bleDevice,先写数据
     */
    private void writeBPBleDevice(BleDevice bleDevice) {
        BleManager.getInstance().write(
                bleDevice,
                Constant.UUID_SERVICE_BP,
                Constant.UUID_CHARA_BP_WRITE,
                ByteUtil.HexString2Bytes(Constant.START),
                new BleWriteCallback() {

                    @Override
                    public void onWriteSuccess(int current, int total, byte[] justWrite) {
                        Log.d(TAG,"current:"+current+"==total:"+total+"===byte[]:"+ Arrays.toString(justWrite));
                        getBPBleDevice(bleDevice);
                    }

                    @Override
                    public void onWriteFailure(BleException exception) {
                        Log.d(TAG,exception.toString());
                    }
                }

        );
    }

    /**
     * 血压计bleDevice,写完数据成功，接收数据
     */

    private void getBPBleDevice(BleDevice bleDevice){
        BleManager.getInstance().notify(
                bleDevice,
                Constant.UUID_SERVICE_BP,
                Constant.UUID_CHARA_BP_NOTIFY,
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
