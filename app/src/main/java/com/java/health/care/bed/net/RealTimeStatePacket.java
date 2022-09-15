package com.java.health.care.bed.net;

import android.util.Log;

import com.java.health.care.bed.util.ByteUtil;

import java.util.Arrays;

/**
 * @author fsh
 * @date 2022/09/15 10:11
 * @Description
 */
public class RealTimeStatePacket {
    public static final int SIZE = 732;
    /**
     * 头部header version 1  code 1  桢长度 2  serial 4  用户id 4
     * 1+1+2+4+4 = 12
     */
    //版本协议
    private byte version = 2;

    //编码
    private byte code =1;

    //帧长度
    private short length =732;

    //序号,从0开始
    public int serialNum = 0;

    //用户ID
    private int userId;

    //心电
    private byte[] ecgData;

    //呼吸波
    private byte[] respData;

    //PPG波
    private byte[] ppgData;

    //心率
    private short heartRate;

    //血氧
    private short spo2;

    //舒张压
    private short szPress;

    //收缩压
    private short ssPress;

    //呼吸频率
    private short resp;

    //体温
    private short temp;
    /**
     * body数据
     * 心电Type = 0x0010->0 16，呼吸Type = 0x0012->0 18，PPG数据Type = 0x0041->0 65，
     * 心率Type = 0x0042->0 66，血氧Type = 0x0043->0 67，
     * 舒张压Type = 0x0044 0 68，收缩压Type = 0x0045->0 69, 呼吸频率Type = 0x0046->0 70，
     * 体温Type = 0x0051->0 81，
     * 以tlv格式类型(2个字节)，长度（2个字节），
     * 值: 心电192+4 呼吸288+4 PPG192+4
     * 其他都定义2个字节（心率，血氧，舒张压，收缩压，呼吸频率，体温）（2+4）*6=36
     *
     *
     * 体温有小数，比较特殊，比如36.8  乘以10后看成368 进行传输
     */

    public RealTimeStatePacket(int userId,int serialNum,byte[] ecgData,byte[] respData,byte[] ppgData,short heartRate,short spo2,
                        short szPress,short ssPress,short resp, short temp){
        this.userId = userId;
        this.serialNum = serialNum;
        this.ecgData = ecgData;
        this.respData=respData;
        this.ppgData = ppgData;
        this.heartRate = heartRate;
        this.spo2 = spo2;
        this.szPress = szPress;
        this.ssPress = ssPress;
        this.resp = resp;
        this.temp =temp;
    }
    public byte[] buildPacket() {
        byte[] bao=new byte[SIZE];
        //version
        bao[0] =(byte) 2;
        //code
        bao[1] =(byte) 1;
        //length
        ByteUtil.short2bytes(bao,length,2);
        //serial
        ByteUtil.putInt(bao,serialNum,4);
        //userId
        ByteUtil.putInt(bao,userId,8);

        //ECG:tlv 12+196
        ByteUtil.short2bytes(bao, (short) 16,12);
        ByteUtil.short2bytes(bao, (short) 96,14);
        System.arraycopy(ecgData,0,bao,16,192);

        //呼吸tlv 12+196+292
        if(null==respData){
            respData = new byte[292];
        }
        ByteUtil.short2bytes(bao, (short) 18,208);
        ByteUtil.short2bytes(bao, (short) 96,210);
        System.arraycopy(respData,0,bao,212,288);

        //PPG:tlv12+196+292+196
        if(null==ppgData){
            ppgData = new byte[196];
        }
        ByteUtil.short2bytes(bao, (short) 65,500);
        ByteUtil.short2bytes(bao, (short) 96,502);
        System.arraycopy(ppgData,0,bao,504,192);

        //心率
        ByteUtil.short2bytes(bao, (short) 66,696);
        ByteUtil.short2bytes(bao, (short) 2,698);
        ByteUtil.short2bytes(bao, heartRate,700);

        //血氧
        ByteUtil.short2bytes(bao, (short) 67,702);
        ByteUtil.short2bytes(bao, (short) 2,704);
        ByteUtil.short2bytes(bao, spo2,706);

        //舒张压
        ByteUtil.short2bytes(bao, (short) 68,708);
        ByteUtil.short2bytes(bao, (short) 2,710);
        ByteUtil.short2bytes(bao, szPress,712);

        //收缩压
        ByteUtil.short2bytes(bao, (short) 69,714);
        ByteUtil.short2bytes(bao, (short) 2,716);
        ByteUtil.short2bytes(bao, ssPress,718);

        //呼吸频率
        ByteUtil.short2bytes(bao, (short) 69,720);
        ByteUtil.short2bytes(bao, (short) 2,722);
        ByteUtil.short2bytes(bao, resp,724);

        //体温
        ByteUtil.short2bytes(bao, (short) 81,726);
        ByteUtil.short2bytes(bao, (short) 2,728);
        ByteUtil.short2bytes(bao, temp,730);

        Log.d("bao==========", Arrays.toString(bao));
        return bao;
    }
}
