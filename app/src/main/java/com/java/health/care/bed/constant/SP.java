package com.java.health.care.bed.constant;

import retrofit2.http.PUT;

/**
 * @author fsh
 * @date 2022/09/07 13:32
 * @Description
 */
public class SP {

    public static final String IP_SERVER_ADDRESS = "ip_server_address";

    public static final String IP_SERVER_PORT = "ip_server_port";

    public static final String TOKEN = "access_token";

    public static final String BUNK_NUM = "bunk_num"; //床号
    public static final String BUNK_ID = "bunk_id";
    public static final String DEPT_NUM = "dept_num"; //科室
    public static final String DEPT_ID = "dept_id";
    public static final String REGION_NUM = "region_num"; //病区
    public static final String REGION_ID = "region_id";
    public static final String PATIENT_ID = "patientId";
    public static final String PATIENT_NAME = "patientName";
    public static final String PATIENT_SEX = "patientSex";
    public static final String PATIENT_AGE = "patientAge";
    public static final String PATIENT_ARM_LENGTH = "patientArmLength";
    public static final String PATIENT_HEIGHT = "patientHeight";
    public static final String PATIENT_WEIGHT = "patientWeight";
    public static final String HOSPITAL_ID = "hospitalId";






    public static final String TEMP = "TEMP";

    //记录开始时间
    public static final String KEY_ECG_FILE_TIME = "key_ecg_file_time";
    //记录结束时间
    public static final String KEY_RESP_FILE_TIME = "key_resp_file_time";

    //接口常量1-血压，2-血氧，3-体温 4-心电
    //处方类型"preType": "FRAGRANCE
    public static final String FANGXING = "FRAGRANCE"; //芳香
    public static final String SHENGBO = "SONIC_WAVE"; //声波
    public static final String ZZSJ = "NERVE"; //自主神经评估
    public static final String XFXZ = "RESONANCE"; //心肺谐振训练
    public static final String SMTZ = "LIFE";  //生命体征
    public static final String WCXY = "BLOOD_PRESSURE"; //无创血压

    //无创连续血压值标定
    public static final String WCPRESSVALUE ="WC_PRESS_VALUE";



}
