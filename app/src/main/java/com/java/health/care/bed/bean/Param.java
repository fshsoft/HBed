package com.java.health.care.bed.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author fsh
 * @date 2022/09/20 14:29
 * @Description
 *             "key": "null",
 *             "value": "3",
 *             "sort": null
 */
public class Param implements Parcelable {
    private String key;
    private String value;
    private String sort;

    protected Param(Parcel in) {
        key = in.readString();
        value = in.readString();
        sort = in.readString();
    }

    public static final Creator<Param> CREATOR = new Creator<Param>() {
        @Override
        public Param createFromParcel(Parcel in) {
            return new Param(in);
        }

        @Override
        public Param[] newArray(int size) {
            return new Param[size];
        }
    };

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(key);
        dest.writeString(value);
        dest.writeString(sort);
    }
}
