package me.krickl.memebotj.web;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import me.krickl.memebotj.Memebot;
import org.bson.Document;
import me.krickl.memebotj.web.WebChannelHandler;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;

/**
 * This file is part of memebotj.
 * Created by unlink on 03/04/16.
 */
public class WebCommandHandler implements Comparable<WebCommandHandler> {
    public static Logger log = Logger.getLogger(WebCommandHandler.class.getName());

    private String channelHandler = null;
    private String commandName = null;
    private MongoCollection<Document> commandCollection;

    private int cooldownLength = 0;
    private String helptext = "";
    private int parameters = 0;
    private String commandType = "default";
    private String unformattedOutput = "";
    private String commandScript = "";
    private String quotePrefix = "";
    private String quoteSuffix = "";

    private double cost = 0.0f;
    private int counter = 0;
    private ArrayList<String> listContent = new ArrayList<>();
    private boolean locked = false;
    private boolean isTextTrigger = false;
    private int neededCommandPower = 10;
    private int userCooldownLength = 0;
    private boolean isEnabled = true;
    private int neededCooldownBypassPower = 50;
    private boolean allowPicksFromList = true;
    private boolean overrideHandleMessage = false;
    private int execCounter = 0;
    private boolean caseSensitive = false;
    private boolean formatData = false;
    private boolean checkDefaultCooldown = true;

    private HashMap<String, String> otherData = new HashMap<>();

    private boolean success = false;
    private boolean whisper = false;
    private boolean hideCommand = false;

    public WebCommandHandler(String channelHandler, String commandName, String dbprefix) {
        this.channelHandler = channelHandler;
        this.commandName = commandName;

        if(!Memebot.channelsPrivate.contains(this.getChannelHandler())) {
            if (dbprefix == null) {
                this.commandCollection = Memebot.db.getCollection(this.channelHandler + "_commands");
            } else {
                this.commandCollection = Memebot.db.getCollection(dbprefix + this.channelHandler + "_commands");
            }
        } else {
            if (dbprefix == null) {
                this.commandCollection = Memebot.dbPrivate.getCollection(this.channelHandler + "_commands");
            } else {
                this.commandCollection = Memebot.dbPrivate.getCollection(dbprefix + this.channelHandler + "_commands");
            }
        }

        readDB();
    }

    public void readDB() {
        log.info("Reading db for command " + commandName + " from db on channel " + channelHandler);

        Document commandQuery = new Document("_id", this.commandName);
        FindIterable cursor = this.commandCollection.find(commandQuery);

        Document commandObject = (Document)cursor.first();

        if(commandObject != null) {
            this.commandName = commandObject.getOrDefault("command", this.commandName).toString();
            this.cooldownLength = (int)commandObject.getOrDefault("cooldown", this.cooldownLength);
            this.helptext = commandObject.getOrDefault("helptext", this.helptext).toString();
            this.parameters = (int)commandObject.getOrDefault("param", this.parameters);
            this.commandType = commandObject.getOrDefault("cmdtype", this.commandType).toString();
            this.unformattedOutput = commandObject.getOrDefault("output", this.unformattedOutput).toString();
            this.quoteSuffix = commandObject.getOrDefault("qsuffix", this.quoteSuffix).toString();
            this.quotePrefix = commandObject.getOrDefault("qprefix", this.quotePrefix).toString();
            this.cost = (double)commandObject.getOrDefault("costf", this.cost);
            this.counter = (int)commandObject.getOrDefault("counter", this.counter);
            this.listContent = (ArrayList<String>)commandObject.getOrDefault("listcontent", this.listContent);
            this.locked = (boolean)commandObject.getOrDefault("locked", this.locked);
            this.isTextTrigger = (boolean)commandObject.getOrDefault("texttrigger", this.isTextTrigger);
            this.neededCommandPower = (int)commandObject.getOrDefault("viewerpower", this.neededCommandPower);
            this.userCooldownLength = (int)commandObject.getOrDefault("usercooldown", this.userCooldownLength);
            this.commandScript = commandObject.getOrDefault("script", this.commandScript).toString();
            this.isEnabled = (boolean)commandObject.getOrDefault("enable", this.isEnabled);
            this.neededCooldownBypassPower = (int)commandObject.getOrDefault("cooldownbypass", this.neededCooldownBypassPower);
            this.allowPicksFromList = (boolean)commandObject.getOrDefault("allowpick", this.allowPicksFromList);
            this.overrideHandleMessage = (boolean)commandObject.getOrDefault("overridehandlemessage", this.overrideHandleMessage);
            this.execCounter = (int)commandObject.getOrDefault("execcounter", this.execCounter);
            this.caseSensitive = (boolean)commandObject.getOrDefault("case", this.caseSensitive);
            this.formatData = (boolean)commandObject.getOrDefault("format", this.formatData);
            this.checkDefaultCooldown = (boolean)commandObject.getOrDefault("checkdefaultcooldown", this.checkDefaultCooldown);
            this.hideCommand = (boolean)commandObject.getOrDefault("hideCommand", this.hideCommand);
            //other data are used to store data that are used for internal commands
            Document otherDataDocument = (Document) commandObject.getOrDefault("otherdata", new Document());

            for(String key : otherDataDocument.keySet()) {
                this.otherData.put(key, otherDataDocument.getString(key));
            }
        }
    }

    @Override
    public int compareTo(WebCommandHandler another) {
        return commandName.compareTo(another.getCommandName());
    }

    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        WebCommandHandler.log = log;
    }

    public String getChannelHandler() {
        return channelHandler;
    }

    public void setChannelHandler(String channelHandler) {
        this.channelHandler = channelHandler;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public MongoCollection<Document> getCommandCollection() {
        return commandCollection;
    }

    public void setCommandCollection(MongoCollection<Document> commandCollection) {
        this.commandCollection = commandCollection;
    }

    public int getCooldownLength() {
        return cooldownLength;
    }

    public void setCooldownLength(int cooldownLength) {
        this.cooldownLength = cooldownLength;
    }

    public String getHelptext() {
        return helptext;
    }

    public void setHelptext(String helptext) {
        this.helptext = helptext;
    }

    public int getParameters() {
        return parameters;
    }

    public void setParameters(int parameters) {
        this.parameters = parameters;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getUnformattedOutput() {
        return unformattedOutput;
    }

    public void setUnformattedOutput(String unformattedOutput) {
        this.unformattedOutput = unformattedOutput;
    }

    public String getCommandScript() {
        return commandScript;
    }

    public void setCommandScript(String commandScript) {
        this.commandScript = commandScript;
    }

    public String getQuotePrefix() {
        return quotePrefix;
    }

    public void setQuotePrefix(String quotePrefix) {
        this.quotePrefix = quotePrefix;
    }

    public String getQuoteSuffix() {
        return quoteSuffix;
    }

    public void setQuoteSuffix(String quoteSuffix) {
        this.quoteSuffix = quoteSuffix;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public ArrayList<String> getListContent() {
        return listContent;
    }

    public void setListContent(ArrayList<String> listContent) {
        this.listContent = listContent;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isTextTrigger() {
        return isTextTrigger;
    }

    public void setTextTrigger(boolean textTrigger) {
        isTextTrigger = textTrigger;
    }

    public int getNeededCommandPower() {
        return neededCommandPower;
    }

    public void setNeededCommandPower(int neededCommandPower) {
        this.neededCommandPower = neededCommandPower;
    }

    public int getUserCooldownLength() {
        return userCooldownLength;
    }

    public void setUserCooldownLength(int userCooldownLength) {
        this.userCooldownLength = userCooldownLength;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getNeededCooldownBypassPower() {
        return neededCooldownBypassPower;
    }

    public void setNeededCooldownBypassPower(int neededCooldownBypassPower) {
        this.neededCooldownBypassPower = neededCooldownBypassPower;
    }

    public boolean isAllowPicksFromList() {
        return allowPicksFromList;
    }

    public void setAllowPicksFromList(boolean allowPicksFromList) {
        this.allowPicksFromList = allowPicksFromList;
    }

    public boolean isOverrideHandleMessage() {
        return overrideHandleMessage;
    }

    public void setOverrideHandleMessage(boolean overrideHandleMessage) {
        this.overrideHandleMessage = overrideHandleMessage;
    }

    public int getExecCounter() {
        return execCounter;
    }

    public void setExecCounter(int execCounter) {
        this.execCounter = execCounter;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isFormatData() {
        return formatData;
    }

    public void setFormatData(boolean formatData) {
        this.formatData = formatData;
    }

    public boolean isCheckDefaultCooldown() {
        return checkDefaultCooldown;
    }

    public void setCheckDefaultCooldown(boolean checkDefaultCooldown) {
        this.checkDefaultCooldown = checkDefaultCooldown;
    }

    public HashMap<String, String> getOtherData() {
        return otherData;
    }

    public void setOtherData(HashMap<String, String> otherData) {
        this.otherData = otherData;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setWhisper(boolean whisper) {
        this.whisper = whisper;
    }

    public boolean isWhisper() {

        return whisper;
    }
}
