package com.java.health.care.bed.activity;

import android.os.Bundle;

import com.java.fsh.soft.common.base.BaseActivity;
import com.java.health.care.bed.R;
import com.java.health.care.bed.bean.Prescription;
import com.java.health.care.bed.bean.User;
import com.java.health.care.bed.contract.prescription.Contract.IMyPrescriptionView;
import com.java.health.care.bed.present.PrescriptionPresenter;

import java.util.List;

/**
 * @author fsh
 * @date 2022/07/29 14:08
 * @Description 我的处方
 */
public class PrescriptionActivity extends BaseActivity<IMyPrescriptionView, PrescriptionPresenter> implements IMyPrescriptionView {

    @Override
    protected int getContentViewId() {
        return R.layout.activity_prescription;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected PrescriptionPresenter createPresenter() {
        return new PrescriptionPresenter();
    }
    @Override
    public void loadFinishedPrescription(List<Prescription> list) {

    }

    @Override
    public void loadNotFinishedPrescription(List<Prescription> list) {

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
