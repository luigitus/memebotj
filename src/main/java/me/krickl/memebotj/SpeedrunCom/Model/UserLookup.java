package me.krickl.memebotj.SpeedrunCom.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class UserLookup {

    @SerializedName("data")
    @Expose
    private UserObject data;

    public UserObject getData() {
        return data;
    }

    public void setData(UserObject data) {
        this.data = data;
    }

}
