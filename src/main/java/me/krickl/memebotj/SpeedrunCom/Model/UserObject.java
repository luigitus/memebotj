package me.krickl.memebotj.SpeedrunCom.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class UserObject {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("names")
    @Expose
    private Names names;
    @SerializedName("weblink")
    @Expose
    private String weblink;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return names.getInternational();
    }

    public Names getNames() {
        return names;
    }

    public void setNames(Names names) {
        this.names = names;
    }

    public String getWeblink() {
        return weblink;
    }

    public void setWeblink(String weblink) {
        this.weblink = weblink;
    }

}
