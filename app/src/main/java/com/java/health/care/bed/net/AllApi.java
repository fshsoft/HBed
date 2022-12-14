package com.java.health.care.bed.net;

import com.java.health.care.bed.base.BaseEntry;
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
import com.java.health.care.bed.constant.Api;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface AllApi {

   /**
     * 获取token
     */
   @POST(Api.getToken)
   @FormUrlEncoded
   Observable<BaseEntry<Token>> getToken(@FieldMap Map<String, String> maps);

   /**
    * 获取科室和病区
    */
   @POST(Api.getDeptRegion)
   Observable<BaseEntry<List<Dept>>> getDeptRegion(@Header("authorization") String str, @Body Map<String, String> maps);

   /**
    * 提交床位信息
    */
   @POST(Api.saveBedInfo)
   Observable<BaseEntry<Bunk>> saveBedInfo(@Header("authorization") String str, @Body Map<String, String> maps);

   /**
    * 根据床位获取患者信息,前提：PC端必须要手动把床添加用户
    */
   @POST(Api.getUser)
   Observable<BaseEntry<Patient>> getUser(@Header("authorization") String str, @Body Map<String, Integer> maps);

   /**
    * 获取处方
    *
    * @Path 是为了拼接 http://192.168.0.13:1234/cas/open/prescription/find/1   1就是拼接的
    */

   @POST(Api.findPrescription)
   Observable<BaseEntry<Prescription>> getPrescription(@Header("authorization") String str,@Path("patient") String patient);

   /**
    * 文件上传 Api.uploadFile 这个是apk下载，我这边不需要，需要对生命体征检测上传文件 需要
    */
   @POST(Api.uploadFile)
   @Multipart
   Observable<BaseEntry<FileBean>> uploadFile(@Header("authorization") String str,@Part List<MultipartBody.Part> parts);


   /**
    * 目前只针对自主神经处方心肺谐振处方
    * 上传文件成功后，调用完成处方
    */
   @POST(Api.presFinish)
   Observable<BaseEntry> presFinish(@Header("authorization") String str,
                                    @Path("patientId") int patientId,@Path("preId") int preId,@Path("preType") String preType);



   /**
    * 心肺谐振评估和训练
    * 上传文件
    */

   @POST(Api.uploadFileCPR)
   @Multipart
   Call<ResponseBody> uploadFileCPR( @Part List<MultipartBody.Part> parts);


   /**
    *香薰和声波 完成处方上传
    */
   @POST(Api.upExec)
   Observable<BaseEntry<LLBean>> upExec(@Header("authorization") String str,@Body Map<String, String> maps);

   /**
    * 呼叫发送
    */
   @POST(Api.sendMessage)
   Observable<BaseEntry> sendMessage(@Header("authorization") String str,@Body Map<String, Object> maps);


   /**
    * 下载apk
    */
   @POST(Api.download)
   Observable<BaseEntry<DownloadFile>> download(@Header("authorization") String str,@Body Map<String, String> maps);

   /**
    * 比对apk版本
    */
   @POST(Api.compareVersionApk)
   Observable<BaseEntry<List<APK>>> compareVersionApk(@Header("authorization") String str, @Body Map<String, Object> maps);

}
