package me.krickl.memebotj.Inventory;

import me.krickl.memebotj.Database.MongoHandler;
import me.krickl.memebotj.Exceptions.DatabaseReadException;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import org.bson.Document;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class will contain items of users that are shared accross channels
 * This file is part of memebotj.
 * Created by unlink on 22/04/16.
 */
@Deprecated
public class Inventory {
    int maxEquips = 8;
    int currentHealth = 1;
    private MongoHandler mongoHandler;
    private String username = "";
    private ArrayList<Item> items = new ArrayList<>();
    private Document stats = new Document();
    private HashMap<String, Buff> buffList = new HashMap<>();
    private ArrayList<String> equips = new ArrayList<>();
    private UserHandler sender = null;

    public Inventory(String username, String channelOrigin, UserHandler sender) {
        this.username = username;
        this.sender = sender;
        update();

        if (Memebot.useMongo) {
            mongoHandler = new MongoHandler(Memebot.db, channelOrigin + "_user_inventory");
        }
    }

    public void addItem(String name, int amount) {
        boolean added = false;
        for (Item i : items) {
            if (i.getItemname().equals(name)) {
                i.setAmount(i.getAmount() + amount);
                added = true;
            }
        }

        if (!added) {
            items.add(new Item(name, amount));
        }
    }

    public void update() {
        // set default values if not presetn
        setDefault("str", 1);
        setDefault("int", 1);
        setDefault("luck", 1);
        setDefault("armour", 1);
        setDefault("agility", 1);

        for (String key : buffList.keySet()) {
            if (buffList.get(key).canContinue()) {
                buffList.remove(key);
            }
        }
    }

    public void setDefault(String key, int value) {
        if (!stats.containsKey(key)) {
            stats.append(key, value);
        }
    }

    public void setStat(String key, int value) {
        if (!stats.containsKey(key)) {
            stats.append(key, value);
        } else {
            stats.replace(key, value);
        }
    }

    public int getStat(String key) {
        if (stats.containsKey(key)) {
            int stat = stats.getInteger(key);

            for (String buffKey : buffList.keySet()) {
                Buff buff = buffList.get(buffKey);
                stat = stat + buff.getBaseItem().getTempStat(key);
            }

            for (String equipKey : equips) {
                Item equip = new Item(equipKey, 1);
                stat = stat + equip.getEquipStat(key);
            }

            return stat;
        }

        return 0;
    }

    public boolean equip(String key) {
        Item item = new Item(key, 1);
        // todo implement this - equip list and max equip items
        // check if item can still be equipped
        // check cause item can have a max amount if same equips
        int i = hasItems(key, 1);
        if ((i >= 0) && !equips.contains(key) && item.isCanEquip() && this.equips.size() <= this.maxEquips) {
            equips.add(key);
            items.get(i).setAmount(items.get(i).getAmount() - 1);
            return true;
        }

        return false;
    }

    public boolean unequip(String key) {
        if (equips.contains(key)) {
            addItem(key, 1);
            equips.remove(key);
        }
        return false;
    }

    public void readDB() {
        if (!Memebot.useMongo) {
            return;
        }

        try {
            mongoHandler.readDatabase(this.username);
        } catch (DatabaseReadException | IllegalArgumentException e) {
            return;
        }

        ArrayList<String> itemNames = (ArrayList<String>) mongoHandler.getDocument().getOrDefault("itemnames", new ArrayList<String>());
        ArrayList<Integer> itemCount = (ArrayList<Integer>) mongoHandler.getDocument().getOrDefault("itemcount", new ArrayList<Integer>());
        stats = (Document) mongoHandler.getDocument().getOrDefault("stats", stats);
        equips = (ArrayList<String>) mongoHandler.getDocument().getOrDefault("equips", new ArrayList<String>());
        currentHealth = (int) mongoHandler.getDocument().getOrDefault("currhp", currentHealth);

        for (int i = 0; i < itemNames.size(); i++) {
            items.add(new Item(itemNames.get(i), itemCount.get(i)));
        }
    }

    public void writeDB() {
        if (!Memebot.useMongo) {
            return;
        }
        ArrayList<String> itemNames = new ArrayList<>();
        ArrayList<Integer> itemCount = new ArrayList<>();

        for (Item i : items) {
            itemNames.add(i.getItemname());
            itemCount.add(i.getAmount());
        }

        Document userData = new Document().append("_id", username)
                .append("itemnames", itemNames)
                .append("itemcount", itemCount)
                .append("stats", stats)
                .append("equips", equips).append("currhp", currentHealth);

        mongoHandler.setDocument(userData);
        mongoHandler.writeDatabase(this.username);
    }

    public MongoHandler getMongoHandler() {
        return mongoHandler;
    }

    public void setMongoHandler(MongoHandler mongoHandler) {
        this.mongoHandler = mongoHandler;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public String toString() {
        return String.format("Health: %d/%d - Strength: %d - Agility: %d - Intelligence: %d - Luck: %d", currentHealth, getStat("str") * 4,
                getStat("str"), getStat("agility"), getStat("int"), getStat("luck"));
    }

    public int hasItems(String name, int amount) {
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item.getItemname().equals(name) && item.getAmount() >= amount) {
                return i;
            }
        }

        return -1;
    }

    public void addBuff(Item item) {
        buffList.put(item.getItemname(), new Buff(item.getBuffTime(), item));

        for (String key : item.getStatGain().keySet()) {
            setStat(key, item.getStatGain().get(key) + getStat(key));
        }
    }

    public boolean hasBuff(String key) {
        return buffList.containsKey(key);
    }

    public int isUsing(String name) {
        hasItems(name, 1);

        return -1;
    }

    public Document getStats() {
        return stats;
    }

    public void setStats(Document stats) {
        this.stats = stats;
    }

    public HashMap<String, Buff> getBuffList() {
        return buffList;
    }

    public void setBuffList(HashMap<String, Buff> buffList) {
        this.buffList = buffList;
    }

    public ArrayList<String> getEquips() {
        return equips;
    }

    public void setEquips(ArrayList<String> equips) {
        this.equips = equips;
    }

    public int getMaxEquips() {
        return maxEquips;
    }

    public void setMaxEquips(int maxEquips) {
        this.maxEquips = maxEquips;
    }

    public UserHandler getSender() {
        return sender;
    }

    public void setSender(UserHandler sender) {
        this.sender = sender;
    }

    public void setHP(int offset) {
        if ((currentHealth + offset) <= getStat("str") * 4) {
            currentHealth = offset + currentHealth;
        } else {
            currentHealth = getStat("str") * 4;
        }

        if (currentHealth < 0) {
            currentHealth = 0;
        }
    }


    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("_id", username);

        return jsonObject;
    }


    public String toJSON() {
        return toJSONObject().toJSONString();
    }
}
