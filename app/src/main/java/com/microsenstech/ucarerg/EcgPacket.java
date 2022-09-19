package com.microsenstech.ucarerg;

public enum EcgPacket {
	
	Ecg(0x0010),
	Acc(0x0010),
	Gyro(0x0021),
	Battery(0x0030),
	PPG(0x0041),
	HeartRate(0x0042),
	Spo2(0x0043),
	DiaBp(0x0044),
	SysBp(0x0045), //收缩压
	RespRate(0x0046),
	RIndex(0x0047),
	Motion(0x0048),
	Step(0x0049),
	//PatientId(0x4000),
	StartTime(0x0050),
	;
	
	
	public int getType()
	{
		return type;
	}
	private short type;
	EcgPacket(int id)
	{
		type = (short)id;
	}
	
	public static int samplieRate = 300;
	
	public static int eachPackentEcgConts = 300;

}
