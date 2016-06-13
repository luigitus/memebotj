package me.krickl.memebotj;

import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Commands.CommandRefernce;
import me.krickl.memebotj.Commands.Internal.*;
import me.krickl.memebotj.Connection.ConnectionInterface;
import me.krickl.memebotj.Connection.IRCConnectionHandler;
import me.krickl.memebotj.Database.DatabaseInterface;
import me.krickl.memebotj.Database.DatabaseObjectInterface;
import me.krickl.memebotj.Database.JSONInterface;
import me.krickl.memebotj.Database.MongoHandler;
import me.krickl.memebotj.Exceptions.DatabaseReadException;
import me.krickl.memebotj.Exceptions.LoginException;
import me.krickl.memebotj.SpeedrunCom.Model.Game;
import me.krickl.memebotj.SpeedrunCom.Model.UserObject;
import me.krickl.memebotj.Utility.ChatColours;
import me.krickl.memebotj.Utility.Cooldown;
import me.krickl.memebotj.Utility.Localisation;
import me.krickl.memebotj.Utility.MessagePackage;
import org.bson.Document;
import org.json.simple.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * This file is part of memebotj.
 * Created by unlink on 07/04/16.
 */
public class ChannelHandler implements Runnable, Comparable<ChannelHandler>, DatabaseObjectInterface, JSONInterface {
    public static Logger log = Logger.getLogger(ChannelHandler.class.getName());
    // todo old db code - remove soon
    //private MongoCollection<Document> channelCollection = null;
    DatabaseInterface mongoHandler = null;
    private BufferedWriter writer = null;
    private int nextID = 0;
    private String channel = "";
    private ConnectionInterface connection = null;
    private String broadcaster = this.channel.replace("#", "");
    private HashMap<String, UserHandler> userList = new java.util.HashMap<String, UserHandler>();
    private Cooldown updateCooldown = new Cooldown(600);
    private Cooldown shortUpdateCooldown = new Cooldown(60);
    private ArrayList<CommandRefernce> channelCommands = new ArrayList<CommandRefernce>();
    private ArrayList<CommandHandler> internalCommands = new ArrayList<CommandHandler>();
    //this is a collection of channel command namaes
    private String followerNotification = "";
    private String raceBaseURL = "http://kadgar.net/live";
    private String greetMessage = "Hello I'm {botnick} {version} the dankest irc bot ever RitzMitz";
    private String currentRaceURL = "";
    private ArrayList<String> fileNameList = new ArrayList<>();
    private int maxFileNameLen = 8;
    private String currentFileName = "";
    private long streamStartTime = 0;
    private String local = Memebot.defaultLocal.getLocal();
    private String channelPageBaseURL = Memebot.webBaseURL + "/commands/" + this.broadcaster;
    private String htmlDir = Memebot.htmlDir + "/" + this.broadcaster;
    private ArrayList<String> otherLoadedChannels = new java.util.ArrayList<String>();
    private double pointsPerUpdate = 1.0f;
    private Thread t = null;
    private boolean isJoined = true;
    private boolean allowAutogreet = true;
    private boolean isLive = false;
    private int currentMessageCount = 0;
    private Cooldown messageLimitCooldown = new Cooldown(30);
    private Cooldown preventMessageCooldown = new Cooldown(30);
    private String streamTitle = "";
    private String currentGame = "Not Playing";
    private SecureRandom random = new SecureRandom();
    private boolean purgeURLS = false;
    private boolean givePointsWhenOffline = false;

    private int linkTimeout = 1;

    private UserHandler broadcasterHandler = new UserHandler(this.broadcaster, this.channel);
    private File htmlDirF = new File(this.htmlDir);
    private UserHandler readOnlyUser = new UserHandler("#readonly#", this.channel, "#readonly#");
    private boolean allowGreetMessage = false;

    private double maxPoints = 100000.0f;

    private double startingPoints = 50.0f;

    private String currencyName = "points";
    private String currencyEmote = "points";
    private String followAnnouncement = "";
    private int maxScreenNameLen = 15;
    private int maxAmountOfNameInList = 25;
    private double pointsTax = 0.0f;
    private UserObject user;
    private Game game;
    private boolean silentMode = false;
    private Localisation localisation = new Localisation(this.local);
    private String bgImage = "";

    private boolean useWhisper = false;
    private Cooldown reconnectCooldown = new Cooldown(40);
    private String itemDrops = "mm";
    private String uptimeString = "";

    private boolean useAlias = true;

    private boolean pointsUpdateDone = false;

    private boolean useRotatingColours = false;

    private Document aliasList = new Document();

    private Cooldown longUpdateCooldown = new Cooldown(600, 0);

    private boolean overrideChannelInformation = false;

    private int neededAutogreetCommandPower = 25;

    public ChannelHandler(String channel, ConnectionInterface connection) {
        this.channel = channel;
        this.connection = connection;
        this.broadcaster = this.channel.replace("#", "");
        broadcasterHandler = new UserHandler(this.broadcaster, this.channel);
        readOnlyUser = new UserHandler("#readonly#", this.channel, "#readonly#");
        channelPageBaseURL = Memebot.webBaseURL + "/commands/" + this.broadcaster;
        htmlDir = Memebot.htmlDir + "/" + this.broadcaster;
        log.info("Joining channel " + this.channel);
        try {
            File f = new File(Memebot.memebotDir + "/logs/" + channel + ".log");
            if (!f.exists())
                f.createNewFile();
            writer = new BufferedWriter(new FileWriter(f, true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        broadcasterHandler.setUserBroadcaster(true);

        broadcasterHandler.setModerator(true);

        this.userList.put(this.broadcaster, broadcasterHandler);
        this.userList.put("#readonly#", readOnlyUser);

        if (!htmlDirF.exists()) {
            htmlDirF.mkdirs();
        }
        this.joinChannel(this.channel);

        if (Memebot.useMongo) {
            if (!Memebot.channelsPrivate.contains(this.channel)) {
                // todo old db code - remove soon
                //this.channelCollection = Memebot.db.getCollection(this.channel);
                mongoHandler = new MongoHandler(Memebot.db, this.channel);
            } else {
                // todo old db code - remove soon
                //this.channelCollection = Memebot.dbPrivate.getCollection(this.channel);
                mongoHandler = new MongoHandler(Memebot.dbPrivate, this.channel);
            }
        }

        this.readDB();
        localisation = new Localisation(this.local);

        // todo add all internal commands
        this.internalCommands.add(new AboutCommand(this, "!about", "#internal#"));
        this.internalCommands.add(new AutogreetCommand(this, "!autogreet", "#internal#"));
        this.internalCommands.add(new EditChannelCommand(this, "!channel", "#internal#"));
        this.internalCommands.add(new HelpCommand(this, "!help", "#internal#"));
        this.internalCommands.add(new HugCommand(this, "!mehug", "#internal#"));
        this.internalCommands.add(new JoinCommand(this, "!mejoin", "#internal#"));
        this.internalCommands.add(new PartCommand(this, "!mepart", "#internal#"));
        this.internalCommands.add(new PointsCommand(this, "!points", "#internal#"));
        this.internalCommands.add(new QuitCommand(this, "!mequit", "#internal#"));
        this.internalCommands.add(new RaceCommand(this, "!race", "#internal#"));
        this.internalCommands.add(new SaveCommand(this, "!mesave", "#internal#"));
        this.internalCommands.add(new WhoisCommand(this, "!whois", "#internal#"));
        this.internalCommands.add(new FilenameCommand(this, "!name", "#internal#"));
        this.internalCommands.add(new EdituserCommand(this, "!user", "#internal#"));
        this.internalCommands.add(new SendMessageCommand(this, "!sm", "#internal#"));
        this.internalCommands.add(new DampeCommand(this, "!dampe", "#internal#"));
        this.internalCommands.add(new DebugCommand(this, "!debug", "#debug#"));
        this.internalCommands.add(new PyramidCommand(this, "!pyramid", "#internal#"));
        this.internalCommands.add(new CommandManager(this, "!command", "#internal#"));
        this.internalCommands.add(new ChannelInfoCommand(this, "!ci", "#internal#"));
        this.internalCommands.add(new BobRossCommand(this, "!bobross", "#internal#"));
        this.internalCommands.add(new InvertedPyramidCommand(this, "!dimaryp", "#internal#"));
        this.internalCommands.add(new RestartThreadCommand(this, "!restartt", "#internal#"));
        this.internalCommands.add(new LoginCredentials(this, "!setlogin", "#internal#"));
        this.internalCommands.add(new SongRequestCommand(this, "!songrequest", "#internal#"));
        //this.internalCommands.add(new GrassCommand(this, "!grass", "#internal#"));
        //this.internalCommands.add(new InventoryCommand(this, "!inventory", "#internal#"));
        this.internalCommands.add(new PersonalBestCommand(this, "!pb", "#internal#"));
        this.internalCommands.add(new WorldRecordCommand(this, "!wr", "#internal#"));
        this.internalCommands.add(new UptimeCommand(this, "!uptime", "#internal#"));
        this.internalCommands.add(new DoggyRaceCommand(this, "!doggy", "#internal#"));
        this.internalCommands.add(new AliasCommand(this, "!alias", "#internal#"));

        //this.internalCommands.add(new PersonalBestCommand(this, "!pb", "#internal#"));
        // todo implement this this. internalCommands.add(new LotteryCommand(this, "!lottery", "#internal#"));

        CommandHandler issueCommand = new CommandHandler(this, "!issue", "#internal#");
        issueCommand.editCommand("output", "{sender}: Having issues? Write a bugreport at https://github.com/unlink2/memebotj/issues", new UserHandler("#internal#", this.channel));
        this.internalCommands.add(issueCommand);

        CommandHandler annoyingDog = new CommandHandler(this, "AnnoyingZ", "#internal#");
        annoyingDog.setTextTrigger(true);
        annoyingDog.setUnformattedOutput("http://annoying.dog ");
        annoyingDog.setCooldown(new Cooldown(6000));
        this.internalCommands.add(annoyingDog);

        if (this.allowGreetMessage) {
            this.sendMessage(Memebot.formatText(this.greetMessage, this, readOnlyUser, null, false, new String[]{}, ""));
        }

        if (!this.pointsUpdateDone) {
            this.pointsPerUpdate = pointsPerUpdate / 10;
            this.pointsUpdateDone = true;
        }
    }

    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        ChannelHandler.log = log;
    }

    public void partChannel(String channel) {
        this.connection.sendMessageBytes("PART " + channel + "\n");
        boolean isInList = false;
        ChannelHandler removeThisCH = null;
        for (ChannelHandler ch : Memebot.joinedChannels) {
            if (ch.getChannel().equalsIgnoreCase(channel)) {
                isInList = true;
                removeThisCH = ch;
                break;
            }
        }

        if (isInList && removeThisCH != null) {
            Memebot.joinedChannels.remove(removeThisCH);
            new File(Memebot.memebotDir + "/channels/" + channel).delete();

        }
        this.sendMessage("Leaving channel :(", this.channel);
        this.isJoined = false;
        this.t.interrupt();
        this.connection.close();
        Memebot.joinedChannels.remove(removeThisCH);
    }

    private void joinChannel(String channel) {
        this.connection.sendMessageBytes("JOIN " + channel + "\n");
        boolean isInList = false;

        for (ChannelHandler ch : Memebot.joinedChannels) {
            if (ch.getChannel().equalsIgnoreCase(channel)) {
                isInList = true;
                break;
            }
        }

        if (!isInList) {
            try {
                new File(Memebot.memebotDir + "/channels/" + channel).createNewFile();
            } catch (IOException e) {
                log.warning(e.toString());
            }
        }
        this.isJoined = true;
    }

    @Override
    public void run() {
        //let update loop run independently
        if (Memebot.useUpdateThread) {
            Thread updateThread = new Thread() {
                @Override
                public void run() {
                    while (isJoined) {
                        update();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            };
            updateThread.start();
        }

        while (this.isJoined) {
            try {
                String ircmsg = this.connection.recvData();

                //if (this.channel.equalsIgnoreCase(ircmsg[0])) {
                this.handleMessage(ircmsg);
                //}
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!Memebot.useUpdateThread) {
                    this.update();
                }
            } catch (LoginException e) {
                log.info(e.toString());
                // fallback in case of login issues - try again
                if (this.reconnectCooldown.canContinue()) {
                    this.connection = new IRCConnectionHandler(Memebot.ircServer, Memebot.ircport, Memebot.botNick, Memebot.botPassword);
                    reconnectCooldown.startCooldown();
                }
            }
        }

        try {
            writer.close();
        } catch (IOException e) {

        }
    }

    public void update() {
        if (this.messageLimitCooldown.canContinue()) {
            this.messageLimitCooldown.startCooldown();
            this.currentMessageCount = 0;
        }

        if (this.shortUpdateCooldown.canContinue()) {
            this.shortUpdateCooldown.startCooldown();

            ArrayList<String> removeUsers = new java.util.ArrayList<String>();
            Iterator it = this.userList.keySet().iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                UserHandler uh = this.userList.get(key);
                uh.update(this);
                if (this.isLive || this.givePointsWhenOffline) {
                    uh.setPoints(uh.getPoints() + this.pointsPerUpdate);
                }
                uh.writeDB();

                if (uh.canRemove()) {
                    removeUsers.add(key);
                }
            }
            for (String user : removeUsers) {
                this.userList.remove(user);
            }

            for (CommandRefernce ch : this.channelCommands) {
                ch.update();
            }

        }

        if (this.updateCooldown.canContinue()) {
            this.updateCooldown.startCooldown();

            //update channel api information
            //twitchChannelAPI.update();
            //speedRunComAPI.update();

            this.writeDB();
            this.writeHTML();
        }

        if (this.longUpdateCooldown.canContinue()) {
            this.longUpdateCooldown.startCooldown();

            if (this.useRotatingColours || connection.getBotNick().equals(Memebot.botNick)) {
                ChatColours.pickColour(this, new UserHandler("#internal#", channel));
            }
        }
    }

    public void writeHTML() {

    }

    public void handleMessage(String rawmessage) {
        useWhisper = false;
        MessagePackage msgPackage = connection.handleMessage(rawmessage, this);

        if (msgPackage == null) {
            return;
        }
        msgPackage.handleAlias(aliasList);

        // if channel does not match ignore the message
        if (!msgPackage.channel.equals(this.channel)) {
            // todo send message to the right channel
            return;
        }

        if (msgPackage.messageType.equals("WHISPER")) {
            useWhisper = true;
            msgPackage.messageType = "PRIVMSG";
        }

        String[] msgContent = msgPackage.messageContent;
        UserHandler sender = msgPackage.sender;
        String msgType = msgPackage.messageType;

        if (!msgType.equals("PRIVMSG")) {
            useWhisper = false;
            return;
        }

        //log chat content
        String messageString = "";
        for (String msg : msgContent) {
            messageString = messageString + msg + " ";
        }
        System.out.println("<" + channel + ">" + sender.getUsername() + ": " + messageString);

        String msg = msgContent[0];
        String[] data = java.util.Arrays.copyOfRange(msgContent, 0, msgContent.length);
        if (this.purgeURLS) {
            for (String username : Memebot.globalBanList) {
                if (sender.getUsername().equals(username) && !sender.isModerator()) {
                    this.sendMessage("/ban " + sender.getUsername());
                }
            }

            for (String message : Memebot.urlBanList) {
                if (rawmessage.contains(message)) {
                    this.sendMessage("/timeout " + sender.getUsername() + " 1");
                }
            }

            for (String message : Memebot.phraseBanList) {
                if (rawmessage.contains(message)) {
                    this.sendMessage("/timeout " + sender.getUsername() + " 1");
                }
            }
        }

        CommandHandler ch = null;
        CommandRefernce cr = null;

        //internal text triggers
        for (String s : msgContent) {
            ch = this.findCommandForString(s, this.internalCommands);

            if (ch != null) {
                if (ch.isTextTrigger()) {
                    ch.executeCommand(sender, new String[]{"", ""});
                }
            }
        }

        //internal commands
        ch = this.findCommandForString(msg, this.internalCommands);
        if (ch != null) {
            if (!ch.isTextTrigger()) {
                ch.executeCommand(sender, java.util.Arrays.copyOfRange(data, 1, data.length));
            }
        }

        //channel commands
        cr = this.findCommandReferneceForString(msg, this.channelCommands);
        if (cr != null) {
            if (!cr.getCH().isTextTrigger()) {
                cr.executeCommand(sender, data);
            }
        }

        //text triggers
        for (String s : msgContent) {
            cr = this.findCommandReferneceForString(s, this.channelCommands);

            if (cr != null) {
                if (cr.getCH().isTextTrigger()) {
                    cr.executeCommand(sender, new String[]{"", ""});
                }
            }
        }

        //todo other channel's commands
        for (ChannelHandler channelHandler : Memebot.joinedChannels) {
            for (String och : this.otherLoadedChannels) {
                String channel = channelHandler.getBroadcaster();
                if (channelHandler.getChannel().equals(och) || channelHandler.getBroadcaster().equals(och)) {
                    cr = channelHandler.findCommandReferneceForString(msg.replace(och.replace("#", "") + ".", ""),
                            channelHandler.getChannelCommands());
                    if (cr != null && msg.contains(channel)) {
                        cr.executeCommand(readOnlyUser, data);
                    }
                }
            }
        }

        //set user activity
        //sender.setTimeSinceActivity(System.currentTimeMillis())
        useWhisper = false;
    }

    /***
     * @param mesgessage
     * @deprecated Deprecated: This method is deprecated to make sure the whisper feature works with every output
     */
    @Deprecated
    public void sendMessage(String mesgessage) {
        sendMessage(mesgessage, this.channel, readOnlyUser);
    }

    /***
     * @param mesgessage
     * @param channel
     * @deprecated Deprecated: This method is deprecated to make sure the whisper feature works with every output
     */
    @Deprecated
    public void sendMessage(String mesgessage, String channel) {
        sendMessage(mesgessage, channel, readOnlyUser);
    }

    /***
     * @param mesgessage
     * @param channel
     * @param sender
     * @deprecated Deprecated: This method is deprecated to make sure the whisper feature works with every output
     */
    @Deprecated
    public void sendMessage(String mesgessage, String channel, UserHandler sender) {
        sendMessage(mesgessage, channel, sender, false);
    }

    public void sendMessage(String mesgessage, String channel, UserHandler sender, boolean whisper) {
        sendMessage(mesgessage, channel, sender, whisper, false);
    }

    public void sendMessage(String mesgessage, String channel, UserHandler sender, boolean whisper, boolean forcechat) {
        sendMessage(mesgessage, channel, sender, whisper, forcechat, false);
    }

    public void sendMessage(String mesgessage, String channel, UserHandler sender, boolean whisper, boolean forcechat,
                            boolean allowIgnored) {
        String msg = mesgessage;
        if (msg.isEmpty()) {
            return;
        }
        if (!this.preventMessageCooldown.canContinue()) {
            return;
        }
        if (this.silentMode) {
            return;
        }
        if (this.currentMessageCount >= me.krickl.memebotj.Memebot.messageLimit) {
            log.warning("Reached global message limit for 30 seconds. try again later");
            this.preventMessageCooldown.startCooldown();
        }
        // ignore /ignore to avoid people being ignored by the bot
        String[] ignoredMessages = new String[]{"/ignore", "/color", ".ignore", ".color", ".unmod", "/unmod",
                "/mod", ".mod"};
        for (String ignoredStr : ignoredMessages) {
            if (msg.startsWith(ignoredStr) && !allowIgnored) {
                msg.replaceFirst("/", "");
                msg.replaceFirst(".", "");
            }
        }

        this.currentMessageCount += 1;
        //log to file
        System.out.println("<" + channel + ">" + msg);

        // force outut to both chat and whisper
        if (forcechat && (whisper || useWhisper)) {
            if (sender.getUsername().equals("#readonly#")) {
                this.connection.sendMessage("PRIVMSG " + sender.getChannelOrigin() + " : " + msg + "\n");
            } else {
                this.connection.sendMessage("PRIVMSG " + channel + " :" + msg + "\n");
            }
        }

        if (!whisper && !useWhisper) {
            if (sender.getUsername().equals("#readonly#")) {
                this.connection.sendMessage("PRIVMSG " + sender.getChannelOrigin() + " : " + msg + "\n");
            } else {
                this.connection.sendMessage("PRIVMSG " + channel + " :" + msg + "\n");
            }
        } else {
            if (sender.getUsername().equals("#readonly#")) {
                this.connection.sendMessage("PRIVMSG #jtv :/w " + sender.getUsername() + " <" + sender.getChannelOrigin() + "> " + msg + "\n");
            } else {
                this.connection.sendMessage("PRIVMSG #jtv :/w " + sender.getUsername() + " <" + channel + "> " + msg + "\n");
            }
        }
    }

    public CommandRefernce findCommandReferneceForString(String command, ArrayList<CommandRefernce> commands) {
        for (CommandRefernce commandHandler : commands) {
            if (commandHandler.getCommandName().equals(command)) {
                return commandHandler;
            }
        }
        return null;
    }

    public CommandHandler findCommandForString(String command, ArrayList<CommandHandler> commands) {
        for (CommandHandler commandHandler : commands) {
            if (commandHandler.getCommandName().equals(command)) {
                return commandHandler;
            }
        }
        return null;
    }

    public void start() {
        if (t == null) {
            t = new Thread(this, this.channel);
            t.start();
        }
    }

    public void readDB() {
        if (!Memebot.useMongo) {
            return;
        }

        try {
            mongoHandler.readDatabase(this.channel);
        } catch (DatabaseReadException e) {
            log.warning(e.toString());
        }

        if (mongoHandler.getDocument() != null) {
            this.maxFileNameLen = (int) mongoHandler.getObject("maxfilenamelen", this.maxFileNameLen);
            this.raceBaseURL = mongoHandler.getObject("raceurl", this.raceBaseURL).toString();
            this.fileNameList = (java.util.ArrayList<String>) mongoHandler.getObject("fileanmelist", this.fileNameList);
            this.otherLoadedChannels = (java.util.ArrayList<String>) mongoHandler.getObject("otherchannels", this.otherLoadedChannels);
            this.pointsPerUpdate = (double) mongoHandler.getObject("pointsperupdate", this.pointsPerUpdate);
            this.allowAutogreet = (boolean) mongoHandler.getObject("allowautogreet", this.allowAutogreet);
            this.linkTimeout = (int) mongoHandler.getObject("linktimeout", this.linkTimeout);
            this.purgeURLS = (boolean) mongoHandler.getObject("purgelinks", this.purgeURLS);
            this.silentMode = (boolean) mongoHandler.getObject("silent", this.silentMode);
            this.givePointsWhenOffline = (boolean) mongoHandler.getObject("pointswhenoffline", this.givePointsWhenOffline);
            this.allowGreetMessage = (boolean) mongoHandler.getObject("allowgreetmessage", this.givePointsWhenOffline);
            this.maxPoints = (double) mongoHandler.getObject("maxpoints", this.maxPoints);
            this.local = mongoHandler.getObject("local", this.local).toString();
            this.currencyName = mongoHandler.getObject("currname", this.currencyName).toString();
            this.currencyEmote = mongoHandler.getObject("curremote", this.currencyEmote).toString();
            this.followAnnouncement = mongoHandler.getObject("followannouncement", this.followAnnouncement).toString();
            this.maxScreenNameLen = (int) mongoHandler.getObject("maxscreennamelen", this.maxScreenNameLen);
            this.maxAmountOfNameInList = (int) mongoHandler.getObject("maxnameinlist", this.maxScreenNameLen);
            this.pointsTax = (double) mongoHandler.getObject("pointstax", this.pointsTax);
            this.startingPoints = (double) mongoHandler.getObject("startingpoints", this.startingPoints);
            this.bgImage = mongoHandler.getObject("bgImage", this.bgImage).toString();
            itemDrops = mongoHandler.getObject("itemDrops", this.itemDrops).toString();
            pointsUpdateDone = (boolean) mongoHandler.getObject("pointsupdate", this.pointsUpdateDone);
            aliasList = (Document) mongoHandler.getObject("aliaslist", this.aliasList);
            useRotatingColours = (boolean) mongoHandler.getObject("rotatingcolours", this.useRotatingColours);
            nextID = (int) mongoHandler.getObject("nextID", nextID);
            overrideChannelInformation = (boolean) mongoHandler.getObject("overrideChannelInformation", overrideChannelInformation);
            currentGame = (String) mongoHandler.getObject("currentgame", currentGame);
            streamTitle = (String) mongoHandler.getObject("currenttitle", streamTitle);
            isLive = (boolean) mongoHandler.getObject("islive", isLive);
            neededAutogreetCommandPower = (int) mongoHandler.getObject("neededAutogreetCommandPower", isLive);
        }

        // read commands
        MongoHandler mongoHandler = null;
        if (!Memebot.channelsPrivate.contains(this.channel)) {
            mongoHandler = new MongoHandler(Memebot.db, this.channel + "_commands");
        } else {
            mongoHandler = new MongoHandler(Memebot.dbPrivate, this.channel + "_commands");
        }

        for (Document doc : mongoHandler.getDocuments()) {
            channelCommands.add(new CommandRefernce(this, doc.getString("command"), ""));
        }
    }

    public void setDB() {
        mongoHandler.updateDocument("_id", this.channel);
        mongoHandler.updateDocument("maxfilenamelen", this.maxFileNameLen);
        mongoHandler.updateDocument("raceurl", this.raceBaseURL);
        mongoHandler.updateDocument("fileanmelist", this.fileNameList);
        mongoHandler.updateDocument("otherchannels", this.otherLoadedChannels);
        mongoHandler.updateDocument("pointsperupdate", this.pointsPerUpdate);
        mongoHandler.updateDocument("allowautogreet", this.allowAutogreet);
        mongoHandler.updateDocument("purgelinks", this.purgeURLS);
        mongoHandler.updateDocument("linktimeout", this.linkTimeout);
        mongoHandler.updateDocument("silent", this.silentMode);
        mongoHandler.updateDocument("pointswhenoffline", this.givePointsWhenOffline);
        mongoHandler.updateDocument("allowgreetmessage", this.allowGreetMessage);
        mongoHandler.updateDocument("maxpoints", this.maxPoints);
        mongoHandler.updateDocument("local", this.local);
        mongoHandler.updateDocument("currname", this.currencyName);
        mongoHandler.updateDocument("curremote", this.currencyEmote);
        mongoHandler.updateDocument("followannouncement", this.followAnnouncement);
        mongoHandler.updateDocument("maxscreennamelen", this.maxScreenNameLen);
        mongoHandler.updateDocument("maxnameinlist", this.maxAmountOfNameInList);
        mongoHandler.updateDocument("pointstax", this.pointsTax);
        mongoHandler.updateDocument("startingpoints", this.startingPoints);
        mongoHandler.updateDocument("bgImage", this.bgImage);
        mongoHandler.updateDocument("itemDrops", this.itemDrops);
        mongoHandler.updateDocument("pointsupdate", this.pointsUpdateDone);
        mongoHandler.updateDocument("aliaslist", this.aliasList);
        mongoHandler.updateDocument("rotatingcolours", this.useRotatingColours);
        mongoHandler.updateDocument("nextID", this.nextID);
        mongoHandler.updateDocument("overrideChannelInformation", overrideChannelInformation);
        mongoHandler.updateDocument("currentgame", currentGame);
        mongoHandler.updateDocument("currenttitle", streamTitle);
        mongoHandler.updateDocument("islive", isLive);
        mongoHandler.updateDocument("neededAutogreetCommandPower", neededAutogreetCommandPower);

        //mongoHandler.setDocument(channelData);
    }

    public void removeDB() {

    }

    public void writeDB() {
        if (!Memebot.useMongo) {
            return;
        }

        setDB();

        mongoHandler.writeDatabase(this.channel);
    }

    public String getNextID() {
        nextID++;
        return Integer.toHexString(nextID);
    }

    @Override
    public int compareTo(ChannelHandler another) {
        return channel.compareTo(another.getChannel());
    }

    public int getNeededAutogreetCommandPower() {
        return neededAutogreetCommandPower;
    }

    public void setNeededAutogreetCommandPower(int neededAutogreetCommandPower) {
        this.neededAutogreetCommandPower = neededAutogreetCommandPower;
    }

    public void setNextID(int nextID) {
        this.nextID = nextID;
    }

    public Cooldown getLongUpdateCooldown() {
        return longUpdateCooldown;
    }

    public void setLongUpdateCooldown(Cooldown longUpdateCooldown) {
        this.longUpdateCooldown = longUpdateCooldown;
    }

    public boolean isOverrideChannelInformation() {
        return overrideChannelInformation;
    }

    public void setOverrideChannelInformation(boolean overrideChannelInformation) {
        this.overrideChannelInformation = overrideChannelInformation;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public ConnectionInterface getConnection() {
        return connection;
    }

    public void setConnection(ConnectionInterface connection) {
        this.connection = connection;
    }

    public String getBroadcaster() {
        return broadcaster;
    }

    public void setBroadcaster(String broadcaster) {
        this.broadcaster = broadcaster;
    }

    public HashMap<String, UserHandler> getUserList() {
        return userList;
    }

    public void setUserList(HashMap<String, UserHandler> userList) {
        this.userList = userList;
    }

    public UserObject getUser() {
        return user;
    }

    public void setUser(UserObject user) {
        this.user = user;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Cooldown getUpdateCooldown() {
        return updateCooldown;
    }

    public void setUpdateCooldown(Cooldown updateCooldown) {
        this.updateCooldown = updateCooldown;
    }

    public ArrayList<CommandRefernce> getChannelCommands() {
        return channelCommands;
    }

    public void setChannelCommands(ArrayList<CommandRefernce> channelCommands) {
        this.channelCommands = channelCommands;
    }

    public ArrayList<CommandHandler> getInternalCommands() {
        return internalCommands;
    }

    public void setInternalCommands(ArrayList<CommandHandler> internalCommands) {
        this.internalCommands = internalCommands;
    }

    public String getBgImage() {
        return bgImage;
    }

    public void setBgImage(String bgImage) {
        this.bgImage = bgImage;
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

    public int getMaxFileNameLen() {
        return maxFileNameLen;
    }

    public void setMaxFileNameLen(int maxFileNameLen) {
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

    public String getChannelPageBaseURL() {
        return channelPageBaseURL;
    }

    public void setChannelPageBaseURL(String channelPageBaseURL) {
        this.channelPageBaseURL = channelPageBaseURL;
    }

    public String getHtmlDir() {
        return htmlDir;
    }

    public void setHtmlDir(String htmlDir) {
        this.htmlDir = htmlDir;
    }

    public ArrayList<String> getOtherLoadedChannels() {
        return otherLoadedChannels;
    }

    public void setOtherLoadedChannels(ArrayList<String> otherLoadedChannels) {
        this.otherLoadedChannels = otherLoadedChannels;
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

    public Cooldown getMessageLimitCooldown() {
        return messageLimitCooldown;
    }

    public void setMessageLimitCooldown(Cooldown messageLimitCooldown) {
        this.messageLimitCooldown = messageLimitCooldown;
    }

    public Cooldown getPreventMessageCooldown() {
        return preventMessageCooldown;
    }

    public void setPreventMessageCooldown(Cooldown preventMessageCooldown) {
        this.preventMessageCooldown = preventMessageCooldown;
    }

    public String getStreamTitle() {
        return streamTitle;
    }

    public void setStreamTitle(String streamTitle) {
        this.streamTitle = streamTitle;
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

    public boolean isUseAlias() {
        return useAlias;
    }

    public void setUseAlias(boolean useAlias) {
        this.useAlias = useAlias;
    }

    public boolean isPointsUpdateDone() {
        return pointsUpdateDone;
    }

    public void setPointsUpdateDone(boolean pointsUpdateDone) {
        this.pointsUpdateDone = pointsUpdateDone;
    }

    public Document getAliasList() {
        return aliasList;
    }

    public void setAliasList(Document aliasList) {
        this.aliasList = aliasList;
    }

    public int getLinkTimeout() {
        return linkTimeout;
    }

    public void setLinkTimeout(int linkTimeout) {
        this.linkTimeout = linkTimeout;
    }

    public UserHandler getBroadcasterHandler() {
        return broadcasterHandler;
    }

    public void setBroadcasterHandler(UserHandler broadcasterHandler) {
        this.broadcasterHandler = broadcasterHandler;
    }

    public File getHtmlDirF() {
        return htmlDirF;
    }

    public void setHtmlDirF(File htmlDirF) {
        this.htmlDirF = htmlDirF;
    }

    public UserHandler getReadOnlyUser() {
        return readOnlyUser;
    }

    public void setReadOnlyUser(UserHandler readOnlyUser) {
        this.readOnlyUser = readOnlyUser;
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

    public Localisation getLocalisation() {
        return localisation;
    }

    public void setLocalisation(Localisation localisation) {
        this.localisation = localisation;
    }

    public DatabaseInterface getMongoHandler() {
        return mongoHandler;
    }

    public void setMongoHandler(DatabaseInterface mongoHandler) {
        this.mongoHandler = mongoHandler;
    }

    public boolean isUseWhisper() {
        return useWhisper;
    }

    public void setUseWhisper(boolean useWhisper) {
        this.useWhisper = useWhisper;
    }

    public Cooldown getReconnectCooldown() {
        return reconnectCooldown;
    }

    public void setReconnectCooldown(Cooldown reconnectCooldown) {
        this.reconnectCooldown = reconnectCooldown;
    }

    public String getItemDrops() {
        return itemDrops;
    }

    public void setItemDrops(String itemDrops) {
        this.itemDrops = itemDrops;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public void setWriter(BufferedWriter writer) {
        this.writer = writer;
    }

    public String getUptimeString() {
        return uptimeString;
    }

    public void setUptimeString(String uptimeString) {
        this.uptimeString = uptimeString;
    }

    public Cooldown getShortUpdateCooldown() {
        return shortUpdateCooldown;
    }

    public void setShortUpdateCooldown(Cooldown shortUpdateCooldown) {
        this.shortUpdateCooldown = shortUpdateCooldown;
    }

    public int getViewerNumber() {
        return userList.size();
    }

    public boolean isUseRotatingColours() {
        return useRotatingColours;
    }

    public void setUseRotatingColours(boolean useRotatingColours) {
        this.useRotatingColours = useRotatingColours;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject wrapper = new JSONObject();
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("_id", channel);
        jsonObject.put("commands", Memebot.webBaseURL + "/api/commands/" + getBroadcaster());
        jsonObject.put("internals", Memebot.webBaseURL + "/api/internals/" + getBroadcaster());
        jsonObject.put("filenames", Memebot.webBaseURL + "/api/filenames/" + getBroadcaster());
        jsonObject.put("users", Memebot.webBaseURL + "/api/users/" + getBroadcaster());
        jsonObject.put("title", streamTitle);
        jsonObject.put("game", currentGame);
        jsonObject.put("currency_emote", currencyEmote);
        jsonObject.put("currecny_name", currencyName);
        jsonObject.put("current_filename", currentFileName);
        jsonObject.put("is_live", isLive);
        jsonObject.put("overrides_twitch_api", overrideChannelInformation);

        wrapper.put("data", jsonObject);
        wrapper.put("links", Memebot.getLinks(Memebot.webBaseURL + "/api/channels/" + broadcaster,
                Memebot.webBaseURL + "/api/channels", null, null));

        return wrapper;
    }

    @Override
    public String toJSONSString() {
        return toJSONObject().toJSONString();
    }

    @Override
    public boolean fromJSON(String jsonString) {
        return false;
    }

    public String filenamesToJSON() {
        JSONObject wrapper = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("filenames", fileNameList);
        jsonObject.put("_id", getChannel());

        wrapper.put("data", jsonObject);
        wrapper.put("links", Memebot.getLinks(Memebot.webBaseURL + "/api/filenames/" + getBroadcaster(),
                Memebot.webBaseURL + "/api/channels/" + getBroadcaster(), null, null));

        return wrapper.toJSONString();
    }
}
