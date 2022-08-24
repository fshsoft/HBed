package com.java.health.care.bed.model;

/**
 * @author fsh
 * @date 2022/08/24 09:08
 * @Description
 */
public class BPDevicePacket {

    private short[] sEcgData;
    private short[] sPpgData;
    private int heartRate;
    private int sSzPressDataData;
    private int sSsPressDataData;

    public BPDevicePacket(short[] sEcgData,short[] sPpgData,int heartRate,int sSzPressDataData,int sSsPressDataData){
        this.sEcgData = sEcgData;
        this.sPpgData = sPpgData;
        this.heartRate = heartRate;
        this.sSzPressDataData = sSzPressDataData;
        this.sSsPressDataData = sSsPressDataData;
    }
    public short[] getsEcgData() {
        return sEcgData;
    }

    public void setsEcgData(short[] sEcgData) {
        this.sEcgData = sEcgData;
    }

    public short[] getsPpgData() {
        return sPpgData;
    }

    public void setsPpgData(short[] sPpgData) {
        this.sPpgData = sPpgData;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getsSzPressDataData() {
        return sSzPressDataData;
    }

    public void setsSzPressDataData(int sSzPressDataData) {
        this.sSzPressDataData = sSzPressDataData;
    }

    public int getsSsPressDataData() {
        return sSsPressDataData;
    }

    public void setsSsPressDataData(int sSsPressDataData) {
        this.sSsPressDataData = sSsPressDataData;
    }
}
