package com.java.health.care.bed.module;

public interface MainContract {

    interface View  {
        void setCode(String code);
        void setMsg(String msg);
        void setInfo(String msg);
        void setObj(Object obj);
        void setData(Object obj);
    }


    interface presenter {

        /**
         * 登录
         */
        void getToken(String grant_type, String client_id,String client_secret);

        /**
         * 获取科室和病区
         */
        void getDeptRegion();

        /**
         * 提交床位信息
         */
        void saveBedInfo(int deptId,int regionId,String number);

        /**
         * 根据床位获取患者信息
         */
        void getUser(int bunkId);

        /**
         * 获取处方
         */
        void getPrescription(int patientId);
    }
}
