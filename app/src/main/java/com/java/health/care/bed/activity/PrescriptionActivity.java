package com.java.health.care.bed.activity;

import android.Manifest;
import android.bluetooth.BluetoothGatt;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ZipUtils;
import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.bean.Patient;
import com.java.health.care.bed.constant.Constant;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.fragment.UnFinishedPresFragment;
import com.java.health.care.bed.fragment.FinishedPresFragment;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.presenter.MainPresenter;
import com.java.health.care.bed.service.DataReaderService;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author fsh
 * @date 2022/07/29 14:08
 * @Description 我的处方,获取User信息，跳转未完成和已完成界面fragment
 */
public class PrescriptionActivity extends BaseActivity implements MainContract.View {
    private static final String TAG = PrescriptionActivity.class.getSimpleName();

    private MainPresenter mainPresenter;

    @BindView(R.id.prescription_tab)
    TabLayout mTabLayout;

    @BindView(R.id.prescription_viewpager)
    ViewPager2 mViewPager;

    @BindView(R.id.user_name)
    AppCompatTextView user_name;
    @BindView(R.id.user_sex)
    AppCompatTextView user_sex;
    @BindView(R.id.user_age)
    AppCompatTextView user_age;
    @BindView(R.id.user_bunk_num)
    AppCompatTextView user_bunk_num;

    private ArrayList<Fragment> mFragmentSparseArray = new ArrayList<>();

    private String[] titles = {"未完成","已完成"};

    private String bleDeviceMac;

    private int patientId;

    private int bunkId;

    private int regionId;

    @OnClick(R.id.prescription_tv_set)
    public void onClickSet(){
        goActivity(InputPassWordActivity.class);
    }

    @OnClick(R.id.user_name)
    public void onClickAssess(){
        goActivity(AssessActivity.class);
    }

    @OnClick(R.id.user_sex)
    public void onClickVital(){
        goActivity(VitalSignsActivity.class);
    }

    @OnClick(R.id.user_age)
    public void onClickSound(){
        goActivity(SoundWaveActivity.class);
    }


    @OnClick(R.id.user_bunk_num)
    public void onClickEcg(){
        goActivity(EcgsActivity.class);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_prescription;
    }

    @Override
    protected void initView() {
        UnFinishedPresFragment unFinishedPresFragment = new UnFinishedPresFragment();
        FinishedPresFragment finishedPresFragment = new FinishedPresFragment();
        mFragmentSparseArray.add(unFinishedPresFragment);
        mFragmentSparseArray.add(finishedPresFragment);

        mViewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return mFragmentSparseArray.get(position);
            }

            @Override
            public int getItemCount() {
                return mFragmentSparseArray.size();
            }
        });
        mViewPager.setOffscreenPageLimit(2);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(mTabLayout, mViewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        tab.setText(titles[position]);
                        tab.getCustomView();
                    }
                });
        //这句话很重要
        tabLayoutMediator.attach();
    }

    @Override
    protected void initData() {
        checkPermissions();
        EventBus.getDefault().register(this);
        mainPresenter = new MainPresenter(this, this);
        //床ID：bunkId
        bunkId = SPUtils.getInstance().getInt(SP.BUNK_ID);
        patientId = SPUtils.getInstance().getInt(SP.PATIENT_ID);
        regionId = SPUtils.getInstance().getInt(SP.REGION_ID);
//        int bunkNum = SPUtils.getInstance().getInt(SP.BUNK_NUM);
        if(bunkId!=0){
//            Log.d("getUser==bunkId",bunkId+"bunkNum:"+bunkNum);
            mainPresenter.getUser(bunkId);
        }

/*        bleDeviceMac = SPUtils.getInstance().getString(Constant.BLE_DEVICE_KYC_MAC);
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);


        //开启服务，保持康养床的蓝牙连接
        goService(DataReaderService.class);
        //自动先进行扫描康养床ble,然后进行连接，前提需要在设置里面先连接过康养床设备获取mac地址
        scanBle();*/



    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        if(event instanceof Integer){
            int num = (int) event;
            mainPresenter.sendMessage(regionId,bunkId,num,patientId);
        }
    }


    /**
     * 权限申请
     */

    private void checkPermissions() {

        List requestList = new ArrayList();
        //文件读写需要的三个权限
        requestList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        requestList.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);

        if(!requestList.isEmpty()){
            PermissionX.init(this)
                    .permissions(requestList)
                    .onExplainRequestReason(new ExplainReasonCallback() {
                        @Override
                        public void onExplainReason(@NonNull ExplainScope scope, @NonNull List<String> deniedList) {
                            scope.showRequestReasonDialog(deniedList,"需要您同意以下权限才能正常使用","同意","拒绝");
                        }
                    })
                    .request(new RequestCallback() {
                        @Override
                        public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                            if (allGranted) {
                            } else {
                                Toast.makeText(PrescriptionActivity.this, "您拒绝了如下权限"+deniedList, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }

    }

    @Override
    public void setCode(String code) {
        if(code.equals("200")){
        }
    }

    @Override
    public void setMsg(String msg) {

    }

    @Override
    public void setInfo(String msg) {

    }

    @Override
    public void setObj(Object obj) {
        Patient patient = (Patient) obj;
        if(patient!=null){
            user_name.setText("姓名："+patient.getPatientName());
            user_sex.setText("性别："+patient.getSex());
            user_age.setText("年龄："+patient.getAge());
            user_bunk_num.setText("床号："+patient.getBunkNo());
            SPUtils.getInstance().put(SP.PATIENT_ID,patient.getPatientId());
            SPUtils.getInstance().put(SP.PATIENT_NAME,patient.getPatientName());
            SPUtils.getInstance().put(SP.PATIENT_SEX,patient.getSex());
            SPUtils.getInstance().put(SP.PATIENT_AGE,patient.getAge());
            SPUtils.getInstance().put(SP.PATIENT_ARM_LENGTH,patient.getArmLength());
            SPUtils.getInstance().put(SP.PATIENT_WEIGHT,patient.getWeight());
            SPUtils.getInstance().put(SP.PATIENT_HEIGHT,patient.getHeight());

            SPUtils.getInstance().put(SP.HOSPITAL_ID,patient.getHospitalId());


        }
    }

    @Override
    public void setData(Object obj) {

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

                //todo
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onDisConnected:status:" + status);
            }
        });
    }
}
