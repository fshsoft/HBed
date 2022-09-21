package com.java.health.care.test;

import com.blankj.utilcode.util.SPUtils;
import com.java.health.care.bed.util.ByteUtil;

import java.util.Arrays;

/**
 * @author fsh
 * @date 2022/09/07 17:05
 * @Description
 */
public class Test {
    public static void main(String[] args) {
        int2bytes(918);
        short2bytes((short) 478);
//        short2bytes((short) 366);
//        updateArray();
//        insertArray();

    }

    private static void int2bytes(int num){
        byte[] bytes = new byte[4];
        ByteUtil.putInt(bytes,num,0);

        System.out.println(Arrays.toString(bytes));
    }


    private static void short2bytes(short num){
        byte[] bytes = new byte[2];
        ByteUtil.short2bytes(bytes,  num,0);

        System.out.println(Arrays.toString(bytes));
    }


    /**
     * 数组更改数组的某一段
     */
    private static void updateArray(){
        byte[] bytes1 = {2,1,0,15,0,0,0,0,2,3,4,5,6,7,8}; //一整包数据
        byte[] bytes2 = {1,1,1,1}; //代表用户id
        //下面代表说明：bytes2数组从下标0开始，复制到byte1数组从下标4开始，复制4个数据， 相当于把bytes1里面的0,0,0,0替换为1,1,1,1,
        System.arraycopy(bytes2,0,bytes1,4,4);
        System.out.println(Arrays.toString(bytes1));
    }

    /**
     * 数组插入数组的某一段 元数组{2,1,0,15,8,8,8,8,8,8,8,8}
     * 想变为{2,1,0,15,1,1,1,1,8,8,8,8,8,8,8,8}
     * 拆开组装
     */
    private static void insertArray(){
        byte[] bytes1 = {2,1,0,15};
        byte[] bytes2 = {1,1,1,1};
        byte[] bytes3 = {8,8,8,8,8,8,8};
        byte[] bytes = new byte[bytes1.length+bytes2.length+bytes3.length];
        System.arraycopy(bytes1,0,bytes,0,bytes1.length);
        System.arraycopy(bytes2,0,bytes,4,bytes2.length);
        System.arraycopy(bytes3,0,bytes,8,bytes3.length);

        System.out.println(Arrays.toString(bytes));
    }
}
