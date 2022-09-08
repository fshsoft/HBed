package com.java.health.care.bed.net;

import android.content.Context;
import android.text.TextUtils;

import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.constant.SP;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @description: retrofit请求工具类
 */

public class RetrofitUtil {
    /**
     * 超时时间
     */
    private static volatile RetrofitUtil mInstance;
    private AllApi allApi;
    public static final int TIMEOUT = 5;

    /**
     * 单例封装
     *
     * @return
     */
    public static RetrofitUtil getInstance() {
        if (mInstance == null) {
            synchronized (RetrofitUtil.class) {
                if (mInstance == null) {
                    mInstance = new RetrofitUtil();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化Retrofit
     */
    public AllApi initBaseRetrofit() {
        String ip = SPUtils.getInstance().getString(SP.IP_SERVER_ADDRESS);
        if(!ip.isEmpty()){
            String url ="http://"+ip+":1240/";

            //增加超时时间
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(InterceptorUtil.LogInterceptor())//添加日志拦截器
                    .build();


            Retrofit mRetrofit = new Retrofit.Builder()
                    // 设置请求的域名
                    .baseUrl(url)
                    // 设置解析转换工厂，用自己定义的
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)
                    .build();
            allApi = mRetrofit.create(AllApi.class);
        }

        return allApi;
    }

    public AllApi initRetrofit() {
        String ip = SPUtils.getInstance().getString(SP.IP_SERVER_ADDRESS);

        if(!ip.isEmpty()){
            String url ="http://"+ip+":1236/";

            //增加超时时间
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(InterceptorUtil.LogInterceptor())//添加日志拦截器
                    .build();


            Retrofit mRetrofit = new Retrofit.Builder()
                    // 设置请求的域名
                    .baseUrl(url)
                    // 设置解析转换工厂，用自己定义的
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(client)
                    .build();
            allApi = mRetrofit.create(AllApi.class);
        }

        return allApi;
    }
}
