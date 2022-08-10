package com.java.health.care.bed.model;

public class DataCmdPara {
	
	public enum DataCmdType {		
		e_preStart,
		eStart,
		eStop,
		eScore
	}
	
	public DataCmdPara() {
		cmdType = 0;
		userID = 0;
		userName = "";
		estimateID = 0;
	}
	
	public  byte cmdType;
	public  int userID;
	public String userName;
	public  int estimateID;
	
	
}
