package com.java.health.care.bed.util;

/**
 * @author fsh
 * @date 2022/09/20 15:13
 * @Description
 */
public class TimeUtil {


    /**
     * @param second 秒
     * @description: 秒转换为时分秒 HH:mm:ss 格式 仅当小时数大于0时 展示HH
     * @return: {@link String}
     * @author: pzzhao
     * @date: 2022-05-08 13:55:17
     */
    public static String second2Time(Long second) {
        if (second == null || second <= 0) {
            return "0秒";
        }

        long h = second / 3600;
        long m = (second % 3600) / 60;
        long s = second % 60;
        String str = "";
        if (h > 0) {
            str =  h + "时";
        }
        if(m>0){
            str += m + "分钟";
        }
        if(s>0){
            str += s + "秒";
        }

        return str;

    }
}
