package me.krickl.memebotj.Inventory;

import me.krickl.memebotj.Utility.PersistentCooldown;

/**
 * This file is part of memebotj.
 * Created by unlink on 23/04/16.
 */
public class Buff extends PersistentCooldown {
    Item baseItem = null;

    public Buff(int lenght, String id, Item item) {
        super(lenght, id);
        baseItem = item;
    }

    public Item getBaseItem() {
        return baseItem;
    }

    public void setBaseItem(Item baseItem) {
        this.baseItem = baseItem;
    }
}
