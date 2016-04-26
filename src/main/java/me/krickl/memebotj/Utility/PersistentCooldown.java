package me.krickl.memebotj.Utility;

import me.krickl.memebotj.UserHandler;

/**
 * This file is part of memebotj.
 * Created by unlink on 25/04/16.
 * These cooldowns have the ability to save to the database and be reloaded
 */
public class PersistentCooldown extends Cooldown {
    UserHandler sender = null;
    public PersistentCooldown(int lenght, UserHandler sender) {
        super(lenght);
        this.sender = sender;
    }
}
