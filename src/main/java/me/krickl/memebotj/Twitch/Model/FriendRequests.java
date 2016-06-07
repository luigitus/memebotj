package me.krickl.memebotj.Twitch.Model;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FriendRequests {

    @SerializedName("_total")
    @Expose
    private Integer Total;
    @SerializedName("requests")
    @Expose
    private ArrayList<FriendRequest> requests = new ArrayList<FriendRequest>();

    public Integer getTotal() {
        return Total;
    }

    public void setTotal(Integer Total) {
        this.Total = Total;
    }

    public ArrayList<FriendRequest> getRequests() {
        return requests;
    }

    public void setRequests(ArrayList<FriendRequest> requests) {
        this.requests = requests;
    }

}
