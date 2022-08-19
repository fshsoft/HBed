package com.java.health.care.bed.test;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.blankj.utilcode.util.Utils;
import com.flyco.tablayout.SlidingTabLayout;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author fsh
 * @date 2022/08/19 09:40
 * @Description
 */
public class ParentSquareActivity extends BaseActivity {

    @BindView(R.id.square_tab)
    SlidingTabLayout mSlidingTabLayout;

    @BindView(R.id.square_divider)
    View mDivider;

    @BindView(R.id.square_viewpager)
    ViewPager mViewPager;

    private int mCurTab;

    private Context mContext;

    private List<String> mTabNames = new ArrayList<>();

    private ArrayList<Fragment> mFragmentSparseArray = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_parentsquare;

    }

    @Override
    protected void initView() {
        mContext = this.getApplicationContext();
        setChildViewVisibility(View.VISIBLE);
        mCurTab = 0;
        initFragment();
        initViewPager();
        initTabColor();
    }
    private void initTabColor() {
        mSlidingTabLayout.setIndicatorColor(R.color.text_theme);
        mSlidingTabLayout.setDividerColor(R.color.primary_red);
    }


    @Override
    protected void initData() {
    }



    private void setChildViewVisibility(int visibility) {
        mSlidingTabLayout.setVisibility(visibility);
        mDivider.setVisibility(visibility);
        mViewPager.setVisibility(visibility);
    }

    private void initFragment() {
        mTabNames.add(0, "广场");
        mTabNames.add(1, "体系");
        mTabNames.add(2, "导航");
        mFragmentSparseArray.add(0, new HomeSquareFragment());
        mFragmentSparseArray.add(1, new HomeSquareFragment());
        mFragmentSparseArray.add(2, new HomeSquareFragment());

    }


    private void initViewPager() {
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return mFragmentSparseArray.get(position);
            }

            @Override
            public int getCount() {
                return mFragmentSparseArray == null ? 0 : mFragmentSparseArray.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return mTabNames.get(position);
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurTab = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mSlidingTabLayout.setViewPager(mViewPager);
    }

}
