package me.krickl.memebotj.User;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Database.MongoHandler;
import me.krickl.memebotj.Exceptions.DatabaseReadException;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.Utility.CommandPower;
import me.krickl.memebotj.Utility.Cooldown;
import org.apache.commons.codec.binary.Base64;
import org.bson.Document;
import org.json.simple.JSONObject;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Logger;

//import me.krickl.memebotj.Inventory.Inventory;

/**
 * This file is part of memebotj.
 * Created by unlink on 06/04/16.
 */
@Deprecated
public class UserHandler implements Comparable<UserHandler> {
    public static Logger log = Logger.getLogger(UserHandler.class.getName());
    private boolean isModerator = false;
    private boolean isUserBroadcaster = false;
    private boolean newUser = false;
    private int commandPower = CommandPower.viewerAbsolute;
    private int autoCommandPower = CommandPower.viewerAbsolute;
    private int customCommandPower = 0;
    private int timeouts = 0;
    private boolean shouldBeRemoved = false;
    private MongoHandler mongoHandler = null;
    private String username = "";
    private String channelOrigin = "";
    private String nickname = "";
    private double points = 0.0f;
    // private boolean isJoined = false
    private Cooldown userCooldown = new Cooldown(0);
    private String autogreet = "";
    // todo old db code - remove soon
    //MongoCollection<Document> userCollection = null;
    private java.util.HashMap<String, Cooldown> userCommandCooldowns = new java.util.HashMap<String, Cooldown>();
    private String modNote = "";
    private SecureRandom random = new SecureRandom();
    private String dateJoined = "null";
    private long timeStampJoined = System.currentTimeMillis();
    private boolean enableAutogreets = true;
    private double walletSize = -1;
    private boolean isFollowing = false;
    private boolean hasFollowed = false;
    private boolean hasAutogreeted = false;
    private Cooldown removeCooldown = new Cooldown(0);
    private int jackpotWins = 0;
    private String id = ""; // todo implement id

    private int constantCommandPower = -1;

    private boolean pointsUpdateDone = false;

    //private Inventory userInventory;

    private int lastTimeoutDuration = 0;
    private String lastTimeoutReason = "";

    private boolean isActive = true;
    private Cooldown idleCooldown = new Cooldown(1200);

    // todo remove userhandler identification by id (omg why)
    public UserHandler(String username, String channelOrigin) {
        this.channelOrigin = channelOrigin;
        this.id = "";
        this.username = username;
        if(username.equals(channelOrigin.replace("#", ""))) {
            setModerator(true);
            setUserBroadcaster(true);
            setCommandPower(CommandPower.broadcasterAbsolute);
        }

        //userInventory = new Inventory(username, channelOrigin, this);

        if (Memebot.useMongo) {
            if (!Memebot.channelsPrivate.contains(this.channelOrigin)) {
                // todo old db code - remove soon
                //this.userCollection = Memebot.db.getCollection(this.channelOrigin + "_users");
                mongoHandler = new MongoHandler(Memebot.db, this.channelOrigin + "_users");
            } else {
                // todo old db code - remove soon
                //this.userCollection = Memebot.dbPrivate.getCollection(this.channelOrigin + "_users");
                mongoHandler = new MongoHandler(Memebot.dbPrivate, this.channelOrigin + "_users");
            }
        }

        readDB();
        if (dateJoined.equals("null")) {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a"); // dd/MM/yyyy
            Calendar cal = Calendar.getInstance();
            this.dateJoined = sdfDate.format(cal.getTime());
        }

        //points update - reduce every points related segment
        if (!this.pointsUpdateDone) {
            this.points = points / 10;
            this.pointsUpdateDone = true;
        }

        setCommandPower(this.autoCommandPower);
    }

    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        UserHandler.log = log;
    }

    private void readDB() {
        if (!Memebot.useMongo) {
            return;
        }

        try {
            mongoHandler.readDatabase(this.username);
        } catch (DatabaseReadException | IllegalArgumentException e1) {
            log.warning(e1.toString());
            this.newUser = true;
        }

        /*try {
            mongoHandler.readDatabase(this.id, "_id_new");
        } catch (DatabaseReadException | IllegalArgumentException e) {
            try {
                mongoHandler.readDatabase(this.username);
            } catch (DatabaseReadException | IllegalArgumentException e1) {
                log.warning(e1.toString());
                this.newUser = true;
            }
            return;
        }*/

        this.username = (String) mongoHandler.getObject("_id", this.username);
        this.id = (String) mongoHandler.getObject("_id_new", this.id);
        this.isModerator = (boolean) mongoHandler.getObject("mod", this.isModerator);
        this.points = (double) mongoHandler.getObject("pointsf", this.points);
        this.autogreet = mongoHandler.getObject("autogreet", this.autogreet).toString();
        this.customCommandPower = (int) mongoHandler.getObject("ccommandpower", this.customCommandPower);
        this.isUserBroadcaster = (boolean) mongoHandler.getObject("broadcaster", this.isUserBroadcaster);
        this.timeouts = (int) mongoHandler.getObject("timeouts", this.timeouts);
        this.enableAutogreets = (boolean) mongoHandler.getObject("enableautogreet", this.enableAutogreets);
        this.dateJoined = mongoHandler.getObject("datejoined", this.dateJoined).toString();
        this.timeStampJoined = (long) mongoHandler.getObject("timeStampJoined", this.timeStampJoined);
        this.nickname = mongoHandler.getObject("nickname", this.nickname).toString();
        this.walletSize = (double) mongoHandler.getObject("wallet", this.walletSize);
        this.isFollowing = (boolean) mongoHandler.getObject("isfollowing", this.isFollowing);
        this.hasFollowed = (boolean) mongoHandler.getObject("hasfollowed", this.hasFollowed);
        this.jackpotWins = (int) mongoHandler.getObject("jackpotwins", this.jackpotWins);
        this.lastTimeoutDuration = (int) mongoHandler.getObject("lasttoduration", this.lastTimeoutDuration);
        this.lastTimeoutReason = mongoHandler.getObject("lasttoreason", this.lastTimeoutReason).toString();
        this.constantCommandPower = (int) mongoHandler.getObject("contstantcommandpower", this.constantCommandPower);

        this.pointsUpdateDone = (boolean) mongoHandler.getObject("pointsupdate", this.pointsUpdateDone);

        //userInventory.readDB();
    }

    public void setDB() {
        //Document userData = mongoHandler.getDocument();
        mongoHandler.updateDocument("_id", this.username);
        mongoHandler.updateDocument("pointsf", this.points);
        mongoHandler.updateDocument("mod", this.isModerator);
        mongoHandler.updateDocument("autogreet", this.autogreet);
        mongoHandler.updateDocument("ccommandpower", this.customCommandPower);
        mongoHandler.updateDocument("broadcaster", this.isUserBroadcaster);
        mongoHandler.updateDocument("timeouts", this.timeouts);
        mongoHandler.updateDocument("enableautogreet", this.enableAutogreets);
        mongoHandler.updateDocument("datejoined", this.dateJoined);
        mongoHandler.updateDocument("timeStampJoined", this.timeStampJoined);
        mongoHandler.updateDocument("nickname", this.nickname);
        mongoHandler.updateDocument("wallet", this.walletSize);
        mongoHandler.updateDocument("isfollowing", this.isFollowing);
        mongoHandler.updateDocument("hasfollowed", this.hasFollowed);
        mongoHandler.updateDocument("jackpotwins", this.jackpotWins);
        mongoHandler.updateDocument("lasttoduration", this.lastTimeoutDuration);
        mongoHandler.updateDocument("lasttoreason", this.lastTimeoutReason);
        mongoHandler.updateDocument("pointsupdate", this.pointsUpdateDone);
        mongoHandler.updateDocument("contstantcommandpower", constantCommandPower);
        //mongoHandler.setDocument(userData);
    }

    public void writeDB() {
        if (!Memebot.useMongo) {
            return;
        }
        setDB();

        mongoHandler.writeDatabase(this.username);

        //userInventory.writeDB();
    }

    public void update(ChannelHandler channelHandler) {
        //userInventory.update();

        // remove unused cooldowns asap
        ArrayList<String> toRemove = new ArrayList<>();

        for (String key : userCommandCooldowns.keySet()) {
            Cooldown cooldown = userCommandCooldowns.get(key);
            if (cooldown.canContinue()) {
                toRemove.add(key);
            }
        }

        for (String key : toRemove) {
            userCommandCooldowns.remove(key);
        }

        //todo implement idle cooldown
    }

    public boolean canRemove() {
        // remove user after 1 hour (0x36EE80 milliseconds) of inactivity
        HashMap<String, UserHandler> tmpUserList = new HashMap<String, UserHandler>();
        tmpUserList.put(this.username, this);

        //check if user has been marked for removal
        if (this.shouldBeRemoved && this.removeCooldown.canContinue()) {
            UserHandler.log.info("Removed user " + this.username);
            return true;
        }

        return false;
    }

    public void sendAutogreet(ChannelHandler channelHandler) {
        // todo && CommandHandler.checkPermissionsForUser(this,channelHandler.getNeededAutogreetCommandPower(), channelHandler.getNeededAutogreetCommandPower(), channelHandler)
        if (!this.hasAutogreeted && this.enableAutogreets && channelHandler.isAllowAutogreet() && !this.autogreet.equals("")) {

            channelHandler.sendMessage(autogreet, this.channelOrigin, this, false);
            this.hasAutogreeted = true;
        }
    }

    public String resetOAuth() {
        String keySource = username + Long.toString(timeStampJoined) + Integer.toHexString(new SecureRandom().nextInt());
        String oauth = Base64.encodeBase64String(keySource.getBytes());
        return oauth;
    }

    @Override
    public int compareTo(UserHandler another) {
        return username.compareTo(another.getUsername());
    }

    public MongoHandler getMongoHandler() {
        return mongoHandler;
    }

    public void setMongoHandler(MongoHandler mongoHandler) {
        this.mongoHandler = mongoHandler;
    }

    public int getJackpotWins() {
        return jackpotWins;
    }

    public void setJackpotWins(int jackpotWins) {
        this.jackpotWins = jackpotWins;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getChannelOrigin() {
        return channelOrigin;
    }

    public void setChannelOrigin(String channelOrigin) {
        this.channelOrigin = channelOrigin;
    }

    public boolean isModerator() {
        return isModerator;
    }

    public void setModerator(boolean moderator) {
        isModerator = moderator;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isUserBroadcaster() {
        return isUserBroadcaster;
    }

    public void setUserBroadcaster(boolean userBroadcaster) {
        isUserBroadcaster = userBroadcaster;
    }

    public boolean isNewUser() {
        return newUser;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getCommandPower() {
        if (this.constantCommandPower >= 0 && this.constantCommandPower >= commandPower) {
            // constant command power can be used to assign a constant value to a user
            // this will be used for permissions on the website
            return constantCommandPower;
        }
        return commandPower;
    }

    public void setCommandPower(int commandPower) {
        this.autoCommandPower = commandPower;
        this.commandPower = commandPower + this.customCommandPower;
    }

    public int getAutoCommandPower() {
        return autoCommandPower;
    }

    public void setAutoCommandPower(int autoCommandPower) {
        this.autoCommandPower = autoCommandPower;
    }

    public int getCustomCommandPower() {
        return customCommandPower;
    }

    public void setCustomCommandPower(int customCommandPower) {
        this.customCommandPower = customCommandPower;
    }

    public double getPoints() {
        return points;
    }

    public boolean setPoints(double f) {
        boolean result = true;

        this.points = f;
        if (this.points < 0) {
            this.points = 0;
        }

        for (ChannelHandler ch : Memebot.joinedChannels) {
            if (ch.getChannel().equals(this.channelOrigin)) {
                if (this.points + f > ch.getMaxPoints() || (this.points + f > this.walletSize && this.walletSize > 0)) {
                    if (Memebot.debug) {
                        this.points = ch.getMaxPoints();
                        result = false;
                    } else {
                        result = true;
                    }
                }
            }
        }

        this.writeDB();

        return result;
    }

    public Cooldown getUserCooldown() {
        return userCooldown;
    }

    public void setUserCooldown(Cooldown userCooldown) {
        this.userCooldown = userCooldown;
    }

    public String getAutogreet() {
        return autogreet;
    }

    public void setAutogreet(String autogreet) {
        this.autogreet = autogreet;
    }

    public int getTimeouts() {
        return timeouts;
    }

    public void setTimeouts(int timeouts) {
        this.timeouts = timeouts;
    }

    public HashMap<String, Cooldown> getUserCommandCooldowns() {
        return userCommandCooldowns;
    }

    public void setUserCommandCooldowns(HashMap<String, Cooldown> userCommandCooldowns) {
        this.userCommandCooldowns = userCommandCooldowns;
    }

    public String getModNote() {
        return modNote;
    }

    public void setModNote(String modNote) {
        this.modNote = modNote;
    }

    public SecureRandom getRandom() {
        return random;
    }

    public void setRandom(SecureRandom random) {
        this.random = random;
    }

    public String getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(String dateJoined) {
        this.dateJoined = dateJoined;
    }

    public long getTimeStampJoined() {
        return timeStampJoined;
    }

    public void setTimeStampJoined(long timeStampJoined) {
        this.timeStampJoined = timeStampJoined;
    }

    public boolean isEnableAutogreets() {
        return enableAutogreets;
    }

    public void setEnableAutogreets(boolean enableAutogreets) {
        this.enableAutogreets = enableAutogreets;
    }

    public double getWalletSize() {
        return walletSize;
    }

    public void setWalletSize(double walletSize) {
        this.walletSize = walletSize;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public boolean isHasFollowed() {
        return hasFollowed;
    }

    public void setHasFollowed(boolean hasFollowed) {
        this.hasFollowed = hasFollowed;
    }

    public boolean isHasAutogreeted() {
        return hasAutogreeted;
    }

    public void setHasAutogreeted(boolean hasAutogreeted) {
        this.hasAutogreeted = hasAutogreeted;
    }

    public Cooldown getRemoveCooldown() {
        return removeCooldown;
    }

    public void setRemoveCooldown(Cooldown removeCooldown) {
        this.removeCooldown = removeCooldown;
    }

    public boolean isShouldBeRemoved() {
        return shouldBeRemoved;
    }

    public void setShouldBeRemoved(boolean shouldBeRemoved) {
        this.shouldBeRemoved = shouldBeRemoved;
    }

    /*public Inventory getUserInventory() {
        return userInventory;
    }

    public void setUserInventory(Inventory userInventory) {
        this.userInventory = userInventory;
    }*/

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getConstantCommandPower() {
        return constantCommandPower;
    }

    public void setConstantCommandPower(int constantCommandPower) {
        this.constantCommandPower = constantCommandPower;
    }

    public boolean isPointsUpdateDone() {
        return pointsUpdateDone;
    }

    public void setPointsUpdateDone(boolean pointsUpdateDone) {
        this.pointsUpdateDone = pointsUpdateDone;
    }

    public int getLastTimeoutDuration() {
        return lastTimeoutDuration;
    }

    public void setLastTimeoutDuration(int lastTimeoutDuration) {
        this.lastTimeoutDuration = lastTimeoutDuration;
    }

    public String getLastTimeoutReason() {
        return lastTimeoutReason;
    }

    public void setLastTimeoutReason(String lastTimeoutReason) {
        this.lastTimeoutReason = lastTimeoutReason;
    }

    public String screenName() {
        if (!this.nickname.isEmpty()) {
            return nickname;
        }
        return this.username;
    }

    public String getOauth() {
        String oauth = resetOAuth();
        MongoHandler mongoHandler = new MongoHandler(Memebot.db, "#oauth#");
        try {
            mongoHandler.readDatabase(username);
        } catch (DatabaseReadException e) {
            log.info(e.toString());
            setOauth(oauth);
            return oauth;
        }

        String dbOauth = mongoHandler.getDocument().getOrDefault("oauth", oauth).toString();

        if (oauth.equals(dbOauth)) {
            setOauth(dbOauth);
        }

        return dbOauth;
    }

    public void setOauth(String oauth) {
        MongoHandler mongoHandler = new MongoHandler(Memebot.db, "#oauth#");

        Document document = new Document();
        document.append("oauth", oauth).append("_id", username);
        mongoHandler.setDocument(document);
        mongoHandler.writeDatabase(username);
    }

    public String getAPIKey() {
        String apikey = resetOAuth();
        MongoHandler mongoHandler = new MongoHandler(Memebot.db, "#apikey#");
        try {
            mongoHandler.readDatabase(username);
        } catch (DatabaseReadException e) {
            log.info(e.toString());
            setAPIKey(apikey);
            return apikey;
        }

        String dbkey = mongoHandler.getDocument().getOrDefault("apikey", apikey).toString();

        if (apikey.equals(dbkey)) {
            setOauth(dbkey);
        }

        return dbkey;
    }

    public boolean isUserACat() {
        return username.contains("cat") || username.contains("kitty");

    }

    public void setAPIKey(String key) {
        MongoHandler mongoHandler = new MongoHandler(Memebot.db, "#apikey#");

        Document document = new Document();
        document.append("apikey", key).append("_id", username);
        mongoHandler.setDocument(document);
        mongoHandler.writeDatabase(username);
    }

    public JSONObject toJSONObject() {
        JSONObject wrapper = new JSONObject();
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("points", points);
        jsonObject.put("timeouts", timeouts);
        jsonObject.put("_id", id);
        jsonObject.put("username", username);
        jsonObject.put("_channel", channelOrigin);
        jsonObject.put("joinded_t", timeStampJoined);
        jsonObject.put("joined_str", dateJoined);
        jsonObject.put("new_user", newUser);
        jsonObject.put("screenname", screenName());
        jsonObject.put("weird_boolean", "rip");
        jsonObject.put("is_user_a_cat", isUserACat());
        jsonObject.put("jackpot_wins", jackpotWins);
        jsonObject.put("constant_commandpower", constantCommandPower);
        jsonObject.put("mod_note", modNote);
        jsonObject.put("commandpower", commandPower);

        wrapper.put("data", jsonObject);
        wrapper.put("links", Memebot.getLinks(Memebot.webBaseURL + "/api/users/" + channelOrigin.replace("#", "") + "/" + username,
                Memebot.webBaseURL + "/api/channels/" + getChannelOrigin().replace("#", ""), null, null));

        return wrapper;
    }

    public String toJSONString() {
        return toJSONObject().toJSONString();
    }
}
