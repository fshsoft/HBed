package com.java.health.care.bed.fragment;

import com.java.health.care.bed.R;
import com.java.health.care.bed.base.BaseFragment;
import com.java.health.care.bed.bean.Prescription;
import com.java.health.care.bed.contract.prescription.Contract;
import com.java.health.care.bed.present.PrescriptionPresenter;

import java.util.List;

/**
 * @author fsh
 * @date 2022/08/01 14:16
 * @Description 未完成处方列表
 */
public class PrescriptionNoFragment extends BaseFragment<Contract.IPrescriptionView, PrescriptionPresenter>
        implements Contract.IPrescriptionView {

    @Override
    protected int getContentViewId() {
        return R.layout.fragment_no;
    }

    @Override
    protected void init() {

    }

    @Override
    protected PrescriptionPresenter createPresenter() {
        return null;
    }


    @Override
    public void loadNotFinishedPrescription(List<Prescription> list) {

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
