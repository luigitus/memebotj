package me.krickl.memebotj.Utility;

import org.bson.Document;

/**
 * This file is part of memebotj.
 * Created by unlink on 07/04/16.
 */
public class Cooldown {

    int cooldownLength = 0;
    int cooldownStart = 0;
    int cooldownEnd = 0;
    int cooldownAfterUses = 0;
    int uses = 0;
    Document doc = new Document();

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
    }

    public Cooldown(int length) {
        cooldownLength = length;
    }

    public void startCooldown() {
        if (uses == cooldownAfterUses) {
            this.cooldownStart = (int) (System.currentTimeMillis() / 1000);
            this.cooldownEnd = (int) (System.currentTimeMillis() / 1000L) + this.cooldownLength;
            uses = 0;
        } else {
            cooldownAfterUses = cooldownAfterUses + 1;
        }
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
}
