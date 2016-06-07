package me.krickl.memebotj.SpeedrunCom.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/06/16.
 */
public class WRSLookup {

    @SerializedName("data")
    @Expose
    private ArrayList<RecordObject> data;

    public ArrayList<RecordObject> getData() {
        return data;
    }

    public void setData(ArrayList<RecordObject> data) {
        this.data = data;
    }

}
