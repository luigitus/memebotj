package me.krickl.memebotj.web;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import me.krickl.memebotj.Memebot;
import org.bson.Document;

import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Logger;

/**
 * This file is part of memebotj.
 * Created by unlink on 07/04/16.
 */
public class WebChannelHandler {
    public static Logger log = Logger.getLogger(WebChannelHandler.class.getName());

    private String channel = "";
    private String broadcaster = this.channel.replace("#", "");
    private String followerNotification = "";
    private String raceBaseURL = "http://kadgar.net/live";
    private String greetMessage = "Hello I'm {botnick} {version} the dankest irc bot ever RitzMitz";
    private String currentRaceURL = "";
    private ArrayList<String> fileNameList = new ArrayList<>();

    private short maxFileNameLen = 8;
    private String currentFileName = "";
    private long streamStartTime = 0;

    private String local = "engb";
    private ArrayList<String> otherLoadedChannels = new java.util.ArrayList<String>();
    private MongoCollection<Document> channelCollection = null;
    private double pointsPerUpdate = 1.0f;
    private Thread t = null;
    private boolean isJoined = true;
    private boolean allowAutogreet = true;
    private boolean isLive = false;
    private int currentMessageCount = 0;
    private String currentGame = "Not Playing";
    private SecureRandom random = new SecureRandom();
    private boolean purgeURLS = false;
    private boolean givePointsWhenOffline = false;

    private int linkTimeout = 1;

    private boolean allowGreetMessage = false;

    private double maxPoints = 100000.0f;

    private double startingPoints = 50.0f;

    private String currencyName = "points";
    private String currencyEmote = "points";
    private String followAnnouncement = "";
    private int maxScreenNameLen = 15;
    private int maxAmountOfNameInList = 25;
    private double pointsTax = 0.0f;
    private boolean silentMode = false;
    private String bgImage = "";

    private ArrayList<WebCommandHandler> commands = new ArrayList<>();
    private ArrayList<WebCommandHandler> internals = new ArrayList<>();
    //todo implement proper user list
    private ArrayList<WebUserHandler> userList = new ArrayList<>();

    public WebChannelHandler(String channel) {
        this.channel = channel;
        this.broadcaster = this.channel.replace("#", "");
        log.info("Joining channel " + this.channel);

        if(Memebot.channelsPrivate.contains(this.channel)) {
            this.channelCollection = Memebot.dbPrivate.getCollection(this.channel);
        } else {
            this.channelCollection = Memebot.db.getCollection(this.channel);
        }

        this.readDB();
    }

    private void readDB() {
        Document channelQuery = new Document("_id", this.channel);
        FindIterable cursor = this.channelCollection.find(channelQuery);
        Document channelData = (Document)cursor.first();
        if (channelData != null) {
            this.maxFileNameLen = (short)channelData.getInteger("maxfilenamelen", this.maxFileNameLen);
            this.raceBaseURL = channelData.getOrDefault("raceurl", this.raceBaseURL).toString();
            this.fileNameList = (java.util.ArrayList<String>)channelData.getOrDefault("fileanmelist", this.fileNameList);
            this.otherLoadedChannels = (java.util.ArrayList<String>)channelData.getOrDefault("otherchannels", this.otherLoadedChannels);
            this.pointsPerUpdate = (double)channelData.getOrDefault("pointsperupdate", this.pointsPerUpdate);
            this.allowAutogreet = (boolean)channelData.getOrDefault("allowautogreet", this.allowAutogreet);
            this.linkTimeout = (int)channelData.getOrDefault("linktimeout", this.linkTimeout);
            this.purgeURLS = (boolean)channelData.getOrDefault("purgelinks", this.purgeURLS);
            this.silentMode = (boolean)channelData.getOrDefault("silent", this.silentMode);
            this.givePointsWhenOffline = (boolean)channelData.getOrDefault("pointswhenoffline", this.givePointsWhenOffline);
            this.allowGreetMessage = (boolean)channelData.getOrDefault("allowgreetmessage", this.givePointsWhenOffline);
            this.maxPoints = (double)channelData.getOrDefault("maxpoints", this.maxPoints);
            this.local = channelData.getOrDefault("local", this.local).toString();
            this.currencyName = channelData.getOrDefault("currname", this.currencyName).toString();
            this.currencyEmote = channelData.getOrDefault("curremote", this.currencyEmote).toString();
            this.followAnnouncement = channelData.getOrDefault("followannouncement", this.followAnnouncement).toString();
            this.maxScreenNameLen = (int)channelData.getOrDefault("maxscreennamelen", this.maxScreenNameLen);
            this.maxAmountOfNameInList = (int)channelData.getOrDefault("maxnameinlist", this.maxScreenNameLen);
            this.pointsTax = (double)channelData.getOrDefault("pointstax", this.pointsTax);
            this.startingPoints = (double)channelData.getOrDefault("startingpoints", this.startingPoints);
            this.bgImage = channelData.getOrDefault("bgImage", this.bgImage).toString();
        }
        MongoCollection commandCollection = null;
        if(!Memebot.channelsPrivate.contains(this.channel)) {
            commandCollection = Memebot.db.getCollection(this.channel + "_commands");
        } else {
            commandCollection = Memebot.dbPrivate.getCollection(this.channel + "_commands");
        }
        FindIterable comms = commandCollection.find();
        final WebChannelHandler ch = this;
        //todo fix this important!
        comms.forEach(new Block<org.bson.Document>(){

            @Override
            public void apply(final Document doc) {
                if(!(boolean)doc.getOrDefault("hideCommand", false)) {
                    commands.add(new WebCommandHandler(channel, doc.getString("command"), null));
                }
            }
        });

        MongoCollection internalcommandCollection = null;
        if(!Memebot.channelsPrivate.contains(this.channel)) {
            internalcommandCollection = Memebot.db.getCollection("#internal#" + this.channel + "_commands");
        } else {
            internalcommandCollection = Memebot.dbPrivate.getCollection("#internal#" + this.channel + "_commands");
        }
        FindIterable intcomms = internalcommandCollection.find();
        //todo fix this important!
        intcomms.forEach(new Block<org.bson.Document>(){

            @Override
            public void apply(final Document doc) {
                if(!(boolean)doc.getOrDefault("hideCommand", false)) {
                    internals.add(new WebCommandHandler(channel, doc.getString("command"), "#internal#"));
                }
            }
        });

        MongoCollection userCollection = null;
        if(!Memebot.channelsPrivate.contains(this.channel)) {
            internalcommandCollection = Memebot.db.getCollection(this.channel + "_users");
        } else {
            internalcommandCollection = Memebot.dbPrivate.getCollection(this.channel + "_users");
        }

        FindIterable users = internalcommandCollection.find();
        //todo fix this important!
        users.forEach(new Block<org.bson.Document>(){

            @Override
            public void apply(final Document doc) {
                userList.add(new WebUserHandler(doc.getString("_id"), channel));
            }
        });

        Collections.sort(commands);
        Collections.sort(internals);
        Collections.sort(userList);
    }

    public static Logger getLog() {
        return log;
    }

    public String getBgImage() {
        return bgImage;
    }

    public void setBgImage(String bgImage) {
        this.bgImage = bgImage;
    }

    public ArrayList<WebUserHandler> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<WebUserHandler> userList) {
        this.userList = userList;
    }

    public static void setLog(Logger log) {
        WebChannelHandler.log = log;
    }

    public String getChannel() {
        return channel;
    }

    public ArrayList<WebCommandHandler> getInternals() {
        return internals;
    }

    public void setInternals(ArrayList<WebCommandHandler> internals) {
        this.internals = internals;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getBroadcaster() {
        return broadcaster;
    }

    public void setBroadcaster(String broadcaster) {
        this.broadcaster = broadcaster;
    }

    public String getFollowerNotification() {
        return followerNotification;
    }

    public void setFollowerNotification(String followerNotification) {
        this.followerNotification = followerNotification;
    }

    public String getRaceBaseURL() {
        return raceBaseURL;
    }

    public void setRaceBaseURL(String raceBaseURL) {
        this.raceBaseURL = raceBaseURL;
    }

    public String getGreetMessage() {
        return greetMessage;
    }

    public void setGreetMessage(String greetMessage) {
        this.greetMessage = greetMessage;
    }

    public String getCurrentRaceURL() {
        return currentRaceURL;
    }

    public void setCurrentRaceURL(String currentRaceURL) {
        this.currentRaceURL = currentRaceURL;
    }

    public ArrayList<String> getFileNameList() {
        return fileNameList;
    }

    public void setFileNameList(ArrayList<String> fileNameList) {
        this.fileNameList = fileNameList;
    }

    public short getMaxFileNameLen() {
        return maxFileNameLen;
    }

    public void setMaxFileNameLen(short maxFileNameLen) {
        this.maxFileNameLen = maxFileNameLen;
    }

    public String getCurrentFileName() {
        return currentFileName;
    }

    public void setCurrentFileName(String currentFileName) {
        this.currentFileName = currentFileName;
    }

    public long getStreamStartTime() {
        return streamStartTime;
    }

    public void setStreamStartTime(long streamStartTime) {
        this.streamStartTime = streamStartTime;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public ArrayList<String> getOtherLoadedChannels() {
        return otherLoadedChannels;
    }

    public void setOtherLoadedChannels(ArrayList<String> otherLoadedChannels) {
        this.otherLoadedChannels = otherLoadedChannels;
    }

    public MongoCollection<Document> getChannelCollection() {
        return channelCollection;
    }

    public void setChannelCollection(MongoCollection<Document> channelCollection) {
        this.channelCollection = channelCollection;
    }

    public double getPointsPerUpdate() {
        return pointsPerUpdate;
    }

    public void setPointsPerUpdate(double pointsPerUpdate) {
        this.pointsPerUpdate = pointsPerUpdate;
    }

    public Thread getT() {
        return t;
    }

    public void setT(Thread t) {
        this.t = t;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void setJoined(boolean joined) {
        isJoined = joined;
    }

    public boolean isAllowAutogreet() {
        return allowAutogreet;
    }

    public void setAllowAutogreet(boolean allowAutogreet) {
        this.allowAutogreet = allowAutogreet;
    }

    public boolean isLive() {
        return isLive;
    }

    public void setLive(boolean live) {
        isLive = live;
    }

    public int getCurrentMessageCount() {
        return currentMessageCount;
    }

    public void setCurrentMessageCount(int currentMessageCount) {
        this.currentMessageCount = currentMessageCount;
    }

    public String getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(String currentGame) {
        this.currentGame = currentGame;
    }

    public SecureRandom getRandom() {
        return random;
    }

    public void setRandom(SecureRandom random) {
        this.random = random;
    }

    public boolean isPurgeURLS() {
        return purgeURLS;
    }

    public void setPurgeURLS(boolean purgeURLS) {
        this.purgeURLS = purgeURLS;
    }

    public boolean isGivePointsWhenOffline() {
        return givePointsWhenOffline;
    }

    public void setGivePointsWhenOffline(boolean givePointsWhenOffline) {
        this.givePointsWhenOffline = givePointsWhenOffline;
    }

    public int getLinkTimeout() {
        return linkTimeout;
    }

    public void setLinkTimeout(int linkTimeout) {
        this.linkTimeout = linkTimeout;
    }

    public boolean isAllowGreetMessage() {
        return allowGreetMessage;
    }

    public void setAllowGreetMessage(boolean allowGreetMessage) {
        this.allowGreetMessage = allowGreetMessage;
    }

    public double getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(double maxPoints) {
        this.maxPoints = maxPoints;
    }

    public double getStartingPoints() {
        return startingPoints;
    }

    public void setStartingPoints(double startingPoints) {
        this.startingPoints = startingPoints;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public String getCurrencyEmote() {
        return currencyEmote;
    }

    public void setCurrencyEmote(String currencyEmote) {
        this.currencyEmote = currencyEmote;
    }

    public String getFollowAnnouncement() {
        return followAnnouncement;
    }

    public void setFollowAnnouncement(String followAnnouncement) {
        this.followAnnouncement = followAnnouncement;
    }

    public int getMaxScreenNameLen() {
        return maxScreenNameLen;
    }

    public void setMaxScreenNameLen(int maxScreenNameLen) {
        this.maxScreenNameLen = maxScreenNameLen;
    }

    public int getMaxAmountOfNameInList() {
        return maxAmountOfNameInList;
    }

    public void setMaxAmountOfNameInList(int maxAmountOfNameInList) {
        this.maxAmountOfNameInList = maxAmountOfNameInList;
    }

    public double getPointsTax() {
        return pointsTax;
    }

    public void setPointsTax(double pointsTax) {
        this.pointsTax = pointsTax;
    }

    public boolean isSilentMode() {
        return silentMode;
    }

    public void setSilentMode(boolean silentMode) {
        this.silentMode = silentMode;
    }

    public ArrayList<WebCommandHandler> getCommands() {
        return commands;
    }

    public void setCommands(ArrayList<WebCommandHandler> commands) {
        this.commands = commands;
    }
}
