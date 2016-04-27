package me.krickl.memebotj.Inventory;

import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.Utility.Localisation;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
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
    private int value = 10;
    private int dropChance = 10;
    private HashMap<String, Integer> statGain = new HashMap<>();
    private HashMap<String, Integer> statGainTemp = new HashMap<>();
    private HashMap<String, Integer> statGainEquip = new HashMap<>();
    private int buffTime = 0;
    private boolean drops = true;
    private boolean canBuy = false;
    private String collection = "mm";
    private boolean canEquip = false;

    // todo implement stat gain/buff effects for items

    public Item(String name, int amount) {
        itemname = name;

        try {
            InputStream itemURL = getClass().getResourceAsStream("/items/" + name + ".cfg");
            if(itemURL != null) {
                InputStreamReader reader = new InputStreamReader(itemURL, "UTF-8");
                itemConfig.load(reader);
                reader.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }


        this.description = itemConfig.getOrDefault("description", "No description available").toString();
        this.maxAmount = Integer.parseInt(itemConfig.getOrDefault("maxamount", maxAmount).toString());
        this.value = Integer.parseInt(itemConfig.getOrDefault("value", value).toString());
        this.dropChance = Integer.parseInt(itemConfig.getOrDefault("dropchance", dropChance).toString());
        this.buffTime = Integer.parseInt(itemConfig.getOrDefault("bufftime", buffTime).toString());
        this.drops = Boolean.parseBoolean(itemConfig.getOrDefault("drops", drops).toString());
        this.collection = itemConfig.getOrDefault("collection", collection).toString();
        this.canBuy = Boolean.parseBoolean(itemConfig.getOrDefault("canbuy", canBuy).toString());
        this.canEquip = Boolean.parseBoolean(itemConfig.getOrDefault("canequip", canEquip).toString());

        String[] statTemp = itemConfig.getOrDefault("statGainPermanent", "").toString().split(";");
        try {
            for (String str : statTemp) {
                statGain.put(str.split(" ")[0], Integer.parseInt(str.split(" ")[1]));
            }
        } catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
        }

        statTemp = itemConfig.getOrDefault("statGainTemp", "").toString().split(";");
        try {
            for (String str : statTemp) {
                statGainTemp.put(str.split(" ")[0], Integer.parseInt(str.split(" ")[1]));
            }
        } catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
        }

        statTemp = itemConfig.getOrDefault("statGainEquip", "").toString().split(";");
        try {
            for (String str : statTemp) {
                statGainEquip.put(str.split(" ")[0], Integer.parseInt(str.split(" ")[1]));
            }
        } catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
        }

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

    public void useItem(int amount) {
        setAmount(this.amount - amount);
    }

    public String toString() {
        return itemname + ": " + Integer.toString(amount);
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

    public HashMap<String, Integer> getStatGain() {
        return statGain;
    }

    public void setStatGain(HashMap<String, Integer> statGain) {
        this.statGain = statGain;
    }

    public HashMap<String, Integer> getStatGainTemp() {
        return statGainTemp;
    }

    public void setStatGainTemp(HashMap<String, Integer> statGainTemp) {
        this.statGainTemp = statGainTemp;
    }

    public int getBuffTime() {
        return buffTime;
    }

    public void setBuffTime(int buffTime) {
        this.buffTime = buffTime;
    }

    public boolean isDrops() {
        return drops;
    }

    public void setDrops(boolean drops) {
        this.drops = drops;
    }

    public String getCollection() {
        return collection;
    }

    public boolean canBuy() {
        return canBuy;
    }

    public void setCanBuy(boolean canBuy) {
        this.canBuy = canBuy;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public boolean isCanBuy() {
        return canBuy;
    }

    public boolean isCanEquip() {
        return canEquip;
    }

    public void setCanEquip(boolean canEquip) {
        this.canEquip = canEquip;
    }

    public HashMap<String, Integer> getStatGainEquip() {
        return statGainEquip;
    }

    public void setStatGainEquip(HashMap<String, Integer> statGainEquip) {
        this.statGainEquip = statGainEquip;
    }
}
