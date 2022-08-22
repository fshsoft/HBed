package com.java.health.care.bed.constant;

/**
 * @author fsh
 * @date 2022/08/04 15:14
 * @Description 常量
 */
public class Constant {

    public static final String UUID_SERVICE_CM19 ="00001526-1212-efde-1523-785feabcd123";
    public static final String UUID_CHARA_CM19_NOTIFY ="00001527-1212-efde-1523-785feabcd123";

    public static final String UUID_SERVICE_SPO2 ="0000ffb0-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARA_SPO2_NOTIFY ="0000ffb2-0000-1000-8000-00805f9b34fb";

    public static final String UUID_SERVICE_BP ="0000fff0-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARA_BP_WRITE ="0000fff3-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARA_BP_NOTIFY ="0000fff2-0000-1000-8000-00805f9b34fb";

    public static final String START = "AA55FFC1000000000000000000000000000000CC";

    public static final String UUID_SERVICE_IRT ="0000ffb0-0000-1000-8000-00805f9b34fb";
    public static final String UUID_CHARA_IRT_NOTIFY ="0000ffb2-0000-1000-8000-00805f9b34fb";

    public static final String UUID_SERVICE_KYC ="00001526-1212-efde-1523-785feabcd123";
    public static final String UUID_CHARA_KYC_NOTIFY ="00001527-1212-efde-1523-785feabcd123";
    public static final String UUID_CHARA_KYC_WRITE ="00001528-1212-efde-1523-785feabcd123";

    public static final String CM19 = "CM19"; //心电
    public static final String SPO2 = "SpO2"; //血氧
    public static final String QIANSHAN = "QianShan"; //血压
    public static final String IRT = "IRT"; //体温计
    public static final String KANGYANGCHUANG = "kangyangchuang"; //康养床
    public static final String BLE_DEVICE_CM19_MAC = "BLE_DEVICE_CM19_MAC";
    public static final String BLE_DEVICE_SPO2_MAC = "BLE_DEVICE_SPO2_MAC";
    public static final String BLE_DEVICE_QIANSHAN_MAC = "BLE_DEVICE_QIANSHAN_MAC";
    public static final String BLE_DEVICE_IRT_MAC = "BLE_DEVICE_IRT_MAC";
    public static final String BLE_DEVICE_KYC_MAC = "BLE_DEVICE_KANGYANGCHUANG_MAC";

    public static final String BP_DATA = "BP_DATA";
    public static final String BP_DATA_ERROR = "BP_ERROR";
    public static final String IRT_DATA = "IRT_DATA";
    public static final String SPO2_DATA = "SPO2_DATA";

    public static final String SERVER_IP = "server_ip";
    public static final String AREA_NUM = "area_num";
    public static final String BED_NUM = "bed_num";

    //==============================================================================================//
    //康养床-声波\熏香\呼叫

    //打开声波
    public static final String OPEN_SOUND_WAVE_ONE_HALL = "FE080101";//FE帧头 08数据长度 01功能码打开（01打开第一通道，0A时间10分钟）
    public static final String OPEN_SOUND_WAVE_TWO_HALL = "FE080102";
    public static final String OPEN_SOUND_WAVE_THREE_HALL = "FE080103";
    public static final String OPEN_SOUND_WAVE_FOUR_HALL = "FE080104";

    public static final String OPEN_SOUND_WAVE_TIME = "0A"; //动态数据

    public static final String OPEN_SOUND_WAVE_CODE = "";//校验位要计算 08+01+01+10时间
    public static final String OPEN_SOUND_WAVE_LAST = "FF16";
    public static final String OPEN_SOUND_WAVE_LAST_ONE = "14FF16";//14校验位 FF16 帧尾
    public static final String OPEN_SOUND_WAVE_LAST_TWO = "15FF16";
    public static final String OPEN_SOUND_WAVE_LAST_THREE = "16FF16";
    public static final String OPEN_SOUND_WAVE_LAST_FOUR = "17FF16";

    //关闭声波
    public static final String CLOSE_SOUND_WAVE_ONE_HALL = "FE0702010AFF16";//FE帧头 08数据长度?? 01功能码关闭（01第一通道）
    public static final String CLOSE_SOUND_WAVE_TWO_HALL = "FE0702020BFF16";
    public static final String CLOSE_SOUND_WAVE_THREE_HALL = "FE0702030CFF16";
    public static final String CLOSE_SOUND_WAVE_FOUR_HALL = "FE0702040DFF16";


    //打开熏香
    public static final String OPEN_SWEET_ONE_HALL = "FE080301";//FE帧头 08数据长度 01功能码打开（01打开第一通道，0A时间10分钟）
    public static final String OPEN_SWEET_TIME = "0A"; //动态数据时间0A表示10分钟
    public static final String SWEET_CODE = "16";//校验位要计算 08+03+01+时间
    public static final String OPEN_SWEET_LAST = "FF16";//14校验位 FF16 帧尾

    //关闭熏香
    public static final String CLOSE_SWEET_ONE_HALL = "FE07040112FF16";

    //打开LED灯
    public static final String OPEN_LED_ONE_HALL = "FE080501";//FE帧头 08数据长度 01功能码打开（01打开第一通道，0A时间10分钟）
    public static final String OPEN_LED_TIME = "0A"; //动态数据时间0A表示10分钟
    public static final String LED_CODE = "";//校验位要计算 08+03+01+时间
    public static final String OPEN_LED_LAST = "FF16";//14校验位 FF16 帧尾

    //关闭LED灯
    public static final String CLOSE_LED_ONE_HALL = "FE07050113FF16";

    //呼叫响应
    public static final String CALL_ON = "FE0707000EFF16";

    //呼叫取消响应
    public static final String CALL_OFF = "FE0707000EFF16";


}
