package com.java.health.care.bed.activity;

import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.blankj.utilcode.util.ToastUtils;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.fragment.PrescriptionNoFragment;
import com.java.health.care.bed.fragment.PrescriptionYesFragment;
import com.java.health.care.bed.test.ParentSquareActivity;
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
public class PrescriptionActivity2 extends BaseActivity {
    @BindView(R.id.pre_yes)
    TextView pre_yes;
    @BindView(R.id.pre_no)
    TextView pre_no;

    private boolean onClickYes;
    private boolean onClickNo;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private String mCurrentFragmentTag = PrescriptionActivity2.class.getSimpleName();

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
        goActivity(ParentSquareActivity.class);
    }

    @OnClick(R.id.pre_no)
    public void onClickNo(){
        onClickNo = true;
        onClickYes = false;
        if(onClickYes){
            pre_no.setBackground(getResources().getDrawable(R.drawable.tab_yes));
            pre_yes.setBackground(getResources().getDrawable(R.drawable.tab_no));
            ToastUtils.showShort("点击了no");
        }

    }

    @OnClick(R.id.pre_yes)
    public void onClickYes(){
        onClickYes = true;
        onClickNo = false;
        pre_yes.setBackground(getResources().getDrawable(R.drawable.tab_yes));
        pre_no.setBackground(getResources().getDrawable(R.drawable.tab_no));
        ToastUtils.showShort("点击了yes");

    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_prescription2;
    }

    @Override
    protected void initView() {
        PrescriptionNoFragment noFragment = new PrescriptionNoFragment();
        PrescriptionYesFragment yesFragment = new PrescriptionYesFragment();

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
                                Toast.makeText(PrescriptionActivity2.this, "您拒绝了如下权限"+deniedList, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }

    }

    /**
     * 向FragmentManager中添加Fragment
     *
     * @param fragment 要添加的Fragment
     * @param tag      给Fragment绑定的Tag
     */
    private void addFragment(Fragment fragment, String tag) {
        if (fragment.isAdded()) {
            return;
        }
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.main_content, fragment, tag);
        fragmentTransaction.hide(fragment);
        fragmentTransaction.commit();
    }

    /**
     * 切换Tab
     *
     * @param fragment Fragment的实例
     * @param tag      要绑定的Tag
     */
    private void changeTabContent(Fragment fragment, String tag) {

        fragmentTransaction = fragmentManager.beginTransaction();
        if (getCurrentFragment() != null) {
            getCurrentFragment().onPause();
            fragmentTransaction.hide(getCurrentFragment());
        }
        fragment.onResume();
        fragmentTransaction.show(fragment);
        fragmentTransaction.commitAllowingStateLoss();
        mCurrentFragmentTag = tag;
    }

    /**
     * 获取当前正在显示的Fragment
     *
     * @return Fragment 当前正在显示的Fragment
     */
    private Fragment getCurrentFragment() {
        return fragmentManager.findFragmentByTag(mCurrentFragmentTag);
    }
}
