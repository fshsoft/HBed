package com.java.health.care.bed.activity;

import android.bluetooth.BluetoothGatt;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.service.DataReaderService;
import com.java.health.care.bed.model.DataReceiver;
import com.java.health.care.bed.model.DataTransmitter;
import com.java.health.care.bed.model.DevicePacket;
import com.java.health.care.bed.model.EstimateRet;
import com.java.health.care.bed.util.ImageLoadUtils;
import com.java.health.care.bed.widget.MyEcgView;
import com.java.health.care.bed.widget.TagValueTextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

/**
 * @author fsh
 * @date 2022/08/11 09:20
 * @Description 评估界面
 */
public class AssessActivity extends BaseActivity implements DataReceiver {
    @BindView(R.id.assess_user)
    TextView assess_user;
    @BindView(R.id.assess_id)
    TextView assess_id;
    @BindView(R.id.assess_hxl)
    TextView assess_hxl;
    @BindView(R.id.assess_hxb)
    TextView assess_hxb;
    @BindView(R.id.assess_ll)
    LinearLayout assess_ll;
    @BindView(R.id.assess_monitor_battery)
    ImageView assess_monitor_battery;

    private static final String TAG = AssessActivity.class.getSimpleName();
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
    private MyEcgView myEcgView;
    private MyEcgView myRespView;
    private TagValueTextView tvBreatheTime;//呼吸时间
    private boolean startDraw = false;
    private String bleDeviceMac;
    List<BleDevice> deviceListConnect = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_assess;
    }

    @Override
    protected void initView() {
        addConnectDeviceView();
        goService(DataReaderService.class);
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
    }

    /**
     * 开始前的状态
     */
    private void addConnectDeviceView() {
        connectDeviceView = LayoutInflater.from(this).inflate(R.layout.patient_view_access_init, null, false);
        ImageView imageView = connectDeviceView.findViewById(R.id.patient_breathe_iv);
        tvBreatheTime = connectDeviceView.findViewById(R.id.patient_access_tv_breathe_time);
        tvConnectDevice = connectDeviceView.findViewById(R.id.patient_access_connect_device);
        tvBreatheTime.setTag(" min");
        tvBreatheTime.setValue(Integer.valueOf("50"));
        ImageLoadUtils.loadGifToImg(this, R.drawable.a369, imageView);
        tvConnectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始，连接cm19设备
//
                scanBle();
                addBreathView();

            }
        });
        assess_ll.removeAllViews();
        assess_ll.addView(connectDeviceView);
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
     * 蓝牙设备连接成功之后，开始后的状态
     */
    private void addBreathView() {
        breatheView = LayoutInflater.from(this).inflate(R.layout.patient_view_breath, null, false);
        testRlBreath = breatheView.findViewById(R.id.patient_ll_disconnect);
        ivBreathe = breatheView.findViewById(R.id.patient_view_breathe);
        myEcgView = breatheView.findViewById(R.id.patient_view_signal);
        myRespView = breatheView.findViewById(R.id.patient_view_resp);
        myRespView.setMaxViewDatalen(5000);
        myRespView.setRespColor();
        ImageLoadUtils.loadGifToImg(this, R.drawable.a369, ivBreathe);
        breatheType = breatheView.findViewById(R.id.patient_view_breathe_type);
        breatheType.setText("patientInfoBean.getRespType()");
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

        myEcgView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //重绘完毕
                startDraw = false;
            }
        });
        myRespView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //重绘完毕
                startDraw = false;
            }
        });
        breatheRate = breatheView.findViewById(R.id.patient_view_tv_breathe);
        tvHeartRate = breatheView.findViewById(R.id.patient_view_tv_hart_rate);
        tvBreathScore = breatheView.findViewById(R.id.patient_view_tv_breathe_score);
        //比如心率值为 23 那么加载数据如下
        tvHeartRate.setText(getString(R.string.patient_heart_rate_value) + "-bpm");
        //添加之前必须先移除里面的所有view
        assess_ll.removeAllViews();
        assess_ll.addView(breatheView);
    }

    private void refreshEcgData() {
        if (!startDraw) {
            startDraw = true;
            myEcgView.refreshView();
            myRespView.refreshView();
        } else {
            startDraw = false;
        }
    }

    /**
     * 数据接收
     *
     * @param packet
     */
    @Override
    public void onDataReceived(DevicePacket packet) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                short[] ecgData = packet.secgdata;
                Log.d(TAG, Arrays.toString(ecgData));
                for (int i = 0; i < ecgData.length; i++) {
                    if (null != myEcgView) {
                        myEcgView.addOneData((int) ecgData[i]/* & 0x00ff*/, -200, 200);
                        myRespView.addOneData((int) packet.irspData[i]/* & 0x00ff*/, -200, 200);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshEcgData();
                            }
                        });
                    }
                }
            }
        }).start();
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
}
