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

    public static final String getToken = "/cas/user/oauth/token";

    /**
     * 获取科室和病区
     */

    public static final String getDeptRegion = "/cas/open/bunk/findDeptRegion";

    /**
     * 提交床位信息
     */

    public static final String saveBedInfo = "/cas/open/bunk/saveOrUpdate";

    /**
     * 根据床位获取患者信息
     */

    public static final String getUser = "/cas/open/bunk/findUser";

    /**
     * 获取处方
     * @Path 是为了拼接 http://192.168.0.13:1234/cas/open/prescription/find/1   1就是拼接的
     * 这个里面的{patient}是为了拼接
     *
     */
    public static final String findPrescription = "/cas/open/prescription/find/{patient}";

    /**
     * APK文件上传,我这边不需要处理这个接口
     */
    public static final String uploadFile = "/cas/open/file/upload";

    /**
     * 文件下载，这个是apk下载
     */
    public static final String downloadFile = "/cas/open/file/download";

    /**
     * 香薰和声波 完成处方上传
     */
    public static final String upExec = "cas/open/pre/exec";

    /**
     * 生命体征检测，心肺谐振评估和训练  完成之后文件上传
     */

}
