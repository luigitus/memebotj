package me.krickl.memebotj.Inventory;

import me.krickl.memebotj.Utility.Cooldown;

/**
 * This file is part of memebotj.
 * Created by unlink on 23/04/16.
 */
public class Buff extends Cooldown {
    Item baseItem = null;

    public Buff(int lenght, Item item) {
        super(lenght);
        baseItem = item;
    }

    public Item getBaseItem() {
        return baseItem;
    }

    public void setBaseItem(Item baseItem) {
        this.baseItem = baseItem;
    }
}
