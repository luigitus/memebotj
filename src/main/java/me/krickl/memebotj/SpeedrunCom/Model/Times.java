package me.krickl.memebotj.SpeedrunCom.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class Times {

    @SerializedName("primary")
    @Expose
    private String primary;
    @SerializedName("primary_t")
    @Expose
    private Integer primaryT;

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(String primary) {
        this.primary = primary;
    }

    public Integer getPrimaryT() {
        return primaryT;
    }

    public void setPrimaryT(Integer primaryT) {
        this.primaryT = primaryT;
    }

}
