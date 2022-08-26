package com.java.health.care.bed.base;

import com.java.health.care.bed.util.ApiServer;
import com.java.health.care.bed.util.Constant;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 基类Model,处理数据，网络请求数据
 * @POST("api/sys/getPermissions")
 *  @Headers({"Content-Type:application/x-www-form-urlencoded",
 *             "Authorization:Basic VmJyZjJNU1FxejhBVlhzMmajhETkJV"})
 *  Observable<TokenBean> getSsoToken();
 */
public class BaseModel {

    protected Retrofit mRetrofit;

    protected ApiServer mApiServer;

    public void retrofitManager()  {
        mRetrofit = new Retrofit.Builder()
                .baseUrl(Constant.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mApiServer = mRetrofit.create(ApiServer.class);
    }
}
