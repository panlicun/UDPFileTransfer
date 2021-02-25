package com.plc.core.model;

import java.util.List;

public class ReissueDataModel {

    private int threadIndex;

    private List<TextModel> datas;

    public List<TextModel> getDatas() {
        return datas;
    }

    public void setDatas(List<TextModel> datas) {
        this.datas = datas;
    }

    public int getThreadIndex() {
        return threadIndex;
    }

    public void setThreadIndex(int threadIndex) {
        this.threadIndex = threadIndex;
    }
}
