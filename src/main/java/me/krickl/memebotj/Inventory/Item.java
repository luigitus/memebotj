package me.krickl.memebotj.Inventory;

import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.Utility.Localisation;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * This file is part of memebotj.
 * Created by unlink on 22/04/16.
 */
public class Item {
    private String itemname = "";
    private String description = "";
    private int amount = 0;
    private int maxAmount = 10;
    private Properties itemConfig = new Properties();
    private int using = 0;
    private int value = 10;
    private int dropChance = 10;

    public Item(String name, int amount) {
        itemname = name;

        try {
            itemConfig.load(new FileReader(Memebot.memebotDir + "/items/" + itemname + ".cfg"));
        } catch(IOException e) {
            e.printStackTrace();
        }


        this.description = itemConfig.getOrDefault("description", "No description available").toString();
        this.maxAmount = Integer.parseInt(itemConfig.getOrDefault("maxamount", maxAmount).toString());
        this.value = Integer.parseInt(itemConfig.getOrDefault("value", value).toString());
        this.dropChance = Integer.parseInt(itemConfig.getOrDefault("dropchance", dropChance).toString());
        this.setAmount(amount);
    }

    public String getItemname() {
        return itemname;
    }

    public void setItemname(String itemname) {
        this.itemname = itemname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
        if(this.amount > maxAmount) {
            this.amount = maxAmount;
        }
        if(this.amount < 0) {
            amount = 0;
        }
    }

    public String toString() {
        return itemname + ": " + Integer.toString(amount) + "/" + Integer.toString(using);
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Properties getItemConfig() {
        return itemConfig;
    }

    public void setItemConfig(Properties itemConfig) {
        this.itemConfig = itemConfig;
    }

    public int getUsing() {
        return using;
    }

    public void setUsing(int using) {
        /*if(using == 0) {
            this.setAmount(this.getAmount() - this.using);
        }*/
        this.using = using;
        if(using < 0) {
            using = 0;
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getDropChance() {
        return dropChance;
    }

    public void setDropChance(int dropChance) {
        this.dropChance = dropChance;
    }
}
