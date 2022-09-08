package com.java.health.care.bed.bean;

/**
 * @author fsh
 * @date 2022/08/29 09:01
 * @Description
 */
public class Token {
    private String value;
    private String tokenType;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
