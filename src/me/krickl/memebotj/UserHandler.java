package me.krickl.memebotj;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Utility.CommandPower;
import me.krickl.memebotj.Utility.Cooldown;
import org.bson.Document;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * This file is part of memebotj.
 * Created by unlink on 06/04/16.
 */
public class UserHandler {
    public static Logger log = Logger.getLogger(UserHandler.class.getName());

    String username = "";
    String channelOrigin = "";

    boolean isModerator = false;
    boolean isUserBroadcaster = false;
    boolean newUser = false;
    String nickname = "";
    int commandPower = CommandPower.viewerAbsolute;
    int autoCommandPower = CommandPower.viewerAbsolute;
    int customCommandPower = 0;
    private double points = 0.0f;

    // private boolean isJoined = false
    Cooldown userCooldown = new Cooldown(0);
    String autogreet = "";
    int timeouts = 0;
    MongoCollection<Document> userCollection = null;
    java.util.HashMap<String, Cooldown> userCommandCooldowns = new java.util.HashMap<String, Cooldown>();
    String modNote = "";
    SecureRandom random = new SecureRandom();

    String dateJoined = "null";
    long timeStampJoined = System.currentTimeMillis();

    boolean enableAutogreets = true;

    double walletSize = -1;

    boolean isFollowing = false;
    boolean hasFollowed = false;

    boolean hasAutogreeted = false;

    Cooldown removeCooldown = new Cooldown(0);
    boolean shouldBeRemoved = false;

    public UserHandler(String username, String channelOrigin) {
        this.username = username;
        this.channelOrigin = channelOrigin;

        if (Memebot.useMongo) {
            this.userCollection = Memebot.db.getCollection(this.channelOrigin + "_users");
        }

        readDB();
        if (dateJoined == "null") {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a"); // dd/MM/yyyy
            Calendar cal = Calendar.getInstance();
            this.dateJoined = sdfDate.format(cal.getTime());
        }

        setCommandPower(this.autoCommandPower);
    }

    private void readDB() {
        if (!Memebot.useMongo) {
            return;
        }

        Document channelQuery = new Document("_id", this.username);
        FindIterable<Document> cursor = this.userCollection.find(channelQuery);

        Document userData = cursor.first();

        // read data
        if (userData != null) {
            this.isModerator = userData.getBoolean("mod", this.isModerator);
            this.points = (double)userData.getOrDefault("pointsf", (int)this.points);
            this.autogreet = userData.getOrDefault("autogreet", this.autogreet).toString();
            this.customCommandPower = (int)userData.getOrDefault("ccommandpower", this.customCommandPower);
            this.isUserBroadcaster = (boolean) userData.getOrDefault("broadcaster", this.isUserBroadcaster);
            this.timeouts = (int)userData.getOrDefault("timeouts", this.timeouts);
            this.enableAutogreets = (boolean)userData.getOrDefault("enableautogreet", this.enableAutogreets);
            this.dateJoined = userData.getOrDefault("datejoined", this.dateJoined).toString();
            this.timeStampJoined = (long)userData.getOrDefault("timeStampJoined", this.timeStampJoined);
            this.nickname = userData.getOrDefault("nickname", this.nickname).toString();
            this.walletSize = (int)userData.getOrDefault("wallet", this.walletSize);
            this.isFollowing = (boolean)userData.getOrDefault("isfollowing", this.isFollowing);
            this.hasFollowed = (boolean)userData.getOrDefault("hasfollowed", this.hasFollowed);

        } else {
            this.newUser = true;
        }
    }

    public void writeDB() {
        if (!Memebot.useMongo) {
            return;
        }

        Document userQuery = new Document("_id", this.username);

        Document userData = new Document("_id", this.username).append("pointsf", this.points)
                .append("mod", this.isModerator).append("autogreet", this.autogreet)
                .append("ccommandpower", this.customCommandPower).append("broadcaster", this.isUserBroadcaster)
                .append("timeouts", this.timeouts)
                .append("enableautogreet", this.enableAutogreets)
                .append("datejoined", this.dateJoined)
                .append("timeStampJoined", this.timeStampJoined)
                .append("nickname", this.nickname)
                .append("wallet", this.walletSize)
                .append("isfollowing", this.isFollowing)
                .append("hasfollowed", this.hasFollowed);
        try {
            if (this.userCollection.findOneAndReplace(userQuery, userData) == null) {
                this.userCollection.insertOne(userData);
            }
        } catch(Exception e) {
            log.warning(e.toString());
        }
    }

    public void update(ChannelHandler channelHandler) {

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
        if (!this.hasAutogreeted && this.enableAutogreets && channelHandler.isAllowAutogreet() && !this.autogreet.equals("")) {
            channelHandler.sendMessage(autogreet, this.channelOrigin, this);
            this.hasAutogreeted = true;
        }
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
                    this.points = ch.getMaxPoints();
                    result = false;
                }
            }
        }

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

    public MongoCollection<Document> getUserCollection() {
        return userCollection;
    }

    public void setUserCollection(MongoCollection<Document> userCollection) {
        this.userCollection = userCollection;
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


    public String screenName() {
        if (!this.nickname.isEmpty() && Memebot.debug) {
            return nickname;
        }
        return this.username;
    }
}
