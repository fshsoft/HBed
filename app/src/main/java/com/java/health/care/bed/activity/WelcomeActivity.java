package com.java.health.care.bed.activity;

import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.SPUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.service.DataReaderService;

import java.util.List;


/**
 * @author fsh
 * @date 2022/08/02 13:58
 * @Description
 */
public class WelcomeActivity extends AppCompatActivity {
    private String ip,dept,region,bunk;
    private String bleDeviceMac;
    private String cm19Mac;
    private String cm22Mac;
    private String spO2Mac;
    private String bpMac;
    private String tempMac;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
        bleDeviceMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_KYC_MAC);
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
        //开启服务，保持康养床的蓝牙连接
        startService(new Intent(WelcomeActivity.this,DataReaderService.class));
        //自动先进行扫描康养床ble,然后进行连接，前提需要在设置里面先连接过康养床设备获取mac地址
        scanBle();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //判断是否有IP地址，是否有科室，病区，床号
                ip = SPUtils.getInstance().getString(SP.IP_SERVER_ADDRESS);
                dept = SPUtils.getInstance().getString(SP.DEPT_NUM);
                region = SPUtils.getInstance().getString(SP.REGION_NUM);
                bunk = SPUtils.getInstance().getString(SP.BUNK_NUM);

                //蓝牙设置
                cm19Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_CM19_MAC);
                cm22Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_CM22_MAC);
                spO2Mac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_SPO2_MAC);
                bpMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_QIANSHAN_MAC);
                tempMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_IRT_MAC);

                if(ip.isEmpty() || dept.isEmpty() || region.isEmpty() || bunk.isEmpty()
                        || bleDeviceMac.isEmpty() || cm19Mac.isEmpty() || cm22Mac.isEmpty() || spO2Mac.isEmpty()
                || bpMac.isEmpty() || tempMac.isEmpty()){
                    startActivity(new Intent(WelcomeActivity.this,SettingActivity.class));
                }else {

                    startActivity(new Intent(WelcomeActivity.this,PrescriptionActivity.class));
                }

                finish();
            }
        },3000);

    }
    private void scanBle() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {


            }

            @Override
            public void onScanStarted(boolean success) {
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

            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {

            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
            }
        });
    }
}
