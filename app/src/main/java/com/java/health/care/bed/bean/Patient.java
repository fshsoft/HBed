package com.java.health.care.bed.bean;

/**
 * @author fsh
 * @date 2022/07/29 14:51
 * @Description
 */
public class Patient {
    private int bunkId; //床位号id
    private String bunkNo; //床位编号
    private int patientId;//患者ID
    private String patientName;
    private String age;
    private String sex;
    private int height;
    private int weight;
    private int armLength;
    private int hospitalId;

    public int getBunkId() {
        return bunkId;
    }

    public void setBunkId(int bunkId) {
        this.bunkId = bunkId;
    }

    public String getBunkNo() {
        return bunkNo;
    }

    public void setBunkNo(String bunkNo) {
        this.bunkNo = bunkNo;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getArmLength() {
        return armLength;
    }

    public void setArmLength(int armLength) {
        this.armLength = armLength;
    }

    public int getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(int hospitalId) {
        this.hospitalId = hospitalId;
    }
}
