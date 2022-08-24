package com.java.health.care.bed.model;


/**
 * 数据接收器
 * @author 
 */

public interface DataReceiver {

	/**
	 * 收到数据
	 */
	void onDataReceived(DevicePacket packet);

	void onDataReceived(BPDevicePacket packet);


	/**
	 * 收到数据
	 */
	void onDataReceived(DevicePacket packet, int battery);



	void onDataReceived(EstimateRet ret);

	
	/**
	 * 连接关闭
	 */
	void onDeviceDisConnected();
	
	/**
	 * 连接成功
	 */
	void onDeviceConnected(long startTime);
	
	/**
	 * 开始连接
	 */
	void onStartToConnect();

	
}
