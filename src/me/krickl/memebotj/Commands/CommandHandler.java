package me.krickl.memebotj.Commands;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;
import me.krickl.memebotj.Utility.Cooldown;
import org.bson.Document;

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
public class CommandHandler implements CommandInterface {
    public static Logger log = Logger.getLogger(CommandHandler.class.getName());

    private ChannelHandler channelHandler = null;
    private String commandName = null;
    private MongoCollection<Document> commandCollection;

    private int cooldownLength = 0;
    private Cooldown cooldown = new Cooldown(cooldownLength);
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

    public boolean checkPermissions(UserHandler senderObject, int reqPermLevel, int secondPerm) {
        String senderName = senderObject.getUsername();

        Iterator it = Memebot.botAdmins.iterator();
        while(it.hasNext()) {
            String user = it.next().toString();
            if(senderName.equals(user)) {
                return true;
            }
        }

        if(!channelHandler.getUserList().containsKey(senderName) && !senderName.equals("#readonly#")) {
            return false;
        }

        if(reqPermLevel <= channelHandler.getUserList().get(senderName).getCommandPower() && secondPerm <= senderObject.getCommandPower()) {
            return true;
        }

        if (senderName.equals("#readonly#")) {
            if (reqPermLevel <= 10) {
                return true;
            }
        }

        return false;
    }

    public CommandHandler(ChannelHandler channelHandler, String commandName, String dbprefix) {
        this.channelHandler = channelHandler;
        this.commandName = commandName;

        if (Memebot.useMongo) {
            if (dbprefix == null) {
                this.commandCollection = Memebot.db.getCollection(this.channelHandler.getChannel() + "_commands");
            } else {
                this.commandCollection = Memebot.db.getCollection(dbprefix + this.channelHandler.getChannel() + "_commands");
            }
        }

        initCommand();
        readDB();
        overrideDB();
    }

    public void readDB() {
        if(!Memebot.useMongo) {
            return;
        }

        log.info("Reading db for command " + commandName + " from db on channel " + channelHandler.getChannel());

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
            //other data are used to store data that are used for internal commands
            Document otherDataDocument = (Document) commandObject.getOrDefault("otherdata", new Document());

            for(String key : otherDataDocument.keySet()) {
                this.otherData.put(key, otherDataDocument.getString(key));
            }

            cooldown = new Cooldown(cooldownLength);
        }
    }

    public void writeDB() {
        if(!Memebot.useMongo) { return; }
        log.info("Saving db entry for command " + commandName + " from db on channel " + channelHandler.getChannel());

        Document commandQuery = new Document("_id", this.commandName);
        FindIterable cursor = this.commandCollection.find(commandQuery);

        Document commandObject = (Document)cursor.first();

        Document otherDataDocument = new Document();
        for(String key : this.otherData.keySet()) {
            otherDataDocument.append(key, this.otherData.get(key));
        }

        Document channelData = new Document("_id", this.commandName)
                .append("command", this.commandName)
                .append("cooldown", new Integer(this.cooldownLength))
                .append("helptext", this.helptext)
                .append("param", new Integer(this.parameters))
                .append("cmdtype", this.commandType)
                .append("output", this.unformattedOutput)
                .append("qsuffix", this.quoteSuffix)
                .append("qprefix", this.quotePrefix)
                .append("costf", this.cost)
                .append("counter", this.counter)
                .append("listcontent", this.listContent)
                .append("locked", this.locked)
                .append("texttrigger", this.isTextTrigger)
                .append("viewerpower", this.neededCommandPower)
                .append("usercooldown", this.userCooldownLength)
                .append("script", this.commandScript)
                .append("enable", this.isEnabled)
                .append("cooldownbypasspower", this.neededCooldownBypassPower)
                .append("allowpick", this.allowPicksFromList)
                .append("overridehandlemessage", this.overrideHandleMessage)
                .append("execcounter", this.execCounter)
                .append("case", this.caseSensitive)
                .append("otherdata", otherDataDocument)
                .append("format", this.formatData)
                .append("checkdefaultcooldown", this.checkDefaultCooldown);

        try {
            if (this.commandCollection.findOneAndReplace(commandQuery, channelData) == null) {
                this.commandCollection.insertOne(channelData);
            }
        } catch(Exception e) {
            log.warning(e.toString());
        }
    }

    public void removeDB() {
        if(!Memebot.useMongo) {
            return;
        }
        try {
            log.info("Removing db entry for command " + commandName + " from db on channel " + channelHandler.getChannel());

            Document commandQuery = new Document("_id", this.commandName);
            FindIterable cursor = this.commandCollection.find(commandQuery);

            Document commandObject = (Document)cursor.first();
            if(commandObject != null) {
                this.commandCollection.deleteOne(commandObject);
            }
        } catch(java.lang.IllegalArgumentException e) {
            log.warning(e.toString());
        }
    }

    public void overrideDB() {

    }

    public void initCommand() {

    }

    public boolean handleCooldown(UserHandler sender) {
        // check global cooldown
        if ((!this.cooldown.canContinue() || !sender.getUserCooldown().canContinue()) && !checkPermissions(sender, this.neededCooldownBypassPower, 0)) {
            return true;
        }

        // check user cooldown
        if (!sender.getUserCommandCooldowns().containsKey(this.commandName)) {
            sender.getUserCommandCooldowns().put(this.commandName, new Cooldown(this.userCooldownLength));
        } else {
            if (sender.getUserCommandCooldowns().get(this.commandName).getCooldownLength() != this.userCooldownLength) {
                sender.getUserCommandCooldowns().get(this.commandName).setCooldownLength(this.userCooldownLength);
            }
        }

        if(!sender.getUserCommandCooldowns().get(this.commandName).canContinue() && !checkPermissions(sender, this.neededCooldownBypassPower, 0)) {
            return true;
        }

        return false;
    }

    public boolean startCooldown(UserHandler sender) {
        if (this.success) {
            this.cooldown.startCooldown();
            sender.getUserCooldown().startCooldown();
            if(!sender.getUserCommandCooldowns().containsKey(this.commandName)) {
                sender.getUserCommandCooldowns().put(this.commandName, new Cooldown(this.userCooldownLength));
            }
            sender.getUserCommandCooldowns().get(this.commandName).startCooldown();
            sender.setPoints(sender.getPoints() - this.cost);

            return true;
        }
        return false;
    }

    public boolean checkCost(UserHandler sender, double cost) {
        if (sender.getPoints() >= cost || checkPermissions(sender, CommandPower.adminAbsolute, 0)) {
            return true;
        }

        if (cost <= 0) {
            return true;
        }

        return false;
    }

    public boolean executeCommand(UserHandler sender, String[] data) {
        this.success = false;

        if(formatData) {
            for (int i = 0; i < data.length; i++) {
                data[i] = Memebot.formatText(data[i], channelHandler, sender, this, false, new String[]{}, "");
            }
        }

        if(this.overrideHandleMessage || !this.isEnabled) {
            return false;
        }
        if(this.checkDefaultCooldown && this.handleCooldown(sender)) {
            return false;
        }
        if(!this.checkPermissions(sender, this.neededCommandPower, 0)) {
            return false;
        }
        if (!this.checkCost(sender, this.cost)) {
            channelHandler.sendMessage(Memebot.formatText("POINTS_NOT_ENOUGH", channelHandler, sender, this,
                    true, new String[]{String.format("%f", (float)this.cost)}, ""), this.channelHandler.getChannel(), sender);
            return false;
        }

        String formattedOutput = unformattedOutput;
        int counterStart = 1;

        // todo list needs clear and import command
        if(commandType.equals("list")) {
            if (data[1].equals("add") && checkPermissions(sender, CommandPower.modAbsolute, neededCommandPower)) {
                String newEntry = "";
                for(int i = 2; i < data.length; i++) {
                    newEntry = newEntry + data[i];
                }
                if(newEntry.isEmpty()) {
                    formattedOutput = Memebot.formatText("NOT_ADDED", channelHandler, sender, this, true, new String[]{}, "");
                    this.success = false;
                } else {
                    this.listContent.add(newEntry + " " + Memebot.formatText(this.commandScript, channelHandler, sender, this, false, new String[]{}, ""));
                    formattedOutput = Memebot.formatText("ADDED", channelHandler, sender, this, true, new String[]{}, "");
                    this.success = false;
                }
            } else if(data[1].equals("remove") && checkPermissions(sender, CommandPower.modAbsolute, neededCommandPower)) {
                try {
                    this.listContent.remove(Integer.parseInt(data[2]));
                    formattedOutput = Memebot.formatText("REMOVED", channelHandler, sender, this, true, new String[]{}, "");
                    this.success = true;
                } catch(ArrayIndexOutOfBoundsException e) {
                    formattedOutput = e.toString();
                    this.success = false;
                }
            } else if(data[1].equals("edit") && checkPermissions(sender, CommandPower.modAbsolute, neededCommandPower)) {
                String newEntry = "";
                for(int i = 3; i < data.length; i++) {
                    newEntry = newEntry + data[i];
                }
                try {
                    this.listContent.set(Integer.parseInt(data[2]), newEntry);
                    formattedOutput = Memebot.formatText("EDITED", channelHandler, sender, this, true, new String[]{}, "");
                    this.success = true;
                } catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    log.warning(e.toString());
                    this.success = false;
                    formattedOutput = e.toString();
                }
            } else if(data[1].equals("list")) {
                try {
                    formattedOutput = Memebot.formatText("LIST", channelHandler, sender, this, true, new String[]{channelHandler.getChannelPageBaseURL() + "/" + URLEncoder.encode(this.commandName, "UTF-8") + ".html"}, "");
                    success = true;
                } catch(Exception e) {
                    log.warning(e.toString());
                    formattedOutput = e.toString();
                    success = false;
                }
            } else if(allowPicksFromList) {
                try {
                    formattedOutput = this.quotePrefix.replace("{number}", data[1]) + this.listContent.get(Integer.parseInt(data[1])) + this.quoteSuffix.replace("{number}", data[1]);
                } catch(NumberFormatException e) {
                    // find string in list content
                    formattedOutput = Memebot.formatText("QUERY_NOT_FOUND", channelHandler, sender, this, true, new String[]{}, "");
                    String query = java.util.Arrays.copyOfRange(data, 1, data.length).toString();
                    int number = 0;
                    for (String str : listContent) {
                        if (str.contains(query)) {
                            formattedOutput = this.quotePrefix.replace("{number}", Integer.toString(number)) + str + this.quoteSuffix.replace("{number}", Integer.toString(number));
                            number += 1;
                        }
                    }
                } catch(ArrayIndexOutOfBoundsException e) {
                    formattedOutput = Memebot.formatText("OOB", channelHandler, sender, this, true, new String[]{"${Integer.toString(this.listContent.size())}"}, "");
                }
            } else {
                try {
                    Random rand = new Random();
                    int i = rand.nextInt(this.listContent.size());
                    formattedOutput = this.quotePrefix.replace("{number}", Integer.toString(i)) + this.listContent.get(i) + this.quoteSuffix.replace("{number}", Integer.toString(i));
                } catch(IllegalArgumentException e) {
                    log.warning(e.toString());
                } finally {
                    // just ignore it
                }
            }
        } else if(commandType.equals("counter")) {
            int modifier = 1;
            try {
                modifier = Integer.parseInt(data[2]);
            } catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
                log.warning(e.toString());
            }

            try {
                if (data[1].equals("add")
                        && checkPermissions(sender, CommandPower.modAbsolute, this.neededCommandPower)) {
                    counter = counter + modifier;
                } else if (data[1].equals("sub")
                        && checkPermissions(sender, CommandPower.modAbsolute, this.neededCommandPower)) {
                    counter = counter - modifier;
                } else if (data[1].equals("set")
                        && checkPermissions(sender, CommandPower.modAbsolute, this.neededCommandPower)) {
                    counter = modifier;
                }
            } catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
                log.warning(e.toString());
            }
        }

        //format parameters
        if (counterStart < this.parameters + 1) {
            formattedOutput = Memebot.formatText(formattedOutput, channelHandler, sender, this, false, java.util.Arrays.copyOfRange(data, counterStart, this.parameters + 1), this.helptext);
        }
        formattedOutput = Memebot.formatText(formattedOutput, channelHandler, sender, this, false, new String[]{}, "");
        commandScript = Memebot.formatText(commandScript, channelHandler, sender, this, false, new String[]{}, "");

        if(!formattedOutput.equals("null")) {
            channelHandler.sendMessage(formattedOutput, this.channelHandler.getChannel(), sender);
            channelHandler.sendMessage(commandScript, this.channelHandler.getChannel(), sender);
        }

        this.commandScript(sender, data);

        // write changes to db
        if (!sender.getUsername().equals("#readonly#")) {
            this.writeDB();
        }

        this.execCounter = this.execCounter + 1;

        return true;
    }

    public void commandScript(UserHandler sender, String[] data) {

    }

    public void update() {
        if (this.commandType.equals("timer") && channelHandler.isLive()) {
            String[] newArray = new String[0];
            this.executeCommand(new UserHandler("#internal#", this.channelHandler.getChannel()), newArray);
        }
    }

    public boolean editCommand(String modType, String newValue, UserHandler sender) {
        if (!checkPermissions(sender, CommandPower.modAbsolute, this.neededCommandPower)) {
            return false;
        }

        HashMap<String, UserHandler> userList = channelHandler.getUserList();

        success = false;
        try {
            if (modType.equals("name")) {
                this.removeDB();
                this.setCommandName(newValue);
                this.writeDB();
                success = true;
            } else if (modType.equals("param")) {
                this.setParameters(Integer.parseInt(newValue));
                success = true;
            } else if (modType.equals("helptext")) {
                this.setHelptext(newValue);
                success = true;
            } else if (modType.equals("output")) {
                this.setUnformattedOutput(newValue);
                success = true;
            } else if (modType.equals("cooldown")) {
                this.setCooldownLength(Integer.parseInt(newValue));
                this.setCooldown(new Cooldown(Integer.parseInt(newValue)));
                success = true;
            } else if (modType.equals("cmdtype")) {
                this.setCommandType(newValue);
                success = true;
            } else if (modType.equals("qsuffix")) {
                this.setQuoteSuffix(newValue);
                success = true;
            } else if (modType.equals("qprefix")) {
                this.setQuotePrefix(newValue);
                success = true;
            } else if (modType.equals("cost")) {
                this.setCost(Double.parseDouble(newValue));
                success = true;
            } else if (modType.equals("lock") && checkPermissions(sender, CommandPower.broadcasterAbsolute, this.neededCommandPower)) {
                this.setLocked(Boolean.parseBoolean(newValue));
                success = true;
            } else if (modType.equals("texttrigger")) {
                this.setTextTrigger(Boolean.parseBoolean(newValue));
                success = true;
            } else if (modType.equals("access")) {
                this.setNeededCommandPower(Integer.parseInt(newValue));
                success = true;
            } else if (modType.equals("usercooldown")) {
                this.setUserCooldownLength(Integer.parseInt(newValue));
                success = true;
            } else if (modType.equals("script")) {
                this.setCommandScript(newValue);
                success = true;
            } else if (modType.equals("enable")) {
                this.setEnabled(Boolean.parseBoolean(newValue));
                success = true;
            } else if (modType.equals("allowpick")) {
                setAllowPicksFromList(Boolean.parseBoolean(newValue));
                success = true;
            } else if (modType.equals("cooldownbypasspower")) {
                this.setNeededCooldownBypassPower(Integer.parseInt(newValue));
                success = true;
            } else if (modType.equals("case")) {
                this.setCaseSensitive(Boolean.parseBoolean(newValue));
                success = true;
            } else if (modType.equals("format")) {
                this.setFormatData(Boolean.parseBoolean(newValue));
                success = false;
            }
        } catch(NumberFormatException e) {
            log.warning(String.format("Screw you Luigitus: %s", e.toString()));
        }
        this.writeDB();

        return success;
    }

    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        CommandHandler.log = log;
    }

    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    public void setChannelHandler(ChannelHandler channelHandler) {
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

    public Cooldown getCooldown() {
        return cooldown;
    }

    public void setCooldown(Cooldown cooldown) {
        this.cooldown = cooldown;
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
}
