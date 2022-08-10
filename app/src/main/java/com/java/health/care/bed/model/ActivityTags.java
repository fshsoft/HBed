/**
 * Copyright (C) Wuxi Microsens Tech Ltd. 2012-2020
 * All rights reserved.
 */
package com.java.health.care.bed.model;

import android.content.Context;
import android.graphics.Color;


/**
 * 加速度数据标记
 * @author Shaofeng Wang 
 */
public class ActivityTags {
	/**
	 * Unknown
	 */
	public static final char TYPE_UNKNOWN = 0xff; 
	
	/**
	 * 躺
	 */
	public static final char TYPE_LYING = 4;
	
	/**
	 * 站、坐
	 */
	public static final char TYPE_UPRIGHT = 2;
	
	/**
	 * 走
	 */
	public static final char TYPE_WALKING = 1;
	
	/**
	 * 跑
	 */
	public static final char TYPE_RUNNING = 3;
	
	/**
	 * 摔
	 */
	public static final char TYPE_FALLING = 5;
	
	public ActivityTags(int type) {
		
	}
	
	public static int getColor(char type) {
		switch(type) {
		case TYPE_UNKNOWN:
			return Color.BLACK;
		case TYPE_LYING:
			return Color.BLUE;
		case TYPE_UPRIGHT:
			return Color.YELLOW;
		case TYPE_WALKING:
			return Color.GREEN;
		case TYPE_RUNNING:
			return Color.MAGENTA;
		case TYPE_FALLING:
			return Color.RED;
		}
		return Color.BLACK;
	}
	
	public static String getString(Context context, char type) {
	/*	switch(type) {
		case TYPE_UNKNOWN:
			return context.getString(R.string.tag_unknown);
		case TYPE_LYING:
			return context.getString(R.string.tag_lying);
		case TYPE_UPRIGHT:
			return context.getString(R.string.tag_upright);
		case TYPE_WALKING:
			return context.getString(R.string.tag_walking);
		case TYPE_RUNNING:
			return context.getString(R.string.tag_running);
		case TYPE_FALLING:
			return context.getString(R.string.tag_falling);
		}*/
//		return context.getString(R.string.tag_unknown);
		return "未知";
	}
}
