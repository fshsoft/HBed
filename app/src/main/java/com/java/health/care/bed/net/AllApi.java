package com.java.health.care.bed.net;


//import com.microsens.breathehealth.base.BaseEntry;
//import com.microsens.breathehealth.bean.Estimate;
//import com.microsens.breathehealth.bean.HRVInfo;
//import com.microsens.breathehealth.bean.Login;
//import com.microsens.breathehealth.bean.NormInfo;
//import com.microsens.breathehealth.bean.Patient;
//import com.microsens.breathehealth.bean.PatientOnLine;
//import com.microsens.breathehealth.bean.RealTimeEcgStruct;
//import com.microsens.breathehealth.bean.RealTimeReportStruct;
//import com.microsens.breathehealth.bean.ReportInfo;
//import com.microsens.breathehealth.bean.TrainList;
//import com.microsens.breathehealth.constant.ApiAddress;
import java.util.List;
import java.util.Map;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * @description:
 */

public interface AllApi {

   /* *//**
     * 登录
     *//*
    @POST(ApiAddress.userLogin)
    Observable<BaseEntry<Login>> userLogin(@Body Map<String, String> maps);

    *//**
     * 获取全部患者列表
     *//*
    @POST(ApiAddress.getPatientList)
    Observable<BaseEntry<List<Patient>>> getPatientList(@Body Map<String, String> maps);

    *//**
     * 获取在线患者
     *//*
    @POST(ApiAddress.getOnLinePatientList)
    Observable<BaseEntry<List<PatientOnLine>>> getOnLinePatientList();

    *//**
     * 获取ECG数据
     *//*
    @POST(ApiAddress.getRealTimeEcgData)
    Observable<BaseEntry<List<RealTimeEcgStruct>>> getRealTimeEcgData(@Body Map<String,String> maps);

    *//**
     * 获取Report数据
     *//*
    @POST(ApiAddress.getRealTimeReportData)
    Observable<BaseEntry<RealTimeReportStruct>> getRealTimeReportData(@Body Map<String,String> maps);

    *//**
     * 搜索患者列表
     *//*
    @POST(ApiAddress.getPatientListBySearch)
    Observable<BaseEntry<List<Patient>>> getPatientListBySearch(@Body Map<String, String> maps);

    *//**
     * 获取在线患者报告列表
     *//*
    @POST(ApiAddress.getFinishEstimateList)
    Observable<BaseEntry<List<Estimate>>> getFinishEstimateList(@Body Map<String, String> maps);

    *//**
     * 获取报告基本信息
     *//*
    @POST(ApiAddress.getBaseInfoReport)
    Observable<BaseEntry<ReportInfo>> getBaseInfoReport(@Body Map<String, String> maps);

    *//**
     *  查询报告HRV信息
     *//*
    @POST(ApiAddress.getHRVInfo)
    Observable<BaseEntry<HRVInfo>> getHRVInfo(@Body Map<String, String> maps);

    *//**
     * 查询报告心肺和谐指数
     *//*
    @POST(ApiAddress.getCIRInfo)
    Observable<BaseEntry<NormInfo>> getCIRInfo(@Body Map<String, String> maps);



    //===================================训练报告=====================================================//

    *//**
     * 查询训练列表
     *//*
    @POST(ApiAddress.trainlist)
    Observable<BaseEntry<List<TrainList>>> getTrainList(@Body Map<String, String> maps);

    *//**
     * 查询报告基本信息
     *//*
    @POST(ApiAddress.trainbaseinfo)
    Observable<BaseEntry<ReportInfo>> getTrainBaseInfoReport(@Body Map<String, String> maps);

    *//**
     * 查询报告HRV信息
     *//*
    @POST(ApiAddress.hrvinfooftrainreport)
    Observable<BaseEntry<HRVInfo>> getTrainHRVInfo(@Body Map<String, String> maps);

    *//**
     * 查询报告心肺和谐指数
     *//*
    @POST(ApiAddress.criinfooftrainreport)
    Observable<BaseEntry<NormInfo>> getTrainCIRInfo(@Body Map<String, String> maps);*/

}
