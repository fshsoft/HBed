package com.java.health.care.bed.presenter;

import android.content.Context;

import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.base.BaseEntry;
import com.java.health.care.bed.base.BaseObserver;
import com.java.health.care.bed.bean.Bunk;
import com.java.health.care.bed.bean.Dept;
import com.java.health.care.bed.bean.LLBean;
import com.java.health.care.bed.bean.Prescription;
import com.java.health.care.bed.bean.Token;
import com.java.health.care.bed.bean.Patient;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.net.RetrofitUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
     * 1、获取token
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
        RetrofitUtil.getInstance().initBaseRetrofit().getToken(map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<Token>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<Token> t)  throws Exception{
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

    /**
     * 2、获取科室和病区
     */
    @Override
    public void getDeptRegion() {
        String value = SPUtils.getInstance().getString(SP.TOKEN);
        Map<String,String> map=new HashMap<>();
        RetrofitUtil.getInstance().initRetrofit().getDeptRegion(value,map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<Dept>>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<List<Dept>> t) throws Exception{
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

    /**
     * 3、保存病床编号
     * @param deptId
     * @param regionId
     * @param number
     */
    @Override
    public void saveBedInfo(int deptId, int regionId, String number) {
        String value = SPUtils.getInstance().getString(SP.TOKEN);
        Map<String,String> map=new HashMap<>();
        map.put("deptId", String.valueOf(deptId));
        map.put("regionId", String.valueOf(regionId));
        map.put("number", number);
        RetrofitUtil.getInstance().initRetrofit().saveBedInfo(value,map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<Bunk>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<Bunk> t) throws Exception {
                        view.setCode(t.getCode());
                        view.setInfo(t.getMessage());
                        view.setData(t.getData());
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    /**
     * 4、根据病床ID获取用户信息
     * @param bunkId
     */
    @Override
    public void getUser(int bunkId) {
        String value = SPUtils.getInstance().getString(SP.TOKEN);
        Map<String,Integer> map=new HashMap<>();
        map.put("bunkId", bunkId);
        RetrofitUtil.getInstance().initRetrofit().getUser(String.valueOf(value),map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<Patient>(context) {

                    @Override
                    protected void onSuccess(BaseEntry<Patient> t) throws Exception {
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

    /**
     * 5、获取处方
     */
    @Override
    public void getPrescription(int patientId) {
        String value = SPUtils.getInstance().getString(SP.TOKEN);
        RetrofitUtil.getInstance().initRetrofit().getPrescription(String.valueOf(value),String.valueOf(patientId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<Prescription>(context) {

                    @Override
                    protected void onSuccess(BaseEntry<Prescription> t) throws Exception {
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
    /**
     * 熏香和声波 结束上传
     *  "duration": 100,
     * 	"endTime": "2022-12-22 14:30:50",
     *  "preId": 3,
     * 	"preType": "SONIC_WAVE",
     * 	"startTime": "2022-12-22 14:25:50"
     */
    @Override
    public void upExec(int preId, String preType, int duration, String startTime, String endTime) {
        String value = SPUtils.getInstance().getString(SP.TOKEN);
        Map<String,String> map=new HashMap<>();
        map.put("preId", String.valueOf(preId));
        map.put("preType",preType);
        map.put("duration", String.valueOf(duration));
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        RetrofitUtil.getInstance().initRetrofit().upExec(value,map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<LLBean>(context) {
                    @Override
                    protected void onSuccess(BaseEntry<LLBean> t) throws Exception {
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

    /**
     * 文件上传
     */







}
