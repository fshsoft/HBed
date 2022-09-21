package com.java.health.care.bed.activity;

import android.Manifest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.blankj.utilcode.util.SPUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.bean.Patient;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.fragment.UnFinishedPresFragment;
import com.java.health.care.bed.fragment.FinishedPresFragment;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.presenter.MainPresenter;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;

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
    private MainPresenter mainPresenter;
    private int bunkId;
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
        mainPresenter = new MainPresenter(this, this);
        bunkId = SPUtils.getInstance().getInt(SP.BUNK_ID);
        if(bunkId!=0){
            mainPresenter.getUser(bunkId);
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

        }
    }

    @Override
    public void setData(Object obj) {

    }
}
