package com.java.health.care.bed.activity;

import android.Manifest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.fragment.PrescriptionNoFragment;
import com.java.health.care.bed.fragment.PrescriptionYesFragment;
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
public class PrescriptionActivity extends BaseActivity {

    @BindView(R.id.prescription_tab)
    TabLayout mTabLayout;

    @BindView(R.id.prescription_viewpager)
    ViewPager2 mViewPager;

    private ArrayList<Fragment> mFragmentSparseArray = new ArrayList<>();

    private String[] titles = {"未完成","已完成"};



    @OnClick(R.id.prescription_tv_set)
    public void onClickSet(){
          goActivity(InputPassWordActivity.class);
    }

    @OnClick(R.id.prescription_user)
    public void onClickUser(){
        goActivity(AssessActivity.class);
    }

    @OnClick(R.id.prescription_ch)
    public void onClickCh(){
            goActivity(EcgsActivity.class);
//        goActivity(SoundWaveActivity.class);
//        goActivity(VitalSignsActivity.class);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_prescription;
    }

    @Override
    protected void initView() {
        PrescriptionNoFragment noFragment = new PrescriptionNoFragment();
        PrescriptionYesFragment yesFragment = new PrescriptionYesFragment();
        mFragmentSparseArray.add(noFragment);
        mFragmentSparseArray.add(yesFragment);

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
}
