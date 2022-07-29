package com.java.health.care.bed.util;

import com.java.health.care.bed.bean.User;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * @author fsh
 * @date 2022/07/29 15:17
 * @Description 请求APi
 */
public interface ApiServer {

    /**
     * 获取用户信息
     *
     * @return
     */

    @GET(Constant.USER)
    Observable<User> getUser();
}
