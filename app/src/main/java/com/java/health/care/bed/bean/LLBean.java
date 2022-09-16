package com.java.health.care.bed.bean;

/**
 * @author fsh
 * @date 2022/09/16 14:53
 * @Description 理疗
 */
public class LLBean {
    private int preId;
    private String startTime;
    private String endTime;
    private int duration;

    public int getPreId() {
        return preId;
    }

    public void setPreId(int preId) {
        this.preId = preId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
