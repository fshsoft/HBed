/**
 * Copyright (C) Wuxi Microsens Tech Ltd. 2012-2020
 * All rights reserved.
 */
package com.java.health.care.bed.constant;

/**
 * @author
 */
public class ImplementConfig {
	public static final boolean DEBUG = true;
	/**
	 *
	 */
	public static final int ECG_SAMPLE_RATE = 300;
	
	/**
	 *
	 */
	public static final int ACC_SAMPLE_RATE = 100;
	
	/**
	 *
	 */
	public static final int SMS_INTERVAL = 10 * 60 * 1000;
	
	/**
	 *
	 */
	public static final int _1HOUR = 60 * 60 * 1000;
	
	/**
	 *
	 */
	public static final int _1MINUTE = 60 * 1000;
	
	/**
	 *
	 */	
	public static final int TLV_CODE_SYS_HEAD = 0x01;
	
	public static final int TLV_CODE_SYS_DATA = 0x02;
	
	public static final int TLV_CODE_SYS_DATA_TYPE_ECG = 0x10;
	public static final int TLV_CODE_SYS_DATA_TYPE_RSP = 0x1A;
	public static final int TLV_CODE_SYS_DATA_TYPE_ECG1 = 0x1010;
	public static final int TLV_CODE_SYS_DATA_TYPE_ECG2 =  0x2010;
	
	public static final int TLV_CODE_SYS_DATA_TYPE_MARKING =  0x11;
	
	public static final int TLV_CODE_SYS_DATA_TYPE_ACC = 0x20;
	public static final int TLV_CODE_SYS_DATA_TYPE_GYR = 0x21;
	public static final int TLV_CODE_SYS_DATA_TYPE_MAG = 0x22;

	//电量数据

	public static final int TLV_CODE_SYS_DATA_TYPE_ELECTRICITY = 0x30;

	//基准电压

	public static final int TLV_CODE_SYS_DATA_TYPE_ECG_BASE = 0x31;
	public static final int TLV_CODE_SYS_DATA_TYPE_ZERO_BIAS = 0x32;

	//系统错误标志

	public static final int TLV_CODE_SYS_ERROR = 0x40;

	//导联脱落

	public static final int SYS_ERROR_FLAG_LEADSOFF = 0x2;
	public static final int SYS_ERROR_FLAG_SSABN = 0x1;
	
	public static final int TLV_VERSION_ONE = 0x01;
	public static final int TLV_VERSION_TWO = 0x02;
	public static final int TLV_VERSION_THREE = 0x03;
	
	public static final byte EcgBase = 43;
	public static final byte ZeroBias =127;
	
}

