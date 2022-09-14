package com.java.health.care.bed.fragment;

import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseFragment;
import com.java.health.care.bed.bean.FinishedPres;
import com.java.health.care.bed.bean.Prescription;
import com.java.health.care.bed.bean.UnFinishedPres;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.presenter.MainPresenter;

import java.util.List;


/**
 * @author fsh
 * @date 2022/08/01 14:16
 * @Description 已完成处方列表
 */
public class FinishedPresFragment extends BaseFragment implements MainContract.View {
    private MainPresenter mainPresenter;
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_yes;
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
        mainPresenter = new MainPresenter(getActivity(), this);
        int patientID = SPUtils.getInstance().getInt(SP.PATIENT_ID);
        if(patientID!=0){
            mainPresenter.getPrescription(1);
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

    }

    @Override
    public void setData(Object obj) {
        Prescription pres = (Prescription) obj;
        if(pres!=null){
            List<FinishedPres> finishedPresList = pres.getFinishedPresList();
            if(finishedPresList!=null){
                for(int i=0;i<finishedPresList.size();i++){
                    Log.d("UnFinishedPresFragment","type==="+finishedPresList.get(i).getPreType());

                }
            }
        }
    }
}
