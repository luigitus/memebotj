package me.krickl.memebotj.Twitch.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 15/05/16.
 */
public class KrakenRoot {

    @SerializedName("identified")
    @Expose
    private Boolean identified;
    @SerializedName("token")
    @Expose
    private KrakenToken token;

    public Boolean getIdentified() {
        return identified;
    }

    public void setIdentified(Boolean identified) {
        this.identified = identified;
    }

    public KrakenToken getToken() {
        return token;
    }

    public void setToken(KrakenToken token) {
        this.token = token;
    }

}
