package com.java.health.care.bed.net;

import com.java.health.care.bed.base.BaseEntry;
import com.java.health.care.bed.bean.Dept;
import com.java.health.care.bed.bean.Token;
import com.java.health.care.bed.bean.User;
import com.java.health.care.bed.constant.Api;

import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

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
   Observable<BaseEntry<Dept>> getDeptRegion();

   /**
    * 提交床位信息
    */
   @POST(Api.saveBedInfo)
   Observable<BaseEntry> saveBedInfo(@Body Map<String, String> maps);

   /**
    * 根据床位获取患者信息
    */
   @POST(Api.getUser)
   Observable<BaseEntry<User>> getUser(@Body Map<String, String> maps);

   /*/**
     * 获取全部患者列表
     *//*
    @POST(ApiAddress.getPatientList)
    Observable<BaseEntry<List<Patient>>> getPatientList(@Body Map<String, String> maps);

    *//**
     * 获取在线患者
     *//*
    @POST(ApiAddress.getOnLinePatientList)
    Observable<BaseEntry<List<PatientOnLine>>> getOnLinePatientList();

   */
}
