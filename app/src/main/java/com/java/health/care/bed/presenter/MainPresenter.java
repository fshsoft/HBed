package com.java.health.care.bed.presenter;

import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.base.BaseEntry;
import com.java.health.care.bed.base.BaseObserver;
import com.java.health.care.bed.bean.APK;
import com.java.health.care.bed.bean.APKS;
import com.java.health.care.bed.bean.Bunk;
import com.java.health.care.bed.bean.Dept;
import com.java.health.care.bed.bean.DownloadFile;
import com.java.health.care.bed.bean.FileBean;
import com.java.health.care.bed.bean.LLBean;
import com.java.health.care.bed.bean.Prescription;
import com.java.health.care.bed.bean.Token;
import com.java.health.care.bed.bean.Patient;
import com.java.health.care.bed.constant.SP;
import com.java.health.care.bed.module.MainContract;
import com.java.health.care.bed.net.RetrofitUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
     * 针对生命体征检测上传文件
     */
    @Override
    public void uploadFile(File file, String strategy,String patientId, String preId,String preType) {
        String value = SPUtils.getInstance().getString(SP.TOKEN);

        RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"),file);

        Map<String,String> map1 = new HashMap<>();
        map1.put("key","preId"); //preID
        map1.put("value",preId);

        Map<String,String> map2 = new HashMap<>();
        map2.put("key","preType"); //preID
        map2.put("value",preType);

        Map<String,String> map3 = new HashMap<>();
        map3.put("key","patientId"); //preID
        map3.put("value",patientId);

        JSONObject json1 = new JSONObject(map1);
        JSONObject json2 = new JSONObject(map2);
        JSONObject json3 = new JSONObject(map3);

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(json1);
        jsonArray.put(json2);
        jsonArray.put(json3);

        Map<String,Object> map = new HashMap<>();
        map.put("source","pad");
        map.put("strategy",strategy); //报告文件strategy
        map.put("otherParam",jsonArray);
        JSONObject jsonObject = new JSONObject(map);

        Log.d(" json.toString()", jsonObject.toString());

        MultipartBody multipartBody = new MultipartBody.Builder()
                .addFormDataPart("file", file.getName(), body)
                .addFormDataPart("param", jsonObject.toString() )
                .setType(MultipartBody.FORM)
                .build();

        RetrofitUtil.getInstance().initRetrofit().uploadFile(value,multipartBody.parts())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BaseObserver<FileBean >(context){

                        @Override
                        protected void onSuccess(BaseEntry<FileBean> t) throws Exception {
                            view.setObj(t.isSuccess());
                        }

                        @Override
                        protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                            showMessage(e,isNetWorkError);
                        }
                    });


    }

    /**
     * 生命体征监测，cm19和cm22无创连续血压文件上传接口
     */
    @Override
    public void uploadFileCPR(File file) {
        RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"),file);
        MultipartBody multipartBody = new MultipartBody.Builder()
                .addFormDataPart("file", file.getName(), body)
                .setType(MultipartBody.FORM)
                .build();

        RetrofitUtil.getInstance().initRetrofitCpr().uploadFileCPR(multipartBody.parts())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()){



                            try {
                                String json = response.body().string();
                                JSONObject jsonObject = new JSONObject(json);
                                String code = jsonObject.optString("rspCode");
                                view.setData(code);
                            } catch (IOException | JSONException e) {
                                e.printStackTrace();
                            }
                            view.setData(response.toString());

                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.d("ResponseBody======1",t.getMessage()+"==="+t.toString());

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
     * 呼叫发送接口
     */
    @Override
    public void sendMessage(int clientId, int bunkId, int type,int userId) {
        String value = SPUtils.getInstance().getString(SP.TOKEN);

        JSONObject js = new JSONObject();
        try {
            js.put("bunkId", bunkId);
            js.put("type", type);
            js.put("code",5001);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String message = js.toString();
        Map<String, Object> map = new HashMap<>();
        map.put("clientId", String.valueOf(clientId));
        map.put("messageType", "1");
        map.put("message", message);
        map.put("userId", String.valueOf(userId));


        RetrofitUtil.getInstance().initRetrofit().sendMessage(value, map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver(context) {
                    @Override
                    protected void onSuccess(BaseEntry t) throws Exception {
                        view.setCode(t.getCode());

                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });

        }

    /**
     * 针对心肺谐振评估和训练，上传文件成功后，调用这个接口，表示已经完成处方
     */
    @Override
    public void presFinish(int patientId, int preId, String preType) {
        String value = SPUtils.getInstance().getString(SP.TOKEN);
        RetrofitUtil.getInstance().initRetrofit().presFinish(String.valueOf(value),patientId,preId,preType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver(context) {

                    @Override
                    protected void onSuccess(BaseEntry t) throws Exception {
                        view.setObj(t.isSuccess());
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
                        showMessage(e,isNetWorkError);
                    }
                });
    }

    /**
     * 下载APK
     */
    @Override
    public void download(String versionName) {
        String value = SPUtils.getInstance().getString(SP.TOKEN);
        Map<String, String> map = new HashMap<>();
        map.put("name", versionName);
        map.put("source", "pad");
        map.put("strategy", "file_downloadLfs");
        RetrofitUtil.getInstance().initRetrofit().download(value, map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<DownloadFile>(context) {


                    @Override
                    protected void onSuccess(BaseEntry<DownloadFile> t) throws Exception {

                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {

                    }
                });
    }

    /**
     * 比对apk版本
     */
    @Override
    public void compareVersionApk() {
        String value = SPUtils.getInstance().getString(SP.TOKEN);
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("fileFormat", "apk");
        map.put("success",true);
        RetrofitUtil.getInstance().initRetrofit().compareVersionApk(value, map)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<List<APK>>(context) {


                    @Override
                    protected void onSuccess(BaseEntry<List<APK>> t) throws Exception {
                        String fileName = t.getData().get(0).getFileName();

                        view.setData(fileName);
                    }

                    @Override
                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {

                    }
                });
    }

}
