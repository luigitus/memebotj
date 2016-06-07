package me.krickl.memebotj.Twitch.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 15/05/16.
 */
public class KrakenToken {

    @SerializedName("valid")
    @Expose
    private Boolean valid;
    @SerializedName("authorization")
    @Expose
    private KrakenAuthorization authorization;
    @SerializedName("user_name")
    @Expose
    private String userName;

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    public KrakenAuthorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(KrakenAuthorization authorization) {
        this.authorization = authorization;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
