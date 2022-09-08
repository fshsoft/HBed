package com.java.health.care.bed.net;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.constant.SP;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author fsh
 * @date 2022/09/07 11:10
 * @Description
 */
public class CommonHeaderInterceptor implements Interceptor {
    String value = SPUtils.getInstance().getString(SP.TOKEN);
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        if(value!=null){
            request.newBuilder()
                    .addHeader("authorization", value)
                    .build();

        }
        return chain.proceed(request);
    }
}
