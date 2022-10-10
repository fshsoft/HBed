package com.java.health.care.bed.module;

import java.io.File;

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
         * 获取token
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

        /**
         * 生命体征检测文件上传
         */
        void uploadFile(File file,String strategy, String patientId,String preID,String preType);

        /**
         * 自主神经评估和心肺谐振训练文件上传
         */
        void uploadFileCPR(File file);

        /**
         * 香薰和声波 完成处方上传
         */
        void upExec(int preId,String preType,int duration,String startTime,String endTime);

        /**
         * 呼叫发送
         */
        void sendMessage(int clientId, int bunkId, int type,int userId);

        /**
         * 目前只针对自主神经处方（RESONANCE）和心肺谐振处方（NERVE）
         * 上传文件成功后，调用完成处方
         */

        void presFinish(int patientId,int preId,String preType);

        /**
         * 下载apk
         */
        void download(String versionName);
    }
}
