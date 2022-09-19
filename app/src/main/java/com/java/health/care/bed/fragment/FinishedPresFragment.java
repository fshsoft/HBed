package com.java.health.care.bed.fragment;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.R;
import com.java.health.care.bed.adapter.FinishedPresAdapter;
import com.java.health.care.bed.base.BaseFragment;
import com.java.health.care.bed.bean.FinishedPres;
import com.java.health.care.bed.bean.Prescription;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.presenter.MainPresenter;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.List;

import butterknife.BindView;


/**
 * @author fsh
 * @date 2022/08/01 14:16
 * @Description 已完成处方列表
 */
public class FinishedPresFragment extends BaseFragment implements MainContract.View {
    private MainPresenter mainPresenter;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    List<FinishedPres> finishedPresList;
    private FinishedPresAdapter finishedPresAdapter;
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_finished;
    }

    @Override
    protected void initView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
    }

    @Override
    protected void initData() {
        mainPresenter = new MainPresenter(getActivity(), this);
        int patientID = SPUtils.getInstance().getInt(SP.PATIENT_ID);
        if(patientID!=0){
            //调用接口
            mainPresenter.getPrescription(1);
        }
        getFinishedPres();
    }
    private void getFinishedPres(){
        refreshLayout.setEnableAutoLoadMore(true);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                refreshLayout.getLayout().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int patientID = SPUtils.getInstance().getInt(SP.PATIENT_ID);
                        if(patientID!=0){
                            //调用接口
                            mainPresenter.getPrescription(1);
                            refreshLayout.finishRefresh();
                        }
                    }
                },0);
            }
        });

        //触发自动刷新
//        refreshLayout.autoRefresh();

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
        Prescription pres = (Prescription) obj;
        if(pres!=null){
            finishedPresList = pres.getFinished();
            finishedPresAdapter = new FinishedPresAdapter(getActivity(),finishedPresList);
            recyclerView.setAdapter(finishedPresAdapter);
        }
    }

    @Override
    public void setData(Object obj) {

    }
}
