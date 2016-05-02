package me.krickl.memebotj.Utility;

import me.krickl.memebotj.UserHandler;

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

    public Cooldown(int length, int uses) {
        cooldownLength = length;
        cooldownAfterUses = uses;
    }

    public Cooldown(int length) {
        cooldownLength = length;
    }

    public void startCooldown() {
        if(uses == cooldownAfterUses) {
            this.cooldownStart = (int)(System.currentTimeMillis() / 1000);
            this.cooldownEnd = (int)(System.currentTimeMillis() / 1000L) + this.cooldownLength;
            uses = 0;
        } else {
            cooldownAfterUses = cooldownAfterUses + 1;
        }
    }

    public boolean canContinue() {
        if (this.cooldownEnd > (int)(System.currentTimeMillis() / 1000)) {
            //this.cooldownEnd = this.cooldownEnd + this.cooldownEnd / 100 * 5 // make cooldown 5% longer if required
            return false;
        }

        return true;
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
