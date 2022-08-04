package com.java.health.care.bed.constant;

import java.security.PublicKey;

/**
 * @author fsh
 * @date 2022/08/04 15:14
 * @Description 常量
 */
public class Constant {

    /**
     *  *      * CM19心电设备
     *  *      * uuid_service===00001526-1212-efde-1523-785feabcd123
     *  *      * uuid_chara===  00001527-1212-efde-1523-785feabcd123
     *  *      *
     *  *      * 血氧设备
     *  *      * uuid_service===0000ffb0-0000-1000-8000-00805f9b34fb
     *  *      * uuid_chara===0000ffb2-0000-1000-8000-00805f9b34fb
     *  *      *
     *  *      * 血压设备
     *  *      * uuid_service===0000fff0-0000-1000-8000-00805f9b34fb
     *  *      * uuid_chara===  0000fff2-0000-1000-8000-00805f9b34fb
     */
    public static final String UUID_SERVICE_CM19 ="00001526-1212-efde-1523-785feabcd123";
    public static final String UUID_CHARA_CM19 ="00001527-1212-efde-1523-785feabcd123";

    public static final String UUID_SERVICE_SPO2 ="0000ffb0-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARA_SPO2 ="0000ffb2-0000-1000-8000-00805f9b34fb";

    public static final String UUID_SERVICE_BP ="0000fff0-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARA_BP ="0000fff3-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARA_NOTIFY_BP ="0000fff2-0000-1000-8000-00805f9b34fb";
    /**
     *  *      * 血压计要写入数据，然后读取数据
     *  *      * private String START = "AA55FFC1000000000000000000000000000000CC";
     *  *      *     private String READY1 = "AA55FFB0";
     *  *      *     private String READY2 = "0000000000000000CC";
     *  *      *
     *  *      *  SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
     *  *      *  String data = df.format(new Date());
     *  *      *  READY = READY_ONE+data+READY_TWO;  //AA55FFB0201903131355530000000000000000CC
     */
    public static final String READY_ONE = "AA55FFB0";
    public static final String READY_TWO = "0000000000000000CC";
    public static final String START = "AA55FFC1000000000000000000000000000000CC";

}
