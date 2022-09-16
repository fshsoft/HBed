package com.java.health.care.bed.bean;

import java.util.List;

/**
 * @author fsh
 * @date 2022/07/29 14:47
 * @Description
 */
public class Prescription {
    private String id;
    private List<FinishedPres> finished;
    private List<UnFinishedPres> unfinished;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public List<FinishedPres> getFinished() {
        return finished;
    }

    public void setFinished(List<FinishedPres> finished) {
        this.finished = finished;
    }

    public List<UnFinishedPres> getUnfinished() {
        return unfinished;
    }

    public void setUnfinished(List<UnFinishedPres> unfinished) {
        this.unfinished = unfinished;
    }
}
