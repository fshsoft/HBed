package com.java.health.care.bed.model;

import android.content.Context;
import java.util.Arrays;

/**
 * 心电图标记
 * @author 
 */
public class DevicePacket {

	public static final int ECG_IN_PACKET = 96;
	
	public static final int ECG_SAMPLE_RATE = 200;

	
	/**
	 * 偏移量
	 */	
	//public int offset = 0;
	
	/**
	 * 心电数据
	 */
	//public boolean bPpgData;
	
	//public byte[] PpgData;
	
	
	// 发现产生了新的R波,获得新增或更新的参数
	public boolean bHasNew;
	
	public float rrNew;
	public float scoreNew;
	
	public float sdnn;
	public float sdsd;
	public float lf;
	public float hf;
	public float lh;
	
	public float psdH;
	public float psdM;
	public float psdL;
	
	public float coherenceH;
	public float coherenceM;
	public float coherenceL;
	
	public float maxPsd;
	public float respRate;
	/*
	public boolean bFinalScoreReport;
	
	public float finalScore;
	public float adjustmentAbility;
	public float stability;
	*/
	
	public boolean mFingerIn;



	/**
	 * 心电数据
	 */
	public byte[] data;


	public byte[] ecgdata;


	public short[] secgdata;
	/**
	 * 加速度数据
	 */
	//public byte[] AccData;
	public short[] AccData;

	/**
	 * 心率
	 */
	public int heartRate;

	/**
	 * 呼吸数据
	 */
	public int[] irspData;

	/**
	 * 偏移量
	 */
	public int offset = 0;
	/**
	 * 连接偏移量
	 */
	public int connOffset = 0;

	public short sBaseHeight;
	public short sBiaHeight;

	public byte EcgBase = 43;
	public byte ZeroBias = 127;
	public short systemErr = 0;


	/**
	 * tag
	 */
	public char activityTag = ActivityTags.TYPE_UNKNOWN;

	/**
	 * 运动强度
	 */
	public double intensity = 0.0f;

	/**
	 * 步伐频率
	 */
	public int stepRate = 0;

	/**
	 * 代谢当量
	 */
	public float met = 0;

	/**
	 * 异常类型
	 */
	public int abnStates;

	/**
	 * 呼吸
	 */
	public int resp;

	/**
	 * 呼吸
	 */
	public int score;
	/**
	 * 若结果不为0，则表示存在当前mask所代表的异常
	 *
	 * @param mask
	 * @return
	 */

	/**
	 * Ecg打标
	 */
	public int ecgmark;
	/**
	 * 导联类型，单导联或三导联
	 */
	public int leadtype;


	public int getAbnStates(int mask) {
		return (abnStates & mask);
	}

	public Context context = null;


	public DevicePacket(int offset, byte[] data, byte[] ecgdatas, short[] secgdatas,int[] irspData,int heartRate, int abnStates,
						char activityTag, double intensity, int stepRate, short[] AccData,
						float met, Context context) {
		super();
		this.offset = offset;
		this.data = data;
		this.secgdata = secgdatas;
		this.ecgdata = ecgdatas;
		this.irspData = irspData;
		this.heartRate = heartRate;
		this.abnStates = abnStates;
		this.activityTag = activityTag;
		this.intensity = intensity;
		this.stepRate = stepRate;
		this.met = met;
		this.AccData = AccData;
		this.context = context;

	}

	public DevicePacket() {
		super();
		
		//bPpgData = false;
		//PpgData = null;
		bHasNew = false;
		//bFinalScoreReport = false;
		mFingerIn = true;
	}

	@Override
	public String toString() {
		return "DevicePacket [offset=" + offset + ", data="
				+ Arrays.toString(data) + ", heartRate=" + heartRate
				+ ", abnStates=" + abnStates + ", activityTag=" + activityTag
				+ ", intensity=" + intensity + ", stepRate=" + stepRate + "]"+" , ecgdata="+Arrays.toString(ecgdata);
	}
	
}
