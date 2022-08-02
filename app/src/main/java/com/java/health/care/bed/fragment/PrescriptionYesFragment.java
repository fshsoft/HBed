package com.java.health.care.bed.fragment;

import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseFragment;
import com.java.health.care.bed.bean.Prescription;
import com.java.health.care.bed.contract.prescription.Contract2;
import com.java.health.care.bed.present.PrescriptionPresenter2;

import java.util.List;

/**
 * @author fsh
 * @date 2022/08/01 14:16
 * @Description 已完成处方列表
 */
public class PrescriptionYesFragment extends BaseFragment<Contract2.IPrescriptionView, PrescriptionPresenter2>
        implements Contract2.IPrescriptionView {

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_yes;
    }

    @Override
    protected void init() {

    }

    @Override
    protected PrescriptionPresenter2 createPresenter() {
        return null;
    }


    @Override
    public void loadFinishedPrescription(List<Prescription> list) {

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
