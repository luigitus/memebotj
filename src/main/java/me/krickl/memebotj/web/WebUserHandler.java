package me.krickl.memebotj.web;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import me.krickl.memebotj.Memebot;
import org.bson.Document;

import java.security.SecureRandom;
import java.util.logging.Logger;

/**
 * This file is part of memebotj.
 * Created by unlink on 06/04/16.
 */
public class WebUserHandler implements Comparable<WebUserHandler>  {
    public static Logger log = Logger.getLogger(WebUserHandler.class.getName());

    String username = "";
    String channelOrigin = "";

    boolean isModerator = false;
    boolean isUserBroadcaster = false;
    boolean newUser = false;
    String nickname = "";
    int commandPower = 0;
    int autoCommandPower = 0;
    int customCommandPower = 0;
    private double points = 0.0f;

    String autogreet = "";
    int timeouts = 0;
    MongoCollection<Document> userCollection = null;
    String modNote = "";
    SecureRandom random = new SecureRandom();

    String dateJoined = "null";
    long timeStampJoined = System.currentTimeMillis();

    boolean enableAutogreets = true;

    double walletSize = -1;

    boolean isFollowing = false;
    boolean hasFollowed = false;

    boolean hasAutogreeted = false;
    boolean shouldBeRemoved = false;

    public WebUserHandler(String username, String channelOrigin) {
        this.username = username;
        this.channelOrigin = channelOrigin;
        if(!Memebot.channelsPrivate.contains(this.channelOrigin)) {
            this.userCollection = Memebot.db.getCollection(this.channelOrigin + "_users");
        } else {
            this.userCollection = Memebot.dbPrivate.getCollection(this.channelOrigin + "_users");
        }

        readDB();
    }

    private void readDB() {
        Document channelQuery = new Document("_id", this.username);
        FindIterable<Document> cursor = this.userCollection.find(channelQuery);

        Document userData = cursor.first();

        // read data
        if (userData != null) {
            this.isModerator = userData.getBoolean("mod", this.isModerator);
            this.points = (double)userData.getOrDefault("pointsf", this.points);
            this.autogreet = userData.getOrDefault("autogreet", this.autogreet).toString();
            this.customCommandPower = (int)userData.getOrDefault("ccommandpower", this.customCommandPower);
            this.isUserBroadcaster = (boolean) userData.getOrDefault("broadcaster", this.isUserBroadcaster);
            this.timeouts = (int)userData.getOrDefault("timeouts", this.timeouts);
            this.enableAutogreets = (boolean)userData.getOrDefault("enableautogreet", this.enableAutogreets);
            this.dateJoined = userData.getOrDefault("datejoined", this.dateJoined).toString();
            this.timeStampJoined = (long)userData.getOrDefault("timeStampJoined", this.timeStampJoined);
            this.nickname = userData.getOrDefault("nickname", this.nickname).toString();
            this.walletSize = (double)userData.getOrDefault("wallet", this.walletSize);
            this.isFollowing = (boolean)userData.getOrDefault("isfollowing", this.isFollowing);
            this.hasFollowed = (boolean)userData.getOrDefault("hasfollowed", this.hasFollowed);

        } else {
            this.newUser = true;
        }
    }

    @Override
    public int compareTo(WebUserHandler another) {
        return username.compareTo(another.getUsername());
    }

    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        WebUserHandler.log = log;
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
        this.commandPower = commandPower;
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

    public void setPoints(double points) {
        this.points = points;
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

    public boolean isShouldBeRemoved() {
        return shouldBeRemoved;
    }

    public void setShouldBeRemoved(boolean shouldBeRemoved) {
        this.shouldBeRemoved = shouldBeRemoved;
    }
}
