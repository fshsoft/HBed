package com.java.health.care.bed.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fsh
 * @date 2022/09/14 16:19
 * @Description
 */
public class UnFinishedPres implements Parcelable {
    //id
    private int preId;
    //类型
    private String preType;
    //处方时长（秒）
    private int duration;
    //执行日期
    private String execTime;

    //======================实现=Parcelable传List集合
    private List<Param> param;

    private Param paramInfo;

    private int flag;
    //===============================================
    protected UnFinishedPres(Parcel in) {
        preId = in.readInt();
        preType = in.readString();
        duration = in.readInt();
        execTime = in.readString();
        flag = in.readInt();
        //=============
        paramInfo = in.readParcelable(Param.class.getClassLoader());
        param = new ArrayList<>();
        in.readList(param,Param.class.getClassLoader());
    }

    public static final Creator<UnFinishedPres> CREATOR = new Creator<UnFinishedPres>() {
        @Override
        public UnFinishedPres createFromParcel(Parcel in) {
            return new UnFinishedPres(in);
        }

        @Override
        public UnFinishedPres[] newArray(int size) {
            return new UnFinishedPres[size];
        }
    };

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

    public List<Param> getParam() {
        return param;
    }

    public void setParam(List<Param> param) {
        this.param = param;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(preId);
        dest.writeString(preType);
        dest.writeInt(duration);
        dest.writeString(execTime);
        dest.writeInt(flag);
        //=============
        dest.writeParcelable(paramInfo,0);
        dest.writeList(param);
    }

    public Param getParamInfo() {
        return paramInfo;
    }

    public void setParamInfo(Param paramInfo) {
        this.paramInfo = paramInfo;
    }
}
