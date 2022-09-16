package com.java.health.care.bed.net;

import android.util.Log;

import com.java.health.care.bed.util.ByteUtil;
import com.microsenstech.ucarerg.TlvBox;

import java.util.Arrays;

/**
 * @author fsh
 * @date 2022/09/15 10:11
 * @Description  需要先获取body长度 然后加上header长度，最后组包
 */
public class RealTimeStatePacket {

    /**
     * 头部header version 1  code 1  桢长度 2  serial 4  用户id 4
     * 1+1+2+4+4 = 12
     */
    //版本协议
    private byte version = 2;

    //编码
    private byte code =1;

    //帧长度
    private short length =0;

    //序号,从0开始
    public int serialNum = 0;

    //用户ID
    private int userId;

    //Preserve域为预留域 为0
    private short preserve =0;

    //Check Code 数据校验码 32
    private short checkCode = 32;

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
     * 心电Type =      0x0010->0 16
     * 呼吸Type =      0x0012->0 18
     * PPG数据Type =   0x0041->0 65
     * 心率Type =      0x0042->0 66
     * 血氧Type =      0x0043->0 67
     * 舒张压Type =    0x0044 0 68
     * 收缩压Type =    0x0045->0 69
     * 呼吸频率Type =   0x0046->0 70
     * 体温Type =      0x0047->0 71
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
        TlvBox tlvBox = new TlvBox();


        /**
         * body TLV TLVBox
         */
        if(null!=ecgData)tlvBox.putBytesValue(18,ecgData);
        if(null!=respData)tlvBox.putBytesValue(26,respData);
        if(null!=ppgData)tlvBox.putBytesValue(65,ppgData);
        if(heartRate!=0)tlvBox.putShortValue(66,heartRate);
        if(spo2!=0)tlvBox.putShortValue(67,spo2);
        if(szPress!=0)tlvBox.putShortValue(68,szPress);
        if(ssPress!=0)tlvBox.putShortValue(69,ssPress);
        if(resp!=0)tlvBox.putShortValue(70,resp);
        if(temp!=0)tlvBox.putShortValue(71,temp);

        byte[] bytes = tlvBox.serialize();
        int length = bytes.length;
        byte[] bao=new byte[length];

        /**
         * header头为16位
         */
        //version 1
        bao[0] =(byte) 2;
        //code 1
        bao[1] =(byte) 1;
        //length 2
        ByteUtil.short2bytes(bao, (short) length,2);
        //serial 4
        ByteUtil.putInt(bao,serialNum,4);
        //userId  4
        ByteUtil.putInt(bao,userId,8);
        //preserve预留 2
        ByteUtil.short2bytes(bao,preserve,12);
        //Check Code校验码 2
        ByteUtil.short2bytes(bao,checkCode,14);

        System.arraycopy(bytes,0,bao,16,length-16);



//
//        //ECG:tlv 12+196
//        ByteUtil.short2bytes(bao, (short) 16,12);
//        ByteUtil.short2bytes(bao, (short) 96,14);
//        System.arraycopy(ecgData,0,bao,16,192);
//
//        //呼吸tlv 12+196+292
//        if(null==respData){
//            respData = new byte[292];
//        }
//        ByteUtil.short2bytes(bao, (short) 18,208);
//        ByteUtil.short2bytes(bao, (short) 96,210);
//        System.arraycopy(respData,0,bao,212,288);
//
//        //PPG:tlv12+196+292+196
//        if(null==ppgData){
//            ppgData = new byte[196];
//        }
//        ByteUtil.short2bytes(bao, (short) 65,500);
//        ByteUtil.short2bytes(bao, (short) 96,502);
//        System.arraycopy(ppgData,0,bao,504,192);
//
//        //心率
//        ByteUtil.short2bytes(bao, (short) 66,696);
//        ByteUtil.short2bytes(bao, (short) 2,698);
//        ByteUtil.short2bytes(bao, heartRate,700);
//
//        //血氧
//        ByteUtil.short2bytes(bao, (short) 67,702);
//        ByteUtil.short2bytes(bao, (short) 2,704);
//        ByteUtil.short2bytes(bao, spo2,706);
//
//        //舒张压
//        ByteUtil.short2bytes(bao, (short) 68,708);
//        ByteUtil.short2bytes(bao, (short) 2,710);
//        ByteUtil.short2bytes(bao, szPress,712);
//
//        //收缩压
//        ByteUtil.short2bytes(bao, (short) 69,714);
//        ByteUtil.short2bytes(bao, (short) 2,716);
//        ByteUtil.short2bytes(bao, ssPress,718);
//
//        //呼吸频率
//        ByteUtil.short2bytes(bao, (short) 69,720);
//        ByteUtil.short2bytes(bao, (short) 2,722);
//        ByteUtil.short2bytes(bao, resp,724);
//
//        //体温
//        ByteUtil.short2bytes(bao, (short) 81,726);
//        ByteUtil.short2bytes(bao, (short) 2,728);
//        ByteUtil.short2bytes(bao, temp,730);
//
        Log.d("bao==========", Arrays.toString(bao));
        return bao;
    }
}
