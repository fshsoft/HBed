package com.java.health.care.bed.bean;

import com.contrarywind.interfaces.IPickerViewData;

import java.util.List;

/**
 * @author fsh
 * @date 2022/09/07 10:25
 * @Description 科室部门
 */
public class Dept implements IPickerViewData {
    private int id;
    private String name;
    private List<Region> regions;

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


    @Override
    public String getPickerViewText() {
        return this.name;
    }

    public List<Region> getRegions() {
        return regions;
    }

    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }
}
