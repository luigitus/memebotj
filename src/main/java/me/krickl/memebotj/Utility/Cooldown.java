package me.krickl.memebotj.Utility;

import me.krickl.memebotj.Database.JSONInterface;
import org.bson.Document;
import org.json.simple.JSONObject;

/**
 * This file is part of memebotj.
 * Created by unlink on 07/04/16.
 */
public class Cooldown implements JSONInterface {

    int cooldownLength = 0;
    int cooldownStart = 0;
    int cooldownEnd = 0;
    int cooldownAfterUses = 0;
    int uses = 0;
    Document doc = new Document();

    public static Document createCooldownDocument(int lenght, int uses) {
        Document doc = new Document();

        doc.put("start", -1);
        doc.put("end", 0);
        doc.put("uses", lenght);
        doc.put("cdafteruses", uses);
        doc.put("uses", 0);

        return doc;
    }

    public Cooldown(Document doc) {
        this.doc = doc;
        cooldownLength = (int)doc.getOrDefault("lenght", cooldownLength);
        cooldownStart = (int)doc.getOrDefault("start", cooldownStart);
        cooldownEnd = (int)doc.getOrDefault("end", cooldownEnd);
        uses = (int)doc.getOrDefault("uses", uses);
        cooldownAfterUses = (int)doc.getOrDefault("cdafteruses", cooldownAfterUses);
    }

    public Cooldown(int length, int uses) {
        cooldownLength = length;
        cooldownAfterUses = uses;
        doc = Cooldown.createCooldownDocument(length, uses);
    }

    public Cooldown(int length) {
        cooldownLength = length;
    }

    public void startCooldown(int offset) {
        if (uses == cooldownAfterUses) {
            this.cooldownStart = (int) (System.currentTimeMillis() / 1000);
            this.cooldownEnd = (int) (System.currentTimeMillis() / 1000L) + this.cooldownLength + offset;
            uses = 0;
        } else {
            cooldownAfterUses = cooldownAfterUses + 1;
        }
    }

    public void startCooldown() {
        startCooldown(0);
    }

    public boolean canContinue() {
        return this.cooldownEnd <= (int) (System.currentTimeMillis() / 1000);

    }

    /***
     * This method returns a document that can be used to store the cooldown in database
     * @return
     */
    public Document getDoc() {
        doc = new Document();
        doc.append("lenght", cooldownLength);
        doc.append("start", cooldownStart);
        doc.append("end", cooldownEnd);
        doc.append("uses", uses);
        doc.append("cdafteruses", cooldownAfterUses);
        return doc;
    }

    public int getCooldownLength() {
        return cooldownLength;
    }

    public void setCooldownLength(int cooldownLength) {
        this.cooldownLength = cooldownLength;
    }

    public int getCooldownStart() {
        return cooldownStart;
    }

    public void setCooldownStart(int cooldownStart) {
        this.cooldownStart = cooldownStart;
    }

    public int getCooldownEnd() {
        return cooldownEnd;
    }

    public void setCooldownEnd(int cooldownEnd) {
        this.cooldownEnd = cooldownEnd;
    }

    @Override
    public JSONObject toJSONObject() {
        return null;
    }

    @Override
    public String toJSONSString() {
        return null;
    }

    @Override
    public boolean fromJSON(String jsonString) {
        return false;
    }
}
