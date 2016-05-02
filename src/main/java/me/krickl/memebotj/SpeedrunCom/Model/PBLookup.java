package me.krickl.memebotj.SpeedrunCom.Model;

import java.util.ArrayList;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class PBLookup {

    @SerializedName("data")
    @Expose
    private ArrayList<RunObject> data = new ArrayList<RunObject>();

    public ArrayList<RunObject> getData() {
        return data;
    }

    public void setData(ArrayList<RunObject> data) {
        this.data = data;
    }

}
