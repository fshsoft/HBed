package com.java.health.care.bed.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;
import com.java.health.care.bed.bean.User;
import com.java.health.care.bed.contract.user.Contract;
import com.java.health.care.bed.fragment.PrescriptionNoFragment;
import com.java.health.care.bed.fragment.PrescriptionYesFragment;

import com.java.health.care.bed.present.UserPresenter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author fsh
 * @date 2022/07/29 14:08
 * @Description 我的处方,获取User信息，跳转未完成和已完成界面fragment
 */
public class PrescriptionActivity extends BaseActivity<Contract.IUserView, UserPresenter>
        implements Contract.IUserView {

    @BindView(R.id.prescription_tab)
    TabLayout mTabLayout;

    @BindView(R.id.prescription_viewpager)
    ViewPager2 mViewPager;

    private ArrayList<Fragment> mFragmentSparseArray = new ArrayList<>();

    private String[] titles = {"未完成","已完成"};



    @OnClick(R.id.prescription_tv_set)
    public void onClickSet(){
          startActivity(new Intent(PrescriptionActivity.this,InputPassWordActivity.class));
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_prescription;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
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
            }
        });
        //这句话很重要
        tabLayoutMediator.attach();


    }

    @Override
    protected UserPresenter createPresenter() {
        return null;
    }


    @Override
    public void refreshUser(User user) {

    }

    @Override
    public void onLoading() {

    }

    @Override
    public void onLoadFailed() {

    }

    @Override
    public void onLoadSuccess() {

    }
}
