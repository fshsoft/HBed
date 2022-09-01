package com.java.health.care.bed.base;


import com.java.health.care.bed.net.MainUtil;

/**
 * @description:
 */

public class BaseEntry<T> {

    private String code;
    private String msg;
    private String current;
    private String total;
    private T data;

    public boolean isSuccess(){
        return getCode().equals(MainUtil.SUCCESS_CODE);
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }
}
