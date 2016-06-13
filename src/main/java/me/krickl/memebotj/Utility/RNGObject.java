package me.krickl.memebotj.Utility;

import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Created by unlink on 5/27/2016.
 */
public class RNGObject {
    private int chance = -1;
    private String idStr = "";
    private int id = -1;
    private int range = -1;

    public RNGObject(String idStr, int id, int range, int chance) {
        this.idStr = idStr;
        this.id = id;
        this.chance = chance;
        this.range = range;
    }

    public static RNGObject rollUntilLast(int maxRange, ArrayList<RNGObject> next, int maxRolls) {
        if (maxRolls < 0) {
            maxRolls = next.size() / 2;
        }
        int rolls = 0;
        SecureRandom ran = new SecureRandom();
        do {
            if (rolls > maxRolls) {
                break;
            }
            next = rollObjects(ran.nextInt(maxRange), next);
            rolls++;
        } while (next.size() > 2);

        if (next.size() <= 0) {
            return null;
        }

        if (next.size() > 1) {
            return next.get(ran.nextInt(next.size() - 1));
        }

        return next.get(0);
    }

    public static ArrayList<RNGObject> rollObjects(int odds, ArrayList<RNGObject> objects) {
        ArrayList<RNGObject> possibleOutComes = new ArrayList<>();

        for (RNGObject RNGObjectT : objects) {
            if (odds <= RNGObjectT.getChance() && odds >= RNGObjectT.getRange()) {
                possibleOutComes.add(RNGObjectT);
            }
        }

        return possibleOutComes;
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

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }
}
