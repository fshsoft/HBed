package com.java.health.care.bed.bean;

import java.util.List;

/**
 * @author fsh
 * @date 2022/09/07 10:25
 * @Description 科室部门
 */
public class Dept {
    private int id;
    private String name;
    private List<Region> regionList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Region> getRegionList() {
        return regionList;
    }

    public void setRegionList(List<Region> regionList) {
        this.regionList = regionList;
    }
}
