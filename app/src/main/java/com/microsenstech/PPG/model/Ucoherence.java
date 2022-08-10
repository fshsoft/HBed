package com.microsenstech.PPG.model;



public final class Ucoherence {

	static {
	        System.loadLibrary("ucoherence");			
	}
	
	private static final String TAG = "Ucoherence";	

	
	public static native void getPPGData(float[] ppgdata);
	public static native void parseEcgData(byte[] data
			, boolean[] ppgret
			, float[] ppgdata
			, float[] newret);
	public static native void sendDataProcessCmd(byte cmdType
			, int userID
			, String userName
			, int estimateID
			, boolean[] finalscore
			, float[] scorerpt);

	public static native void appendRRData(float[] data);

	public static native void setting( float freqCentre);


}
