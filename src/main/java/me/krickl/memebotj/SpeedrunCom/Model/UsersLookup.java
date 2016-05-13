package me.krickl.memebotj.SpeedrunCom.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class UsersLookup {

    @SerializedName("data")
    @Expose
    private ArrayList<UserObject> data = new ArrayList<UserObject>();

    public ArrayList<UserObject> getData() {
        return data;
    }

    public void setData(ArrayList<UserObject> data) {
        this.data = data;
    }

}
