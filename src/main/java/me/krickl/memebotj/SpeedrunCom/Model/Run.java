package me.krickl.memebotj.SpeedrunCom.Model;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class Run {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("weblink")
    @Expose
    private String weblink;
    @SerializedName("game")
    @Expose
    private String game;
    @SerializedName("level")
    @Expose
    private String level;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("players")
    @Expose
    private List<Player> players = new ArrayList<Player>();
    @SerializedName("times")
    @Expose
    private Times times;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWeblink() {
        return weblink;
    }

    public void setWeblink(String weblink) {
        this.weblink = weblink;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Times getTimes() {
        return times;
    }

    public void setTimes(Times times) {
        this.times = times;
    }

    @Override
    public String toString() {
        return "Run{" +
                "id='" + id + '\'' +
                ", weblink='" + weblink + '\'' +
                ", game='" + game + '\'' +
                ", level='" + level + '\'' +
                ", category='" + category + '\'' +
                ", players=" + players +
                ", times=" + times +
                '}';
    }
}
