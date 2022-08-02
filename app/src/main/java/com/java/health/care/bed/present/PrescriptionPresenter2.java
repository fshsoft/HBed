package com.java.health.care.bed.present;

import com.java.health.care.bed.base.BasePresenter;
import com.java.health.care.bed.contract.prescription.Contract2;
import com.java.health.care.bed.model.PrescriptionModel2;

/**
 * @author fsh
 * @date 2022/07/29 15:12
 * @Description
 */
public class PrescriptionPresenter2 extends BasePresenter<Contract2.IPrescriptionView>
        implements Contract2.IPrescriptionPresenter {

    Contract2.IPrescriptionModel iPrescriptionModel;
    public PrescriptionPresenter2(){
        iPrescriptionModel = new PrescriptionModel2();
    }
    @Override
    public void loadFinishedPrescription() {

    }


}
