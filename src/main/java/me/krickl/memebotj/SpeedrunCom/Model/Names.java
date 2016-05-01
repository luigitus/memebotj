package me.krickl.memebotj.SpeedrunCom.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class Names {

    @SerializedName("international")
    @Expose
    private String international;
    @SerializedName("japanese")
    @Expose
    private String japanese;

    public String getInternational() {
        return international;
    }

    public void setInternational(String international) {
        this.international = international;
    }

    public String getJapanese() {
        return japanese;
    }

    public void setJapanese(String japanese) {
        this.japanese = japanese;
    }

}