package com.microsenstech.ucarerg.device;

public final class PacketParse {
	static {
	        System.loadLibrary("packetParse");
	}
	
	public static native boolean parsePacket(byte[] packet);
	public static native int getPacketType();
	public static native int getPacketVersion();
	public static native int[] getTypes();
	public static native byte[] getTlv(int type);

}

