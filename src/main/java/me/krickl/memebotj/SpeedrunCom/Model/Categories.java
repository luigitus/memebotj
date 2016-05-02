package me.krickl.memebotj.SpeedrunCom.Model;

import java.util.ArrayList;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class Categories {

    @SerializedName("data")
    @Expose
    private ArrayList<Category> data = new ArrayList<Category>();

    public ArrayList<Category> getData() {
        return data;
    }

    public void setData(ArrayList<Category> data) {
        this.data = data;
    }

}
