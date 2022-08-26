package com.java.health.care.bed.util;

import com.java.health.care.bed.test.SquareData;
import com.java.health.care.bed.bean.User;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

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

    /**
     * 获取广场数据
     *
     * @param pageNum
     * @return
     */
    @GET(Constant.HOME_SQUARE_URL)
    Observable<SquareData> loadHomeSquareData(@Path("pageNum") int pageNum);


//    Observable<SquareData> loadHomeSquareData(@Header("Authorization") String authorization,@Path("pageNum") int pageNum);


    /**
     * 静态添加header
     * @return
     */
    @POST("api/sys/getPermissions")
    @Headers({"Content-Type:application/x-www-form-urlencoded",
            "Authorization:Basic VmJyZjJNU1FxejhBVlhzMmajhETkJV"})
    Observable<User> getSsoToken();


    /**
     * 动态添加header
     */
//    @GET("/data")
//    Call<List<AddressBean>> getData(@Header("Content-Range") String contentRange);

    /**
     * 动态添加多个header
     */
//    @GET("/search")
//    Call<ResponseBody> list(@HeaderMap Map<String, String> headers);
}
