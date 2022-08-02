package com.java.health.care.bed.contract.prescription;

import com.java.health.care.bed.base.IBaseView;
import com.java.health.care.bed.bean.Prescription;
import com.java.health.care.bed.bean.User;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author fsh
 * @date 2022/07/29 14:12
 * @Description  已完成处方
 */
public class Contract2 {

    public interface IPrescriptionModel{
        /**
         * 获取已完成处方
         * @return 接口数据
         */

        Observable<List<Prescription>> loadFinishedPrescription();


    }



    public interface IPrescriptionView extends IBaseView {
        /**
         * 获取已完成处方
         * @param list 数据显示
         */
        void loadFinishedPrescription(List<Prescription> list);

    }



    public interface IPrescriptionPresenter{
        /**
         * 获取已完成处方
         * @param
         */
        void loadFinishedPrescription();

    }
}
