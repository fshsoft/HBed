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
    public static final String findPrescription = "/cas/open/prescription/find/patient/{patient}";

    /**
     * 目前只针对自主神经处方（RESONANCE）和心肺谐振处方（NERVE）
     * 上传文件成功后，调用完成处方
     */
    public static final String presFinish  =  "/cas/open/prescription/finish/{patientId}/{preType}/{preId}";


    /**
     * 生命体征检测需要上传的文件
     */
    public static final String uploadFile = "/cas/open/file/upload";

    /**
     * 自主神经评估和心肺谐振训练文件上传
     */

    public static final String uploadFileCPR = "/cprraw";


    /**
     * 香薰和声波 完成处方上传
     */
    public static final String upExec = "cas/open/pre/exec";


    /**
     * 呼叫发送
     */
    public static final String sendMessage = "/cas/device/gateway/deviceGateWay/sendMessage";


    /**
     * 下载apk
     */
    public static final String download = "/cas/open/file/download";

    /**
     * apk 版本号进行比对
     */


}
