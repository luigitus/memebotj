package me.krickl.memebotj.Inventory;

import me.krickl.memebotj.Database.MongoHandler;
import me.krickl.memebotj.Exceptions.DatabaseReadException;
import me.krickl.memebotj.Memebot;
import org.bson.Document;
import org.bson.codecs.IntegerCodec;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class will contain items of users that are shared accross channels
 * This file is part of memebotj.
 * Created by unlink on 22/04/16.
 */
public class Inventory {
    MongoHandler mongoHandler;
    String username = "";
    ArrayList<Item> items = new ArrayList<>();
    Document stats = new Document();

    private HashMap<String, Buff> buffList = new HashMap<>();

    public Inventory(String username, String channelOrigin) {
        this.username = username;
        update();

        if (Memebot.useMongo) {
            mongoHandler = new MongoHandler(Memebot.db, channelOrigin + "_user_inventory");
        }
    }

    public void addItem(String name, int amount) {
        boolean added = false;
        for(Item i : items) {
            if(i.getItemname().equals(name)) {
                i.setAmount(i.getAmount() + amount);
                added = true;
            }
        }

        if(!added) {
            items.add(new Item(name, amount));
        }
    }

    public void update() {
        // set default values if not presetn
        setDefault("str", 1);
        setDefault("str", 1);
        setDefault("int", 1);
        setDefault("armour", 1);
        setDefault("agility", 1);

        for(String key : buffList.keySet()) {
            if(buffList.get(key).canContinue()) {
                buffList.remove(key);
            }
        }
    }

    public void setDefault(String key, int value) {
        if(!stats.containsKey(key)) {
            stats.append(key, value);
        }
    }

    public void setStat(String key, int value) {
        if(!stats.containsKey(key)) {
            stats.append(key, value);
        } else {
            stats.replace(key, value);
        }
    }

    public int getStat(String key) {
        if(stats.containsKey(key)) {
            int stat = stats.getInteger(key);

            for(String buffKey : buffList.keySet()) {
                Buff buff = buffList.get(buffKey);
                stat = stat + buff.getBaseItem().getStatGainTemp().getOrDefault(key, 0);
            }

            return stat;
        }

        return 0;
    }

    public void readDB() {
        if (!Memebot.useMongo) {
            return;
        }

        try {
            mongoHandler.readDatabase(this.username);
        } catch(DatabaseReadException | IllegalArgumentException e) {
            return;
        }

        ArrayList<String> itemNames = (ArrayList<String>)mongoHandler.getDocument().getOrDefault("itemnames", new ArrayList<String>());
        ArrayList<Integer> itemCount = (ArrayList<Integer>)mongoHandler.getDocument().getOrDefault("itemcount", new ArrayList<Integer>());
        stats = (Document)mongoHandler.getDocument().getOrDefault("stats", stats);

        for(int i = 0; i < itemNames.size(); i++) {
            items.add(new Item(itemNames.get(i), itemCount.get(i)));
        }
    }

    public void writeDB() {
        if (!Memebot.useMongo) {
            return;
        }
        ArrayList<String> itemNames = new ArrayList<>();
        ArrayList<Integer> itemCount = new ArrayList<>();

        for(Item i : items) {
            itemNames.add(i.getItemname());
            itemCount.add(i.getAmount());
        }

        Document userData = new Document().append("_id", username)
                .append("itemnames", itemNames)
                .append("itemcount", itemCount)
                .append("stats", stats);

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
        return String.format("Health: %f", getStat("str") / 4);
    }

    public int hasItems(String name, int amount) {
        for(int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if(item.getItemname().equals(name) && item.getAmount() >= amount) {
                return i;
            }
        }

        return  -1;
    }

    public void addBuff(Item item) {
        buffList.put(item.getItemname(), new Buff(item.getBuffTime(), item));

        for(String key : item.getStatGain().keySet()) {
            setStat(key, item.getStatGain().get(key) + getStat(key));
        }
    }

    public boolean hasBuff(String key) {
        return buffList.containsKey(key);
    }

    public int isUsing(String name) {
        hasItems(name, 1);

        return  -1;
    }

    public Document getStats() {
        return stats;
    }

    public void setStats(Document stats) {
        this.stats = stats;
    }

}
