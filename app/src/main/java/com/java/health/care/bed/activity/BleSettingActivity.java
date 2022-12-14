package com.java.health.care.bed.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.ble.adapter.DeviceAdapter;
import com.java.health.care.bed.ble.comm.ObserverManager;
import com.java.health.care.bed.ble.operation.OperationActivity;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.service.DataReaderService;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fsh
 * @date 2022/08/02 13:55
 * @Description
 */
public class BleSettingActivity extends BaseActivity implements View.OnClickListener{

    private Button btn_scan,btn_see_vital,btn_see_kyc;
    private TextView back;
    private ImageView img_loading;

    private Animation operatingAnim;
    private DeviceAdapter mDeviceAdapter;
    private ProgressDialog progressDialog;
    List<BleDevice> deviceListConnect = new ArrayList<>();

    private static final String TAG = BleSettingActivity.class.getSimpleName();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ble_set;
    }

    @Override
    protected void onResume() {
        super.onResume();
        showConnectedDevice();

        Log.d(TAG,"deviceList==="+deviceListConnect.size());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();
        if(null!=deviceListConnect){
            deviceListConnect.clear();
            deviceListConnect=null;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (btn_scan.getText().equals(getString(R.string.start_scan))) {
                    checkPermissions();
                } else if (btn_scan.getText().equals(getString(R.string.stop_scan))) {
                    BleManager.getInstance().cancelScan();
                }
                break;
            case R.id.btn_see_kyc: //???????????????
                if(deviceListConnect.size()!=0){
                    goActivity(BLESeeAndOperateActivity.class);
                }else {
                    ToastUtils.showShort( R.string.please_connect);
                }
                break;
            case R.id.back:
                finish();
            default:
                break;
        }
    }

    @Override
    protected void initView() {
        goService(DataReaderService.class);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_scan.setText(getString(R.string.start_scan));
        btn_scan.setOnClickListener(this);

        btn_see_kyc = findViewById(R.id.btn_see_kyc);
        btn_see_kyc.setOnClickListener(this);

        back = findViewById(R.id.back);
        back.setOnClickListener(this);

        img_loading = (ImageView) findViewById(R.id.img_loading);
        operatingAnim = AnimationUtils.loadAnimation(this, R.anim.rotate);
        operatingAnim.setInterpolator(new LinearInterpolator());
        progressDialog = new ProgressDialog(this);

        mDeviceAdapter = new DeviceAdapter(this);
        mDeviceAdapter.setOnDeviceClickListener(new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (!BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().cancelScan();
                    connect(bleDevice);
                }
            }

            @Override
            public void onDisConnect(final BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    BleManager.getInstance().disconnect(bleDevice);
                }
            }

            @Override
            public void onDetail(BleDevice bleDevice) {
                if (BleManager.getInstance().isConnected(bleDevice)) {
                    Intent intent = new Intent(BleSettingActivity.this, OperationActivity.class);
                    intent.putExtra(OperationActivity.KEY_DATA, bleDevice);
                    startActivity(intent);
                }
            }

        });
        ListView listView_device = (ListView) findViewById(R.id.list_device);
        listView_device.setAdapter(mDeviceAdapter);
    }

    @Override
    protected void initData() {
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);
    }


    private void showConnectedDevice() {
        List<BleDevice> deviceList = BleManager.getInstance().getAllConnectedDevice();
        mDeviceAdapter.clearConnectedDevice();
        for (BleDevice bleDevice : deviceList) {
            mDeviceAdapter.addDevice(bleDevice);
        }
        mDeviceAdapter.notifyDataSetChanged();
    }
    private void setScanNameRule(){
        String[] names ={Constant.CM19,Constant.CM22,Constant.SPO2,Constant.QIANSHAN,Constant.IRT,Constant.KANGYANGCHUANG};
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                // ??????????????????????????????????????????
                .setDeviceName(true, names)
                // ????????????????????????????????????10???
                .setScanTimeOut(10000)
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }


    private void startScan() {
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                mDeviceAdapter.clearScanDevice();
                mDeviceAdapter.notifyDataSetChanged();
                img_loading.startAnimation(operatingAnim);
                img_loading.setVisibility(View.VISIBLE);
                btn_scan.setText(getString(R.string.stop_scan));
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
            }
        });
    }

    private void connect(final BleDevice bleDevice) {
        BleManager.getInstance().connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                progressDialog.show();
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                img_loading.clearAnimation();
                img_loading.setVisibility(View.INVISIBLE);
                btn_scan.setText(getString(R.string.start_scan));
                progressDialog.dismiss();
                Toast.makeText(BleSettingActivity.this, getString(R.string.connect_fail), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                deviceListConnect.add(bleDevice);
                //???????????????????????????????????????BleDevice???mac
                if(bleDevice.getName().contains(Constant.CM19)){
                    SPUtils.getInstance().put(Constant.BLE_DEVICE_CM19_MAC,bleDevice.getMac());
                } else if (bleDevice.getName().contains(Constant.SPO2)) {
                    SPUtils.getInstance().put(Constant.BLE_DEVICE_SPO2_MAC,bleDevice.getMac());
                }else if(bleDevice.getName().contains(Constant.QIANSHAN)){
                    SPUtils.getInstance().put(Constant.BLE_DEVICE_QIANSHAN_MAC,bleDevice.getMac());
                }else if(bleDevice.getName().contains(Constant.IRT)){
                    SPUtils.getInstance().put(Constant.BLE_DEVICE_IRT_MAC,bleDevice.getMac());
                }else if(bleDevice.getName().contains(Constant.KANGYANGCHUANG)){
                    SPUtils.getInstance().put(Constant.BLE_DEVICE_KYC_MAC,bleDevice.getMac());
                }else if(bleDevice.getName().contains(Constant.CM22)){
                    SPUtils.getInstance().put(Constant.BLE_DEVICE_CM22_MAC,bleDevice.getMac());
                }
                Log.d(TAG,"deviceList1==="+deviceListConnect.size());
                if(deviceListConnect.size()!=0) {
                    EventBus.getDefault().post(deviceListConnect);
                }

            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                progressDialog.dismiss();

                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();

                if (isActiveDisConnected) {
                    Toast.makeText(BleSettingActivity.this, getString(R.string.active_disconnected), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(BleSettingActivity.this, getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
                    ObserverManager.getInstance().notifyObserver(bleDevice);
                }
                if(null!=deviceListConnect){
                    if(deviceListConnect.size()!=0){
                        deviceListConnect.remove(bleDevice);
                    }
                }


            }
        });
    }


    /**
     * ????????????
     */

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, getString(R.string.please_open_blue), Toast.LENGTH_SHORT).show();
            return;
        }

        List requestList = new ArrayList();
        //android12?????????????????????????????????????????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestList.add(Manifest.permission.BLUETOOTH_SCAN);
            requestList.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            requestList.add(Manifest.permission.BLUETOOTH_CONNECT);
        }else {
            requestList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(!requestList.isEmpty()){
            PermissionX.init(this)
                    .permissions(requestList)
                    .onExplainRequestReason(new ExplainReasonCallback() {
                        @Override
                        public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {
                            scope.showRequestReasonDialog(deniedList,"?????????????????????????????????????????????","??????","??????");
                        }
                    })
                    .request(new RequestCallback() {
                        @Override
                        public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                            if (allGranted) {
                                setScanNameRule();
                                startScan();
//
                            } else {
                                Toast.makeText(BleSettingActivity.this, "????????????????????????"+deniedList, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }

    }

}
