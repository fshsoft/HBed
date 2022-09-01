package com.java.health.care.bed.module;

import com.java.health.care.bed.base.BasePresenter;
import com.java.health.care.bed.base.BaseView;

public interface MainContract {

    interface View  {
        void setCode(String code);
        void setMsg(String msg);
        void setObj(Object obj);
    }

    interface presenter {

        /**
         * 登录
         */
        void userLogin(String user, String pwd);

        /**
         * 获取全部患者列表
         */
        void getPatientList(String currentPage);

        /**
         * 获取在线患者
         */
        void getOnLinePatientList();

        /**
         * 获取ECG数据
         */
        void getRealTimeEcgData(String patientid,String serial,String starttime);

        /**
         * 获取Report数据
         */
        void getRealTimeReportData(String patientid,String time);

        /**
         * 搜索患者列表
         */
        void getPatientListBySearch(String currentPage,String keywords);

        /**
         * 获取在线患者报告列表
         */
        void getFinishEstimateList(String currentPage,String patientid);

        /**
         * 获取报告基本信息
         */
        void getBaseInfoReport(String estimateid);

        /**
         *  查询报告HRV信息
         */
        void getHRVInfo(String estimateid,String period);

        /**
         * 查询报告心肺和谐指数
         */
        void getCIRInfo(String estimateid,String period);

        //==============================训练报告=============================//

        /**
         * 查询训练列表
         */
        void getTrainList(String currentPage,String patientid);

        /**
         * 查询报告基本信息
         */
        void getTrainBaseInfoReport(String trainId,String preId);

        /**
         * 查询报告HRV信息
         */
        void getTrainHRVInfo(String trainId);

        /**
         * 查询报告心肺和谐指数
         */
        void getTrainCIRInfo(String trainId);

    }
}
