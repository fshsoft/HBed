package com.java.health.care.bed.presenter;

import android.content.Context;

import com.java.health.care.bed.base.BaseEntry;
import com.java.health.care.bed.base.BaseObserver;
import com.java.health.care.bed.bean.Dept;
import com.java.health.care.bed.bean.Token;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.net.RetrofitUtil;

import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MainPresenter implements MainContract.presenter {

    private Context context;
    private MainContract.View view;

    public MainPresenter(Context context, MainContract.View view) {
        this.context = context;
        this.view = view;
    }

    private void showMessage(Throwable e,boolean isNetWorkError){
        if(isNetWorkError){
            view.setMsg("网络或服务器异常");
        }else {
            view.setMsg("服务器连接失败，请检查网络或服务器！");
        }

    }

    /**
     * 获取token
     * @param grant_type
     * @param client_id
     * @param client_secret
     */
    @Override
    public void getToken(String grant_type, String client_id, String client_secret) {
        Map<String,String> map=new HashMap<>();
        map.put("grant_type",grant_type);
        map.put("client_id",client_id);
        map.put("client_secret",client_secret);
        RetrofitUtil.getInstance().initBaseRetrofit(context).getToken(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<Token>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<Token> t)  {
                        view.setCode(t.getCode());
                        view.setMsg(t.getMessage());
                        view.setObj(t.getData());
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError)  {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    @Override
    public void getDeptRegion() {
        RetrofitUtil.getInstance().initRetrofit(context).getDeptRegion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<Dept>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<Dept> t) throws Exception {
                        view.setCode(t.getCode());
                        view.setMsg(t.getMessage());
                        view.setObj(t.getData());
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    @Override
    public void saveBedInfo(int deptId, int regionId, String number) {

    }

    @Override
    public void getUser(int bunkId) {

    }

    /**
     * 获取科室和病区
     */




    /*   *//**
     * 登录
     *//*
    @Override
    public void userLogin(String user, String pwd) {
        Map<String,String> map=new HashMap<>();
        map.put("account",user);
        map.put("password",pwd);
        RetrofitUtil.getInstance().initRetrofit(context).userLogin(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<Login>(context, MainUtil.loadLogin) {
                    @Override
                    protected void onSuccess(BaseEntry<Login> t) throws Exception {
                        view.setCode(t.getCode());
                        view.setMsg(t.getMsg());
                       if(t.isSuccess()){
                            Login.getInstance().setAccountId(t.getData().getAccountId());
                       }
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);

                    }
                });
    }

    *//**
     * 获取全部患者列表
     *//*
    @Override
    public void getPatientList(String currentPage) {
        Map<String,String> map=new HashMap<>();
        map.put("currentPage",currentPage);
        RetrofitUtil.getInstance().initRetrofit(context).getPatientList(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<Patient>>(context) {

                    @Override
                    protected void onSuccess(BaseEntry<List<Patient>> t) throws Exception {
                        view.setObj(t.getData());
                        view.setCode(t.getTotal());
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    *//**
     * 获取在线患者
     *//*
    @Override
    public void getOnLinePatientList() {
        RetrofitUtil.getInstance().initRetrofit(context).getOnLinePatientList().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<PatientOnLine>>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<List<PatientOnLine>> t) throws Exception {
                        view.setObj(t.getData());
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    *//**
     * 获取ECG数据
     *//*

    @Override
    public void getRealTimeEcgData(String patientid, String serial, String starttime) {
        Map<String,String> map=new HashMap<>();
        map.put("patientid",patientid);
        map.put("serial",serial);
        map.put("starttime",starttime);
        RetrofitUtil.getInstance().initRetrofit(context).getRealTimeEcgData(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<RealTimeEcgStruct>>(context) {

                    @Override
                    protected void onSuccess(BaseEntry<List<RealTimeEcgStruct>> t) throws Exception {
                        view.setObj(t.getData());
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    *//**
     * 获取Report数据
     *//*
    @Override
    public void getRealTimeReportData(String patientid, String time) {
        Map<String,String> map=new HashMap<>();
        map.put("patientid",patientid);
        map.put("time",time);
        RetrofitUtil.getInstance().initRetrofit(context).getRealTimeReportData(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<RealTimeReportStruct>(context) {


                    @Override
                    protected void onSuccess(BaseEntry<RealTimeReportStruct> t) throws Exception {
                        view.setObj(t.getData());
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    *//**
     * 搜索患者列表
     *//*
    @Override
    public void getPatientListBySearch(String currentPage, String keywords) {
        Map<String,String> map=new HashMap<>();
        map.put("currentPage",currentPage);
        map.put("keywords",keywords);
        RetrofitUtil.getInstance().initRetrofit(context).getPatientListBySearch(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<Patient>>(context) {

                    @Override
                    protected void onSuccess(BaseEntry<List<Patient>> t) throws Exception {
                        view.setObj(t.getData());
                        view.setCode(t.getTotal());
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    *//**
     * 获取在线患者报告列表
     *//*
    @Override
    public void getFinishEstimateList(String currentPage, String patientid) {
        Map<String,String> map=new HashMap<>();
        map.put("currentPage",currentPage);
        map.put("patientid",patientid);
        RetrofitUtil.getInstance().initRetrofit(context).getFinishEstimateList(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<Estimate>>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<List<Estimate>> t) throws Exception {
                        view.setObj(t.getData());
                        view.setCode(t.getTotal());
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });

    }

    *//**
     * 获取报告基本信息
     *//*
    @Override
    public void getBaseInfoReport(String estimateid) {
        Map<String,String> map=new HashMap<>();
        map.put("estimateid",estimateid);
        RetrofitUtil.getInstance().initRetrofit(context).getBaseInfoReport(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ReportInfo>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<ReportInfo> t) throws Exception {
                        view.setObj(t.getData());
                        if(!t.isSuccess()){
                            view.setMsg(t.getMsg());
                        }

                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    *//**
     *  查询报告HRV信息
     *//*
    @Override
    public void getHRVInfo(String estimateid,String period) {
        Map<String,String> map=new HashMap<>();
        map.put("estimateid",estimateid);
        map.put("period",period);
        RetrofitUtil.getInstance().initRetrofit(context).getHRVInfo(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<HRVInfo>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<HRVInfo> t) throws Exception {
                        view.setObj(t.getData());
                        if(!t.isSuccess()){
                            view.setMsg(t.getMsg());
                        }
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    *//**
     * 查询报告心肺和谐指数
     *//*
    @Override
    public void getCIRInfo(String estimateid,String period) {
        Map<String,String> map=new HashMap<>();
        map.put("estimateid",estimateid);
        map.put("period",period);
        RetrofitUtil.getInstance().initRetrofit(context).getCIRInfo(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<NormInfo>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<NormInfo> t) throws Exception {
                        view.setObj(t.getData());
                        if(!t.isSuccess()){
                            view.setMsg(t.getMsg());
                        }
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    //==========================================训练报告====================================================//

    *//**
     * 查询训练列表
     *//*
    @Override
    public void getTrainList(String currentPage, String patientid) {
        Map<String,String> map = new HashMap<>();
        map.put("currentPage",currentPage);
        map.put("patientid",patientid);
        RetrofitUtil.getInstance().initRetrofit(context).getTrainList(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<TrainList>>(context) {

                    @Override
                    protected void onSuccess(BaseEntry<List<TrainList>> t) throws Exception {
                        view.setObj(t.getData());
                        view.setCode(t.getTotal());
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    *//**
     * 查询报告基本信息
     *//*
    @Override
    public void getTrainBaseInfoReport(String trainId, String preId) {
        Map<String,String> map = new HashMap<>();
        map.put("trainId",trainId);
        String spreId = preId.substring(8);
        map.put("preId",spreId);
        RetrofitUtil.getInstance().initRetrofit(context).getTrainBaseInfoReport(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<ReportInfo>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<ReportInfo> t) throws Exception {
                        view.setObj(t.getData());
                        if(!t.isSuccess()){
                            view.setMsg(t.getMsg());
                        }
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    *//**
     * 查询报告HRV信息
     *//*
    @Override
    public void getTrainHRVInfo(String trainId) {
        Map<String,String> map = new HashMap<>();
        map.put("trainId",trainId);
        RetrofitUtil.getInstance().initRetrofit(context).getTrainHRVInfo(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<HRVInfo>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<HRVInfo> t) throws Exception {
                        view.setObj(t.getData());
                        if(!t.isSuccess()){
                            view.setMsg(t.getMsg());
                        }
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    *//**
     * 查询报告心肺和谐指数
     *//*
    @Override
    public void getTrainCIRInfo(String trainId) {
        Map<String,String> map = new HashMap<>();
        map.put("trainId",trainId);
        RetrofitUtil.getInstance().initRetrofit(context).getTrainCIRInfo(map).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<NormInfo>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<NormInfo> t) throws Exception {
                        view.setObj(t.getData());
                        if(!t.isSuccess()){
                            view.setMsg(t.getMsg());
                        }
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }*/

}
