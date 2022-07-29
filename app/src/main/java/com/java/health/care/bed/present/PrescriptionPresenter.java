package com.java.health.care.bed.present;

import com.java.fsh.soft.common.base.BasePresenter;
import com.java.health.care.bed.contract.prescription.Contract;

/**
 * @author fsh
 * @date 2022/07/29 15:12
 * @Description
 */
public class PrescriptionPresenter extends BasePresenter<Contract.IMyPrescriptionView> implements Contract.IMyPrescriptionPresenter {

    Contract.IMyPrescriptionModel iMyPrescriptionModel;
    public PrescriptionPresenter(){
//        iMyPrescriptionModel = new M
    }
    @Override
    public void loadFinishedPrescription() {

    }

    @Override
    public void loadNotFinishedPrescription() {

    }

    @Override
    public void refreshUser() {

    }
}
