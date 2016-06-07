package me.krickl.memebotj.Twitch.Model;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 15/05/16.
 */
public class KrakenAuthorization {

    @SerializedName("scopes")
    @Expose
    private ArrayList<String> scopes = new ArrayList<String>();
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public ArrayList<String> getScopes() {
        return scopes;
    }

    public void setScopes(ArrayList<String> scopes) {
        this.scopes = scopes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

}