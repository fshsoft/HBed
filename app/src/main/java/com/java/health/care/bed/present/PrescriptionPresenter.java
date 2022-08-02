package com.java.health.care.bed.present;

import com.java.health.care.bed.base.BasePresenter;
import com.java.health.care.bed.contract.prescription.Contract;
import com.java.health.care.bed.model.PrescriptionModel;

/**
 * @author fsh
 * @date 2022/07/29 15:12
 * @Description
 */
public class PrescriptionPresenter extends BasePresenter<Contract.IPrescriptionView>
        implements Contract.IPrescriptionPresenter {

    Contract.IPrescriptionModel iPrescriptionModel;
    public PrescriptionPresenter(){
        iPrescriptionModel = new PrescriptionModel();
    }


    @Override
    public void loadNotFinishedPrescription() {

    }


}
