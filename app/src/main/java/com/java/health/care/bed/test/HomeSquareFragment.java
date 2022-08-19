package com.java.health.care.bed.test;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseMVPFragment;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author fsh
 * @date 2022/08/19 13:17
 * @Description
 */
public class HomeSquareFragment extends BaseMVPFragment<HomeSquarePresenter> implements Contracts.IHomeSquareView, OnRefreshListener, OnLoadMoreListener {

    private Context mContext;

    private HomeSquareAdapter mHomeSquareAdapter;

    private int mCurrentPage = 0;

    private List<Article> mHomeSquareList = new ArrayList<>();

    @BindView(R.id.article_recycler)
    RecyclerView mRecyclerView;

    @BindView(R.id.refresh_layout)
    SmartRefreshLayout mSmartRefreshLayout;

    @BindView(R.id.layout_error)
    ViewGroup mLayoutError;
    @Override
    protected int getContentViewId() {
        return R.layout.homesquare_fragment;
    }

    @Override
    protected void init() {
        mContext = getContext().getApplicationContext();
        initAdapter();
        mPresenter.loadHomeSquareData(mCurrentPage);
        mSmartRefreshLayout.setOnRefreshListener(this);
        mSmartRefreshLayout.setOnLoadMoreListener(this);

    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mHomeSquareAdapter = new HomeSquareAdapter(mContext, mHomeSquareList);
        mHomeSquareAdapter.setItemOnClickListener(new HomeSquareAdapter.ItemOnClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                ToastUtils.showShort("点击了第"+position+"下标");
            }
        });
        mRecyclerView.setAdapter(mHomeSquareAdapter);
    }

    @Override
    protected HomeSquarePresenter createPresenter() {
        return new HomeSquarePresenter();
    }

    @Override
    public void loadHomeSquareData(List<Article> homeSquareData) {
        if (mCurrentPage == 0) {
            mHomeSquareList.clear();
        }
        mHomeSquareList.addAll(homeSquareData);
        mHomeSquareAdapter.setHomeSquareList(mHomeSquareList);
    }

    @Override
    public void refreshHomeSquareData(List<Article> homeSquareData) {
        mHomeSquareList.clear();
        mHomeSquareList.addAll(0, homeSquareData);
        mHomeSquareAdapter.setHomeSquareList(mHomeSquareList);
    }

    private void setNetWorkError(boolean isSuccess) {
        if (isSuccess) {
            mSmartRefreshLayout.setVisibility(View.VISIBLE);
            mLayoutError.setVisibility(View.GONE);
        } else {
            mSmartRefreshLayout.setVisibility(View.GONE);
            mLayoutError.setVisibility(View.VISIBLE);
        }
    }


    @OnClick(R.id.layout_error)
    public void onReTry() {
        setNetWorkError(true);
        mPresenter.loadHomeSquareData(0);
    }

    @Override
    public void onLoading() {
        super.onLoading();
    }

    @Override
    public void onLoadSuccess() {
        super.onLoadSuccess();
        setNetWorkError(true);
        mSmartRefreshLayout.finishRefresh();
        mSmartRefreshLayout.finishLoadMore();
    }

    @Override
    public void onLoadFailed() {
        super.onLoadFailed();
        setNetWorkError(false);
        ToastUtils.showShort("网络未连接请重试");
        mSmartRefreshLayout.finishRefresh(false);
        mSmartRefreshLayout.finishLoadMore(false);
    }


    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mCurrentPage = 0;
        mPresenter.refreshHomeSquareData(mCurrentPage);
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        mCurrentPage++;
        mPresenter.loadHomeSquareData(mCurrentPage);
    }

}
