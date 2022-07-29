package com.java.health.care.bed.contract.prescription;

import com.java.fsh.soft.common.interfaces.IBaseView;
import com.java.health.care.bed.bean.Prescription;
import com.java.health.care.bed.bean.User;

import java.util.List;

import io.reactivex.Observable;

/**
 * @author fsh
 * @date 2022/07/29 14:12
 * @Description
 */
public class Contract {

    public interface IMyPrescriptionModel{
        /**
         * 获取已完成处方
         * @return 接口数据
         */

        Observable<List<Prescription>> loadFinishedPrescription();



        /**
         * 获取未完成处方
         * @return 接口数据
         */
        Observable<List<Prescription>> loadNotFinishedPrescription();


        /**
         * 用户信息
         * @return 接口数据
         */

        Observable<User> refreshUser();
    }



    public interface IMyPrescriptionView extends IBaseView{
        /**
         * 获取已完成处方
         * @param list 数据显示
         */
        void loadFinishedPrescription(List<Prescription> list);
        /**
         * 获取未完成处方
         * @param list 数据显示
         */
        void loadNotFinishedPrescription(List<Prescription> list);

        /**
         * 用户信息
         * @param user 数据显示
         */
        void refreshUser(User user);
    }



    public interface IMyPrescriptionPresenter{
        /**
         * 获取已完成处方
         * @param
         */
        void loadFinishedPrescription();
        /**
         * 获取未完成处方
         * @param
         */
        void loadNotFinishedPrescription();

        /**
         * 用户信息
         * @param
         */
        void refreshUser();
    }
}
