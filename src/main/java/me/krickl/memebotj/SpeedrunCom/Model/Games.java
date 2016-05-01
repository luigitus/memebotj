package me.krickl.memebotj.SpeedrunCom.Model;

import java.util.ArrayList;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class Games {

    @SerializedName("data")
    @Expose
    private ArrayList<Game> data = new ArrayList<Game>();

    public ArrayList<Game> getData() {
        return data;
    }

    public void setData(ArrayList<Game> data) {
        this.data = data;
    }

}
