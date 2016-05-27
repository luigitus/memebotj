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

    public RNGObject(String idStr, int id, int chance) {
        this.idStr = idStr;
        this.id = id;
        this.chance = chance;
    }

    public static RNGObject rollUntilLast(int maxRange, ArrayList<RNGObject> objects) {
        ArrayList<RNGObject> next = objects;
        final int maxRolls = 10;
        int rolls = 0;
        SecureRandom ran = new SecureRandom();
        do {
            if(rolls > maxRolls) {
                break;
            }
            rollObjects(ran.nextInt(maxRange), next);
            rolls++;
        } while(next.size() > 2 );

        if(next.size() <= 0) {
            return null;
        }

        if(next.size() >= 1) {
            return next.get(ran.nextInt(next.size() - 1));
        }

        return next.get(0);
    }

    public static ArrayList<RNGObject> rollObjects(int odds, ArrayList<RNGObject> objects) {
        ArrayList<RNGObject> possibleOutComes = new ArrayList<>();

        for(RNGObject RNGObjectT : objects) {
            if(odds <= RNGObjectT.getChance()) {
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
}
