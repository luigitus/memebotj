package me.krickl.memebotj.Twitch.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class Stream {

    @SerializedName("_id")
    @Expose
    private long Id;
    @SerializedName("game")
    @Expose
    private String game;
    @SerializedName("viewers")
    @Expose
    private Integer viewers;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("video_height")
    @Expose
    private Integer videoHeight;
    @SerializedName("average_fps")
    @Expose
    private Integer averageFps;
    @SerializedName("delay")
    @Expose
    private Integer delay;
    @SerializedName("is_playlist")
    @Expose
    private Boolean isPlaylist;
    @SerializedName("channel")
    @Expose
    private Channel channel;

    public long getId() {
        return Id;
    }

    public void setId(Integer Id) {
        this.Id = Id;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public Integer getViewers() {
        return viewers;
    }

    public void setViewers(Integer viewers) {
        this.viewers = viewers;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(Integer videoHeight) {
        this.videoHeight = videoHeight;
    }

    public Integer getAverageFps() {
        return averageFps;
    }

    public void setAverageFps(Integer averageFps) {
        this.averageFps = averageFps;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Boolean getIsPlaylist() {
        return isPlaylist;
    }

    public void setIsPlaylist(Boolean isPlaylist) {
        this.isPlaylist = isPlaylist;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

}
