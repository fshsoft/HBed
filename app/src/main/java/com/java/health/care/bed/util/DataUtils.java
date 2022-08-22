package com.java.health.care.bed.util;

import android.util.Log;
import java.math.BigInteger;

/**
 * @author fsh
 * @date 2022/08/22 13:33
 * @Description
 */
public class DataUtils {
    public static final String TAG = "DataUtils";
    //获取体温计十进制数据
    public static double getIRTData(byte[] data){

            String str = new BigInteger(1,data).toString(16);
            Log.d(TAG,str+"=====");//22656a01=====36.2
            String lStr = str.substring(4,6);
            String hStr = str.substring(6,8);
            Log.d(TAG,"hStr:"+hStr+"==lStr:"+lStr);
            int num = Integer.parseInt(hStr+lStr,16);
            Log.d(TAG,num+"==");
            double numIRT = (double) num/10;
            Log.d(TAG+"temp:",numIRT+"==");
            return numIRT;


    }

    //获取血氧
    public static int getSPO2Data(byte[] data){
        int num = 0;
        String o2Data = new BigInteger(1, data).toString(16);
            //包头 1字节
            String head1Data = o2Data.substring(2,o2Data.length());
            String head1Str = o2Data.substring(0,2);
            Log.i("----ff",head1Str+"");
            if(head1Str.equals("ff")){
                String head2Str = head1Data.substring(0,2);
                String head2Data = head1Data.substring(2,head1Data.length());
                Log.i("----fe",head2Str+"");
                if(head2Str.equals("fe")){
                    //命令id95
                    String id= head2Data.substring(6,8);
                    //数据data1:
                    String head3Data = head2Data.substring(8,head2Data.length());
                    Log.i("----id",id+"");
                    if(id.equals("95")){
                        String O2 = head3Data.substring(4,6);
                        Log.i("----O2",O2+"");
                        num  = Integer.parseInt(O2,16);
                        Log.i("--------oo",num+"==");
                        if(num>100){
                            num =0;
                        }

                    }
                }
            }
        return num;
    }

    //获取血压
    public static String getSBPData(byte[] data){
            String hp = String.valueOf(data[6] & 0xff);
            String lp = String.valueOf(data[8] & 0xff);
            return hp+"/"+lp;
    }

}
