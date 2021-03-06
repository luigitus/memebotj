package me.krickl.memebotj.SpeedrunCom.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class WRLookup {

    @SerializedName("data")
    @Expose
    private RecordObject data;

    public RecordObject getData() {
        return data;
    }

    public void setData(RecordObject data) {
        this.data = data;
    }

}
