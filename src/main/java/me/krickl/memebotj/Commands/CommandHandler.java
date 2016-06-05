package me.krickl.memebotj.Commands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Database.DatabaseObjectInterface;
import me.krickl.memebotj.Database.MongoHandler;
import me.krickl.memebotj.Exceptions.DatabaseReadException;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;
import me.krickl.memebotj.Utility.Cooldown;
import org.bson.Document;
import org.json.simple.JSONObject;

import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;

/**
 * This file is part of memebotj.
 * Created by unlink on 03/04/16.
 */
public class CommandHandler implements CommandInterface, Comparable<CommandHandler>,
        DatabaseObjectInterface {
    public static Logger log = Logger.getLogger(CommandHandler.class.getName());
    protected MongoHandler mongoHandler = null;
    protected boolean canBeEdited = true;
    protected String commandName = null;
    private ChannelHandler channelHandler = null;
    //private MongoCollection<Document> commandCollection;
    private int cooldownLength = 0;
    private Cooldown cooldown = new Cooldown(cooldownLength);
    private Cooldown removeCooldown = new Cooldown(300);
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
    private boolean caseSensitive = true;
    private boolean formatData = false;

    //private HashMap<String, String> otherData = new HashMap<>();
    private boolean checkDefaultCooldown = true;
    private boolean success = false;
    private boolean whisper = false;
    private boolean hideCommand = false;
    private int state = 0;
    private ArrayList<String> suggestedList = new ArrayList<>();
    private String lastOutput = "";
    private int uses = 0;
    private boolean startCooldown = true;
    private int cooldownOffsetPerViewer = 1; // cooldown increase per viewer - defaults to 1% of user cooldown

    private int listLimit = 10000;

    private boolean pointsUpdateDone = false;

    public CommandHandler(ChannelHandler channelHandler, String commandName, String dbprefix) {
        this.channelHandler = channelHandler;
        this.commandName = commandName;
        removeCooldown.startCooldown();

        if (Memebot.useMongo) {
            if (!Memebot.channelsPrivate.contains(this.getChannelHandler().getChannel())) {
                if (dbprefix == null) {
                    // todo old db code - remove soon
                    //this.commandCollection = Memebot.db.getCollection(this.channelHandler.getChannel() + "_commands");
                    mongoHandler = new MongoHandler(Memebot.db, this.channelHandler.getChannel() + "_commands");
                } else {
                    // todo old db code - remove soon
                    //this.commandCollection = Memebot.db.getCollection(dbprefix + this.channelHandler.getChannel() + "_commands");
                    mongoHandler = new MongoHandler(Memebot.db, dbprefix + this.channelHandler.getChannel() + "_commands");
                }
            } else {
                if (dbprefix == null) {
                    // todo old db code - remove soon
                    //this.commandCollection = Memebot.dbPrivate.getCollection(this.channelHandler.getChannel() + "_commands");
                    mongoHandler = new MongoHandler(Memebot.dbPrivate, this.channelHandler.getChannel() + "_commands");
                } else {
                    // todo old db code - remove soon
                    //this.commandCollection = Memebot.dbPrivate.getCollection(dbprefix + this.channelHandler.getChannel() + "_commands");
                    mongoHandler = new MongoHandler(Memebot.dbPrivate, dbprefix + this.channelHandler.getChannel() + "_commands");
                }
            }
        }

        initCommand();
        readDB();
        overrideDB();

        if(!pointsUpdateDone) {
            cost = cost / 10;
            pointsUpdateDone = true;
        }
    }

    public static Logger getLog() {
        return log;
    }

    public static void setLog(Logger log) {
        CommandHandler.log = log;
    }

    public boolean checkPermissions(UserHandler senderObject, int reqPermLevel, int secondPerm) {
        String senderName = senderObject.getUsername();

        Iterator it = Memebot.botAdmins.iterator();
        while (it.hasNext()) {
            String user = it.next().toString();
            if (senderName.equals(user)) {
                return true;
            }
        }

        if (!channelHandler.getUserList().containsKey(senderName) && !senderName.equals("#readonly#")) {
            return false;
        }

        if (reqPermLevel <= channelHandler.getUserList().get(senderName).getCommandPower() && secondPerm <= senderObject.getCommandPower()) {
            return true;
        }

        if (senderName.equals("#readonly#")) {
            if (reqPermLevel <= 10) {
                return true;
            }
        }

        return false;
    }

    public void readDB() {
        if (!Memebot.useMongo) {
            return;
        }

        try {
            mongoHandler.readDatabase(this.commandName);
        } catch (DatabaseReadException e) {
            return;
        }

        this.commandName = mongoHandler.getObject("command", this.commandName).toString();
        this.cooldownLength = (int) mongoHandler.getObject("cooldown", this.cooldownLength);
        this.helptext = mongoHandler.getObject("helptext", this.helptext).toString();
        this.parameters = (int) mongoHandler.getObject("param", this.parameters);
        this.commandType = mongoHandler.getObject("cmdtype", this.commandType).toString();
        this.unformattedOutput = mongoHandler.getObject("output", this.unformattedOutput).toString();
        this.quoteSuffix = mongoHandler.getObject("qsuffix", this.quoteSuffix).toString();
        this.quotePrefix = mongoHandler.getObject("qprefix", this.quotePrefix).toString();
        this.cost = (double) mongoHandler.getObject("costf", this.cost);
        this.counter = (int) mongoHandler.getObject("counter", this.counter);
        this.listContent = (ArrayList<String>) mongoHandler.getObject("listcontent", this.listContent);
        this.locked = (boolean) mongoHandler.getObject("locked", this.locked);
        this.isTextTrigger = (boolean) mongoHandler.getObject("texttrigger", this.isTextTrigger);
        this.neededCommandPower = (int) mongoHandler.getObject("viewerpower", this.neededCommandPower);
        this.userCooldownLength = (int) mongoHandler.getObject("usercooldown", this.userCooldownLength);
        this.commandScript = mongoHandler.getObject("script", this.commandScript).toString();
        this.isEnabled = (boolean) mongoHandler.getObject("enable", this.isEnabled);
        this.neededCooldownBypassPower = (int) mongoHandler.getObject("cooldownbypass", this.neededCooldownBypassPower);
        this.allowPicksFromList = (boolean) mongoHandler.getObject("allowpick", this.allowPicksFromList);
        this.overrideHandleMessage = (boolean) mongoHandler.getObject("overridehandlemessage", this.overrideHandleMessage);
        this.execCounter = (int) mongoHandler.getObject("execcounter", this.execCounter);
        this.caseSensitive = (boolean) mongoHandler.getObject("case", this.caseSensitive);
        this.formatData = (boolean) mongoHandler.getObject("format", this.formatData);
        this.checkDefaultCooldown = (boolean) mongoHandler.getObject("checkdefaultcooldown", this.checkDefaultCooldown);
        this.hideCommand = (boolean) mongoHandler.getObject("hideCommand", this.hideCommand);
        this.lastOutput = mongoHandler.getObject("lasoutput", this.lastOutput).toString();
        this.state = (int) mongoHandler.getObject("state", this.state);
        this.suggestedList = (ArrayList<String>) mongoHandler.getObject("suggestedList", suggestedList);
        uses = (int) mongoHandler.getObject("uses", uses);
        cooldownOffsetPerViewer = (int)mongoHandler.getObject("usercdoffset", cooldownOffsetPerViewer);

        this.pointsUpdateDone = (boolean) mongoHandler.getObject("pointsupdate", this.pointsUpdateDone);

        // todo reload cooldown from document - set default data
        //cooldown = new Cooldown((Document)mongoHandler.getObject("cooldowndoc", new Document()));

        cooldown = new Cooldown(cooldownLength, uses);
    }

    public void setDB() {
        /*Document otherDataDocument = new Document();
        for(String key : this.otherData.keySet()) {
            otherDataDocument.append(key, this.otherData.get(key));
        }*/

        mongoHandler.updateDocument("_id", this.commandName);
        mongoHandler.updateDocument("command", this.commandName);
        mongoHandler.updateDocument("cooldown", new Integer(this.cooldownLength));
        mongoHandler.updateDocument("helptext", this.helptext);
        mongoHandler.updateDocument("param", new Integer(this.parameters));
        mongoHandler.updateDocument("cmdtype", this.commandType);
        mongoHandler.updateDocument("output", this.unformattedOutput);
        mongoHandler.updateDocument("qsuffix", this.quoteSuffix);
        mongoHandler.updateDocument("qprefix", this.quotePrefix);
        mongoHandler.updateDocument("costf", this.cost);
        mongoHandler.updateDocument("counter", this.counter);
        mongoHandler.updateDocument("listcontent", this.listContent);
        mongoHandler.updateDocument("locked", this.locked);
        mongoHandler.updateDocument("texttrigger", this.isTextTrigger);
        mongoHandler.updateDocument("viewerpower", this.neededCommandPower);
        mongoHandler.updateDocument("usercooldown", this.userCooldownLength);
        mongoHandler.updateDocument("script", this.commandScript);
        mongoHandler.updateDocument("enable", this.isEnabled);
        mongoHandler.updateDocument("cooldownbypasspower", this.neededCooldownBypassPower);
        mongoHandler.updateDocument("allowpick", this.allowPicksFromList);
        mongoHandler.updateDocument("overridehandlemessage", this.overrideHandleMessage);
        mongoHandler.updateDocument("execcounter", this.execCounter);
        mongoHandler.updateDocument("case", this.caseSensitive);
        //mongoHandler.updateDocument("otherdata", otherDataDocument);
        mongoHandler.updateDocument("format", this.formatData);
        mongoHandler.updateDocument("checkdefaultcooldown", this.checkDefaultCooldown);
        mongoHandler.updateDocument("hideCommand", this.hideCommand);
        mongoHandler.updateDocument("lastoutput", this.lastOutput);
        mongoHandler.updateDocument("state", this.state);
        mongoHandler.updateDocument("uses", this.uses);
        mongoHandler.updateDocument("suggestedList", suggestedList);
        mongoHandler.updateDocument("cooldowndoc", cooldown.getDoc());
        mongoHandler.updateDocument("usercdoffset", cooldownOffsetPerViewer);
        mongoHandler.updateDocument("pointsupdate", this.pointsUpdateDone);

        //mongoHandler.setDocument(channelData);
    }

    public void writeDB() {
        if (!Memebot.useMongo) {
            return;
        }

        setDB();

        mongoHandler.writeDatabase(this.commandName);
    }

    public void removeDB() {
        if (!Memebot.useMongo) {
            return;
        }

        mongoHandler.removeDatabase(this.commandName);
    }

    public void overrideDB() {

    }

    public void initCommand() {

    }

    public boolean handleCooldown(UserHandler sender) {
        if(!this.startCooldown) {
            return false;
        }
        // check global cooldown
        if ((!this.cooldown.canContinue() || !sender.getUserCooldown().canContinue())
                && !checkPermissions(sender, this.neededCooldownBypassPower, this.neededCooldownBypassPower)) {
            if (Memebot.debug) {
                channelHandler.sendMessage("Cooldown true");
            }
            return true;
        }

        // check user cooldown
        if (!sender.getUserCommandCooldowns().containsKey(this.commandName)) {
            sender.getUserCommandCooldowns().put(this.commandName, new Cooldown(this.userCooldownLength, this.uses));
        } else {
            if (sender.getUserCommandCooldowns().get(this.commandName).getCooldownLength() != this.userCooldownLength) {
                sender.getUserCommandCooldowns().get(this.commandName).setCooldownLength(this.userCooldownLength);
            }
        }
        System.out.println(sender.getUserCommandCooldowns().get(this.commandName).canContinue());

        if (!sender.getUserCommandCooldowns().get(this.commandName).canContinue()
                && !checkPermissions(sender, this.neededCooldownBypassPower, this.neededCooldownBypassPower)) {
            if (Memebot.debug) {
                channelHandler.sendMessage("Cooldown true " + this.toString());
            }
            return true;
        }

        return false;
    }

    public boolean startCooldown(UserHandler sender) {
        if (this.success && this.startCooldown) {
            this.cooldown.startCooldown();
            sender.getUserCooldown().startCooldown();
            if (!sender.getUserCommandCooldowns().containsKey(this.commandName)) {
                sender.getUserCommandCooldowns().put(this.commandName, new Cooldown(this.userCooldownLength, uses));
            }
            sender.getUserCommandCooldowns().get(this.commandName).startCooldown(channelHandler.getViewerNumber() *
                    (userCooldownLength /
                    100 * cooldownOffsetPerViewer));
            sender.setPoints(sender.getPoints() - this.cost);

            if (Memebot.debug) {
                channelHandler.sendMessage("Cooldown started " + this.toString());
            }

            return true;
        }
        return false;
    }

    public boolean checkCost(UserHandler sender, double cost) {
        if (sender.getPoints() >= cost || checkPermissions(sender, CommandPower.adminAbsolute, 0)) {
            if (Memebot.debug) {
                channelHandler.sendMessage("Cost true " + this.toString());
            }
            return true;
        }

        if (cost <= 0) {
            if (Memebot.debug) {
                channelHandler.sendMessage("Cost true " + this.toString());
            }
            return true;
        }

        if (Memebot.debug) {
            channelHandler.sendMessage("Cost false " + this.toString());
        }

        return false;
    }

    public String[] formatData(UserHandler sender, String[] data) {
        if (formatData) {
            for (int i = 0; i < data.length; i++) {
                data[i] = Memebot.formatText(data[i], channelHandler, sender, this, false, new String[]{}, "");
            }
        }

        return data;
    }

    /***
     * This method handles list command specific actions
     * @param sender
     * @param data
     * @param formattedOutput
     * @return
     */
    private String handleListCommand(UserHandler sender, String data[], String formattedOutput) {
        try {
            if (data[1].equals("add") && checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {
                if(listContent.size() >= listLimit) {
                    formattedOutput = Memebot.formatText("LIMIT_ERROR", channelHandler, sender, this, true, new String[]{}, "");
                    this.success = false;
                } else {
                    String newEntry = "";
                    for (int i = 2; i < data.length; i++) {
                        newEntry = newEntry + data[i] + " ";
                    }
                    if (newEntry.isEmpty()) {
                        formattedOutput = Memebot.formatText("NOT_ADDED", channelHandler, sender, this, true, new String[]{}, "");
                        this.success = false;
                    } else {
                        this.listContent.add(newEntry + " " + Memebot.formatText(this.commandScript,
                                channelHandler, sender, this, false, new String[]{}, ""));

                        formattedOutput = Memebot.formatText("ADDED", channelHandler, sender,
                                this, true, new String[]{this.listContent.get(listContent.size() - 1)}, "");
                        this.success = false;
                    }
                    this.startCooldown = false;
                }
            } else if (data[1].equals("suggest")) {
                if(suggestedList.size() >= listLimit) {
                    formattedOutput = Memebot.formatText("LIMIT_ERROR", channelHandler, sender, this, true, new String[]{}, "");
                    this.success = false;
                } else {
                    // allow users to suggest quotes
                    String newEntry = "";
                    for (int i = 2; i < data.length; i++) {
                        newEntry = newEntry + data[i] + " ";
                    }
                    if (newEntry.isEmpty()) {
                        formattedOutput = Memebot.formatText("NOT_ADDED", channelHandler, sender, this, true, new String[]{}, "");
                        this.success = false;
                    } else {
                        String newEntryFormatted = newEntry + " " +
                                Memebot.formatText(this.commandScript, channelHandler, sender, this, false, new String[]{}, "");
                        if (!this.suggestedList.contains(newEntryFormatted)) {
                            this.suggestedList.add(newEntryFormatted);
                        }
                        formattedOutput = Memebot.formatText("ADDED_SUGGESTED", channelHandler, sender, this, true,
                                new String[]{this.suggestedList.get(suggestedList.size() - 1)}, "");
                        this.success = false;
                    }
                }
            } else if (data[1].equals("replace") && checkPermissions(sender, CommandPower.broadcasterAbsolute,
                    CommandPower.broadcasterAbsolute)) {

                String fullargument = "";
                for (int i = 2; i < data.length; i++) {
                    fullargument = fullargument + data[i] + " ";
                }
                if (fullargument.split(";;").length >= 2) {
                    String oldString = fullargument.split(";;")[0];
                    String newString = fullargument.split(";;")[1];
                    for (int i = 0; i < listContent.size(); i++) {
                        listContent.get(i).replace(oldString, newString);
                    }
                    formattedOutput = Memebot.formatText("REPLACED", channelHandler, sender, this, true, new String[]{}, "");
                } else {
                    formattedOutput = Memebot.formatText("REPLACED_ERROR", channelHandler, sender, this, true, new String[]{}, "");
                }

                this.startCooldown = false;
            } else if (data[1].equals("approve") &&
                    checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {

                try {
                    int index = Integer.parseInt(data[2]) - 1;
                    if (this.suggestedList.size() > index) {
                        this.listContent.add(suggestedList.get(index));
                        suggestedList.remove(suggestedList.get(index));
                        formattedOutput = Memebot.formatText("APPROVED", channelHandler, sender, this, true,
                                new String[]{}, "");
                    } else {
                        formattedOutput = Memebot.formatText("OOB", channelHandler, sender, this, true,
                                new String[]{Integer.toString(this.suggestedList.size())}, "");
                    }
                } catch (NumberFormatException e) {
                    formattedOutput = e.toString();
                    this.success = false;
                }

                this.startCooldown = false;
            } else if (data[1].equals("deny") && checkPermissions(sender, CommandPower.modAbsolute,
                    CommandPower.modAbsolute)) {
                try {
                    int index = Integer.parseInt(data[2]) - 1;
                    if (!data[2].equals("all")) {
                        if (this.suggestedList.size() > index) {
                            suggestedList.remove(suggestedList.get(index));
                            formattedOutput = Memebot.formatText("DENIED", channelHandler, sender, this, true, new String[]{}, "");
                        } else {
                            formattedOutput = Memebot.formatText("OOB", channelHandler, sender, this, true,
                                    new String[]{Integer.toString(this.suggestedList.size())}, "");
                        }
                    } else {
                        suggestedList.clear();
                        formattedOutput = Memebot.formatText("DENIED_ALL", channelHandler, sender, this, true,
                                new String[]{}, "");
                    }
                } catch (NumberFormatException e) {
                    formattedOutput = e.toString();
                    this.success = false;
                }

                this.startCooldown = false;
            } else if (data[1].equals("remove") && checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {
                try {
                    int index = Integer.parseInt(data[2]) - 1;
                    if (this.listContent.size() > index) {
                        this.listContent.remove(index);
                        formattedOutput = Memebot.formatText("REMOVED", channelHandler, sender, this, true, new String[]{}, "");
                        this.success = true;
                    } else {
                        formattedOutput = Memebot.formatText("OOB", channelHandler, sender, this, true, new String[]{Integer.toString(this.listContent.size())}, "");
                    }
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    formattedOutput = e.toString();
                    this.success = false;
                }

                this.startCooldown = false;
            } else if (data[1].equals("edit") && checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {
                String newEntry = "";
                for (int i = 3; i < data.length; i++) {
                    newEntry = newEntry + data[i] + " ";
                }
                try {
                    int index = Integer.parseInt(data[2]) - 1;
                    if (this.listContent.size() > index) {
                        this.listContent.set(index, newEntry);
                        formattedOutput = Memebot.formatText("EDITED", channelHandler, sender, this, true, new String[]{}, "");
                        this.success = true;
                    } else {
                        formattedOutput = Memebot.formatText("OOB", channelHandler, sender, this, true, new String[]{Integer.toString(this.listContent.size())}, "");
                    }
                } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                    log.warning(e.toString());
                    this.success = false;
                    formattedOutput = e.toString();
                }

                this.startCooldown = false;
            } else if (data[1].equals("list")) {
                try {
                    formattedOutput = Memebot.formatText("LIST", channelHandler, sender, this, true, new String[]{channelHandler.getChannelPageBaseURL() + "/" + URLEncoder.encode(this.commandName, "UTF-8")}, "");
                    //success = true;
                } catch (Exception e) {
                    log.warning(e.toString());
                    formattedOutput = e.toString();
                    success = false;
                }

                this.startCooldown = false;
            } else if (allowPicksFromList) {
                try {
                    int index = Integer.parseInt(data[1]) - 1;
                    if (this.listContent.size() <= index) {
                        formattedOutput = Memebot.formatText("OOB", channelHandler,
                                sender, this, true, new String[]{Integer.toString(this.listContent.size())}, "");
                    } else {
                        formattedOutput = this.quotePrefix.replace("{number}", data[1]) + " " +
                                this.listContent.get(index) + " " + this.quoteSuffix.replace("{number}", data[1]);
                        success = true;
                    }
                } catch (NumberFormatException e) {
                    // find string in list content
                    formattedOutput = Memebot.formatText("QUERY_NOT_FOUND", channelHandler, sender, this, true, new String[]{}, "");
                    String[] queryA = java.util.Arrays.copyOfRange(data, 1, data.length);
                    String query = "";
                    for (String s : queryA) {
                        query = query + s + " ";
                    }
                    int number = 0;
                    // find all contents of list containing the required string
                    ArrayList<String> tempList = new ArrayList<>();
                    for (String str : listContent) {
                        if (str.contains(query)) {
                            formattedOutput = this.quotePrefix.replace("{number}", Integer.toString(number)) + str + this.quoteSuffix.replace("{number}", Integer.toString(number));
                            number += 1;
                            tempList.add(formattedOutput);
                        }
                    }
                    if (tempList.size() > 0) {
                        SecureRandom ran = new SecureRandom();
                        formattedOutput = tempList.get(ran.nextInt(tempList.size()));
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            try {
                Random rand = new Random();
                int i = rand.nextInt(this.listContent.size());
                formattedOutput = this.quotePrefix.replace("{number}", Integer.toString(i)) + " " + this.listContent.get(i) + " " + this.quoteSuffix.replace("{number}", Integer.toString(i));
                success = true;
            } catch (IllegalArgumentException e1) {
                log.warning(e1.toString());
            } finally {
                // just ignore it
            }
        }

        return formattedOutput;
    }

    /***
     * This method handles counter command specific actions
     * @param sender
     * @param data
     * @param formattedOutput
     * @return
     */
    private String handleCounterCommand(UserHandler sender, String[] data, String formattedOutput) {
        int modifier = 1;
        try {
            modifier = Integer.parseInt(data[2]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            log.warning(e.toString());
        }

        try {
            if (data[1].equals("add")
                    && checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {
                counter = counter + modifier;
                this.startCooldown = false;
            } else if (data[1].equals("sub")
                    && checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {
                counter = counter - modifier;
                this.startCooldown = false;
            } else if (data[1].equals("set")
                    && checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {
                counter = modifier;
                this.startCooldown = false;
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            log.warning(e.toString());
            this.success = false;
        }

        return formattedOutput;
    }

    public boolean executeCommand(UserHandler sender, String[] data) {
        this.success = false;
        this.startCooldown = true;
        removeCooldown.startCooldown();
        data = formatData(sender, data);

        if (this.overrideHandleMessage) {
            return false;
        }

        if (!this.checkPermissions(sender, this.neededCommandPower, this.neededCommandPower)) {
            return false;
        }
        if (!this.checkCost(sender, this.cost)) {
            channelHandler.sendMessage(Memebot.formatText("POINTS_NOT_ENOUGH", channelHandler, sender, this,
                    true, new String[]{String.format("%f", (float) this.cost)}, ""), this.channelHandler.getChannel(),
                    sender);
            return false;
        }

        if (!this.isEnabled) {
            return false;
        }

        String formattedOutput = unformattedOutput;
        int counterStart = 1;

        // todo list needs clear and import command
        if (commandType.equals("list")) {
            formattedOutput = handleListCommand(sender, data, formattedOutput);
        } else if (commandType.equals("counter")) {
            formattedOutput = handleCounterCommand(sender, data, formattedOutput);
        }

        // start cooldown both tells whether to check cooldown and wheter to start a cooldown
        // if start cooldown is false output should be sent regardless of cooldown
        if (this.checkDefaultCooldown && this.handleCooldown(sender)) {
            return false;
        }

        String formattedScript = "";
        //format parameters
        if (counterStart < this.parameters + 1) {
            formattedOutput = Memebot.formatText(formattedOutput, channelHandler, sender, this, false, java.util.Arrays.copyOfRange(data, counterStart, this.parameters + 1), this.helptext);
            formattedScript = Memebot.formatText(commandScript, channelHandler, sender, this, false, java.util.Arrays.copyOfRange(data, counterStart, this.parameters + 1), this.helptext);
        }
        formattedOutput = Memebot.formatText(formattedOutput, channelHandler, sender, this, false, new String[]{}, "");
        formattedScript = Memebot.formatText(formattedScript, channelHandler, sender, this, false, new String[]{}, "");

        if (!formattedOutput.equals("null")) {
            if (commandType.equals("counter") || commandType.equals("list") || commandType.equals("default") || commandType.equals("timer")) {
                channelHandler.sendMessage(formattedOutput, this.channelHandler.getChannel(), sender);
                success = true;
            }

            String channel = this.channelHandler.getChannel();

            if (commandType.equals("default")) {
                channelHandler.sendMessage(formattedScript, channel, sender);
                success = true;
            } else if (commandType.equals("state")) {
                if (state == 0) {
                    channelHandler.sendMessage(formattedOutput, channel, sender);
                    success = true;
                    state = 1;
                } else if (state == 1) {
                    channelHandler.sendMessage(formattedScript, channel, sender);
                    success = true;
                    state = 0;
                }
            }
        }

        this.commandScript(sender, data);

        // write changes to db
        if (!sender.getUsername().equals("#readonly#")) {
            this.writeDB();
        }

        this.execCounter = this.execCounter + 1;

        if (success) {
            this.startCooldown(sender);
        }

        lastOutput = formattedOutput;

        return true;
    }

    public void commandScript(UserHandler sender, String[] data) {

    }

    public void update() {
        if (this.commandType.equals("timer") && channelHandler.isLive()) {
            String[] newArray = new String[0];
            this.executeCommand(new UserHandler("#internal#", this.channelHandler.getChannel(), "#internal#"), newArray);
        }
    }

    public boolean editCommand(String modType, String newValue, UserHandler sender) {
        if (!checkPermissions(sender, this.neededCommandPower, this.neededCommandPower)) {
            return false;
        }

        if (!canBeEdited) {
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
                this.setCooldown(new Cooldown(Integer.parseInt(newValue), uses));
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
                if(checkPermissions(sender, Integer.parseInt(newValue), Integer.parseInt(newValue))) {
                    this.setNeededCommandPower(Integer.parseInt(newValue));
                    success = true;
                }
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
                if(checkPermissions(sender, Integer.parseInt(newValue), Integer.parseInt(newValue))) {
                    this.setNeededCooldownBypassPower(Integer.parseInt(newValue));
                    success = true;
                }
            } else if (modType.equals("case")) {
                this.setCaseSensitive(Boolean.parseBoolean(newValue));
                success = true;
            } else if (modType.equals("format")) {
                this.setFormatData(Boolean.parseBoolean(newValue));
                success = false;
            } else if (modType.equals("hidden")) {
                setHideCommand(Boolean.parseBoolean(newValue));
                success = true;
            } else if (modType.equals("uses")) {
                setUses(Integer.parseInt(newValue));
                success = true;
            } else if(modType.equals("cooldownoffset")) {
                setCooldownOffsetPerViewer(Integer.parseInt(newValue));
                success = true;
            }
        } catch (NumberFormatException e) {
            log.warning(String.format("Screw you Luigitus: %s", e.toString()));
        }
        this.writeDB();

        return success;
    }

    @Override
    public int compareTo(CommandHandler another) {
        return commandName.compareTo(another.getCommandName());
    }

    public String getLastOutput() {
        return lastOutput;
    }

    public void setLastOutput(String lastOutput) {
        this.lastOutput = lastOutput;
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

    public boolean isHideCommand() {
        return hideCommand;
    }

    public void setHideCommand(boolean hideCommand) {
        this.hideCommand = hideCommand;
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

    public int getUses() {
        return uses;
    }

    public void setUses(int uses) {
        this.uses = uses;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isWhisper() {

        return whisper;
    }

    public void setWhisper(boolean whisper) {
        this.whisper = whisper;
    }

    public MongoHandler getMongoHandler() {
        return mongoHandler;
    }

    public void setMongoHandler(MongoHandler mongoHandler) {
        this.mongoHandler = mongoHandler;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public ArrayList<String> getSuggestedList() {
        return suggestedList;
    }

    public void setSuggestedList(ArrayList<String> suggestedList) {
        this.suggestedList = suggestedList;
    }

    public boolean isCanBeEdited() {
        return canBeEdited;
    }

    public void setCanBeEdited(boolean canBeEdited) {
        this.canBeEdited = canBeEdited;
    }

    public Cooldown getRemoveCooldown() {
        return removeCooldown;
    }

    public void setRemoveCooldown(Cooldown removeCooldown) {
        this.removeCooldown = removeCooldown;
    }

    public int getCooldownOffsetPerViewer() {
        return cooldownOffsetPerViewer;
    }

    public void setCooldownOffsetPerViewer(int cooldownOffsetPerViewer) {
        this.cooldownOffsetPerViewer = cooldownOffsetPerViewer;
    }

    public JSONObject toJSONObject() {
        JSONObject wrapper = new JSONObject();
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("_id", commandName);
        jsonObject.put("_channel", channelHandler.getChannel());
        jsonObject.put("execcounter", execCounter);
        jsonObject.put("listcontent", listContent);
        jsonObject.put("suggestedlist", suggestedList);
        jsonObject.put("counter", counter);
        jsonObject.put("output", unformattedOutput);
        jsonObject.put("helptext", helptext);
        jsonObject.put("neededpower", neededCommandPower);
        jsonObject.put("qprefix", quotePrefix);
        jsonObject.put("qsuffix", quoteSuffix);

        wrapper.put("data", jsonObject);
        wrapper.put("links", Memebot.getLinks(Memebot.webBaseURL + "/api/commands/" + channelHandler.getBroadcaster() + "/" + commandName,
                Memebot.webBaseURL + "/api/channels/" + getChannelHandler().getBroadcaster(), null
                , null));

        return wrapper;
    }

    public String toJSONString() {
        return toJSONObject().toJSONString();
    }

    // this method returns true if the object can be removed from memeory
    public boolean canBeRemoved() {
        return removeCooldown.canContinue() && !commandType.equals("timer");
    }
}
