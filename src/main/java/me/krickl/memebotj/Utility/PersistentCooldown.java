package me.krickl.memebotj.Utility;

import me.krickl.memebotj.UserHandler;

/**
 * This file is part of memebotj.
 * Created by unlink on 25/04/16.
 * These cooldowns have the ability to save to the database and be reloaded
 */
public class PersistentCooldown extends Cooldown {
    String id = null;

    public PersistentCooldown(int length, int uses, String id) {
        super(length);
        this.id = id;
    }

    public PersistentCooldown(int lenght, String id) {
        super(lenght);
        this.id = id;
    }


}
