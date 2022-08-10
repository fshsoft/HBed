/**
 * Copyright (C) Wuxi Microsens Tech Ltd. 2012-2020
 * All rights reserved.
 */
package com.microsenstech.ucarerg.process;


/**

 * 
 * @author Shaofeng Wang 
 */
public class SignalProcessor {
	
	static {
		System.loadLibrary("ucareRG");
	}
	
	private int pointer = 0;
	
	public SignalProcessor() {
		pointer = createObject();
	}
	
	public void recycle() {
		if(pointer != 0) {
			deleteObject(pointer);
		}
	}
	
	/**
	 * 创建对象，并返回句柄
	 * @return
	 */
	public native int createObject();

	/**

	 * @param pEcgObject
	 */
	public native void deleteObject(int pEcgObject);

	/**
	 * 处理数据

	 * @param dataEcgSeg	ECG	
	 * @param dataLength	数据长度
	 * @param heartrate		心率信息


	 * @return
	 */
	public native boolean callDataProcessing(int pEcgObject, short[] dataEcgSeg,
			int dataLength, int[] heartrate, double[] activity,
			int[] abnormalState);

	public void processData(short[] dataEcgSeg, int dataLength, int[] heartrate,
			double[] activity, int[] abnormalState) {
		this.callDataProcessing(pointer, dataEcgSeg, dataLength, heartrate,
				activity, abnormalState);
	}

    /**
     * */
//    public native boolean callDataProcessing(int pEcgObject, short[] dataEcgSeg,
//                                             int dataLength, int[] heartrate, double[] activity,
//                                             int[] abnormalState, short testSiteLen,int testState);
//
//    public void processData(short[] dataEcgSeg, int dataLength, int[] heartrate,
//                            double[] activity, int[] abnormalState, short testSiteLen,int testState) {
//        this.callDataProcessing(pointer, dataEcgSeg, dataLength, heartrate,
//                activity, abnormalState, testSiteLen,testState);
//    }
	/*
	 * 参数设置入口
	 * @data 


	 * */
	public native boolean callSetting(int pEcgObject, int[] data,
			int dataLength);

	/*
	 * 参数设置入口
	 * @EcgData
	 *    1）ECG原始数据
	 *    2）进行滤波，去除基线
	 * */
	public native boolean callSmoothBaseLine(int pEcgObject, short[] EcgData,
											 int dataLength);
	public void SmoothBaseLine(short[] EcgData, int dataLength) {
		this.callSmoothBaseLine(pointer, EcgData, dataLength);}
	
	public void SetSetting(int[] SettingData, int dataLength) {
		this.callSetting(pointer, SettingData, dataLength);}

	/*
	 * 参数设置入口
	 * @respData
	 *    1）reso原始数据
	 *    2）
	 * */
	public native int callRespProcess(int pEcgObject, int[] respData,
											 int dataLength);

	public int  RespProcess(int[] respData, int dataLength)
	{
		return this.callRespProcess(pointer, respData, dataLength);
	}




}
