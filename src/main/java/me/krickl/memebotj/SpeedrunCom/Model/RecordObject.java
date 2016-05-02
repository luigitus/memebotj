package me.krickl.memebotj.SpeedrunCom.Model;

import java.util.ArrayList;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class RecordObject {

    @SerializedName("weblink")
    @Expose
    private String weblink;
    @SerializedName("game")
    @Expose
    private String game;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("level")
    @Expose
    private String level;
    @SerializedName("runs")
    @Expose
    private ArrayList<RankingObject> runs = new ArrayList<RankingObject>();

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public ArrayList<RankingObject> getRuns() {
        return runs;
    }

    public void setRuns(ArrayList<RankingObject> runs) {
        this.runs = runs;
    }

}
