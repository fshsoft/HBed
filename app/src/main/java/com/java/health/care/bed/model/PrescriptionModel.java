package com.java.health.care.bed.model;

import com.java.health.care.bed.base.BaseModel;
import com.java.health.care.bed.bean.Prescription;
import com.java.health.care.bed.contract.prescription.Contract;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author fsh
 * @date 2022/07/29 15:16
 * @Description
 */
public class PrescriptionModel extends BaseModel implements Contract.IPrescriptionModel {


    @Override
    public Observable<List<Prescription>> loadNotFinishedPrescription() {
        return null;
    }

}
