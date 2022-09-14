package com.java.health.care.bed.bean;

import java.util.List;

/**
 * @author fsh
 * @date 2022/07/29 14:47
 * @Description
 */
public class Prescription {
    private String id;
    private List<FinishedPres> finishedPresList;
    private List<UnFinishedPres> unFinishedPresList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<FinishedPres> getFinishedPresList() {
        return finishedPresList;
    }

    public void setFinishedPresList(List<FinishedPres> finishedPresList) {
        this.finishedPresList = finishedPresList;
    }

    public List<UnFinishedPres> getUnFinishedPresList() {
        return unFinishedPresList;
    }

    public void setUnFinishedPresList(List<UnFinishedPres> unFinishedPresList) {
        this.unFinishedPresList = unFinishedPresList;
    }
}
