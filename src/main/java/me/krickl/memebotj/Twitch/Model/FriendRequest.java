package me.krickl.memebotj.Twitch.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FriendRequest {

    @SerializedName("requested_at")
    @Expose
    private String requestedAt;
    @SerializedName("user")
    @Expose
    private FriendUser user;

    public String getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(String requestedAt) {
        this.requestedAt = requestedAt;
    }

    public FriendUser getUser() {
        return user;
    }

    public void setUser(FriendUser user) {
        this.user = user;
    }

}