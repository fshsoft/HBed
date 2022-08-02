package com.java.health.care.bed.model;

import com.java.health.care.bed.base.BaseModel;
import com.java.health.care.bed.bean.Prescription;
import com.java.health.care.bed.contract.prescription.Contract2;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author fsh
 * @date 2022/07/29 15:16
 * @Description
 */
public class PrescriptionModel2 extends BaseModel implements Contract2.IPrescriptionModel {
    @Override
    public Observable<List<Prescription>> loadFinishedPrescription() {
        return null;
    }


}
