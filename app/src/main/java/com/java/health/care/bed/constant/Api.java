package com.java.health.care.bed.constant;

/**
 * @author fsh
 * @date 2022/09/07 09:33
 * @Description
 */
public class Api {
    /**
     * 获取token
     */

    public static final String getToken = "/user/oauth/token";

    /**
     * 获取科室和病区
     */

    public static final String getDeptRegion = "open/bunk/findDeptRegion";

    /**
     * 提交床位信息
     */

    public static final String saveBedInfo = "open/bunk/saveOrUpdate";

    /**
     * 根据床位获取患者信息
     */

    public static final String getUser = "open/bunk/findUser";
}
