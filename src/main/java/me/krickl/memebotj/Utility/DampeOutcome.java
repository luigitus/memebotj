package me.krickl.memebotj.Utility;

/**
 * Created by unlink on 5/27/2016.
 */
public class DampeOutcome {
    private int chance = -1;
    private String idStr = "";
    private int id = -1;

    public DampeOutcome(String idStr, int id, int chance) {
        this.idStr = idStr;
        this.id = id;
        this.chance = chance;
    }

    public int getChance() {
        return chance;
    }

    public void setChance(int chance) {
        this.chance = chance;
    }

    public String idStr() {
        return idStr;
    }

    public void idStr(String id) {
        this.idStr = id;
    }

    public String getIdStr() {
        return idStr;
    }

    public void setIdStr(String idStr) {
        this.idStr = idStr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
