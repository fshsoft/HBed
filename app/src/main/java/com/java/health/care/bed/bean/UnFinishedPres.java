package com.java.health.care.bed.bean;

/**
 * @author fsh
 * @date 2022/09/14 16:19
 * @Description
 */
public class UnFinishedPres {
    //id
    private int preId;
    //类型
    private String preType;
    //处方时长（秒）
    private int duration;
    //执行日期
    private String execTime;

    public int getPreId() {
        return preId;
    }

    public void setPreId(int preId) {
        this.preId = preId;
    }

    public String getPreType() {
        return preType;
    }

    public void setPreType(String preType) {
        this.preType = preType;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getExecTime() {
        return execTime;
    }

    public void setExecTime(String execTime) {
        this.execTime = execTime;
    }
}
