package com.java.health.care.bed.contract.prescription;

import com.java.health.care.bed.base.IBaseView;
import com.java.health.care.bed.bean.Prescription;
import java.util.List;

import io.reactivex.Observable;

/**
 * @author fsh
 * @date 2022/07/29 14:12
 * @Description  未完成处方
 */
public class Contract {

    public interface IPrescriptionModel{


        /**
         * 获取未完成处方
         * @return 接口数据
         */
        Observable<List<Prescription>> loadNotFinishedPrescription();


    }



    public interface IPrescriptionView extends IBaseView {

        /**
         * 获取未完成处方
         * @param list 数据显示
         */
        void loadNotFinishedPrescription(List<Prescription> list);


    }



    public interface IPrescriptionPresenter{

        /**
         * 获取未完成处方
         * @param
         */
        void loadNotFinishedPrescription();


    }
}
