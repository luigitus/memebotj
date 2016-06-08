package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Commands.CommandRefernce;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class CommandManager extends CommandHandler {
    private int commandLimit = 1000;

    public CommandManager(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setNeededCommandPower(CommandPower.viewerAbsolute);

        this.setHelptext(Memebot.formatText("COMMANDMANAGER_SYNTAX", getChannelHandler(), null, this, true, new String[]{}, ""));
        this.setFormatData(false);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            if (data[0].equals("add") && checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {
                if(getChannelHandler().getChannelCommands().size() >= commandLimit) {
                    getChannelHandler().sendMessage(Memebot.formatText("ADD_COMMAND_ERROR", getChannelHandler(),
                            sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender);
                    return;
                }
                CommandRefernce newCommand = new CommandRefernce(getChannelHandler(), "null", null);
                if (getChannelHandler().findCommandReferneceForString(data[1], getChannelHandler().getChannelCommands()) == null) {
                    newCommand.editCommand("name", data[1], new UserHandler("#internal#", "#internal#"));
                    newCommand.editCommand("access", "viewers", new UserHandler("#internal#", "#internal#"));
                    String output = data[2];
                    if (!output.equals("{none}")) {
                        for (int i = 3; i < data.length; i++) {
                            output = output + " " + data[i];
                        }
                    }
                    newCommand.editCommand("output", output, new UserHandler("#internal#", "#internal#"));
                    getChannelHandler().sendMessage(Memebot.formatText("ADD_COMMAND", getChannelHandler(),
                            sender, this, true, new String[]{newCommand.getCommandName()}, ""),
                            this.getChannelHandler().getChannel(), sender);
                    getChannelHandler().getChannelCommands().add(newCommand);
                } else {
                    getChannelHandler().sendMessage(Memebot.formatText("COMMAND_EXISTS", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender);
                }
            } else if (data[0].equals("remove") && checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {
                CommandRefernce j = getChannelHandler().findCommandReferneceForString(data[1], getChannelHandler().getChannelCommands());
                if (j != null) {
                    if (!j.getCH().isLocked()) {
                        getChannelHandler().sendMessage(Memebot.formatText("DELCOM_OK", getChannelHandler(), sender, this, true, new String[]{j.getCommandName()}, ""), this.getChannelHandler().getChannel(), sender);

                        j.removeDB();
                        getChannelHandler().getChannelCommands().remove(j);
                    } else {
                        getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("DELCOM_LOCKED"), getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender);
                    }
                } else {
                    getChannelHandler().sendMessage(Memebot.formatText("DELCOM_NOT_FOUND", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender);
                }
            } else if (data[0].equals("edit") && checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {
                CommandRefernce j = getChannelHandler().findCommandReferneceForString(data[1], getChannelHandler().getChannelCommands());
                if (j != null) {
                    String newValue = data[3];
                    for (int x = 4; x < data.length; x++) {
                        newValue = newValue + " " + data[x];
                    }
                    if (j.editCommand(data[2], newValue, sender)) {
                        getChannelHandler().sendMessage(Memebot.formatText("EDITCOMMAND_OK", getChannelHandler(), sender, this, true, new String[]{data[1], data[2], newValue}, ""), this.getChannelHandler().getChannel(), sender);

                    } else {
                        getChannelHandler().sendMessage(Memebot.formatText("EDITCOMMAND_FAIL", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender);
                    }
                }
            } else if (data[0].equals("editinternal") && checkPermissions(sender, CommandPower.adminAbsolute, CommandPower.adminAbsolute)) {
                CommandHandler j = getChannelHandler().findCommandForString(data[1], getChannelHandler().getInternalCommands());
                if (j != null) {
                    String newValue = data[3];
                    for (int x = 4; x < data.length; x++) {
                        newValue = newValue + " " + data[x];
                    }
                    if (j.editCommand(data[2], newValue, sender)) {
                        getChannelHandler().sendMessage(Memebot.formatText("EDITCOMMAND_OK", getChannelHandler(), sender, this, true, new String[]{data[1], data[2], newValue}, ""), this.getChannelHandler().getChannel(), sender);

                    } else {
                        getChannelHandler().sendMessage(Memebot.formatText("EDITCOMMAND_FAIL", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender);
                    }
                }
            } else if (data[0].equals("toggleinternal") && checkPermissions(sender, CommandPower.broadcasterAbsolute, CommandPower.broadcasterAbsolute)) {
                CommandHandler ch = getChannelHandler().findCommandForString(data[1], getChannelHandler().getInternalCommands());
                if (ch != null) {
                    if (ch.getCommandName().equals(this.getCommandName())) {
                        getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("COMMAND_DISABLE_FAILED"), getChannelHandler(), sender, this, false, new String[]{sender.screenName()}, ""), this.getChannelHandler().getChannel(), sender);
                    } else {
                        ch.setEnabled(!ch.isEnabled());
                        getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("COMMAND_TOGGLE"), getChannelHandler(), sender, this, false, new String[]{sender.screenName(), data[1], Boolean.toString(ch.isEnabled())}, ""), this.getChannelHandler().getChannel(), sender);
                        ch.writeDB();
                    }
                }
            } else if (data[0].equals("removeinternal") && checkPermissions(sender, CommandPower.adminAbsolute, CommandPower.adminAbsolute)) {
                new CommandHandler(this.getChannelHandler(), data[1], "#internal#").removeDB();
            } else if (data[0].equals("info") && checkPermissions(sender, CommandPower.broadcasterAbsolute, CommandPower.broadcasterAbsolute)) {
                CommandRefernce j = getChannelHandler().findCommandReferneceForString(data[1], getChannelHandler().getChannelCommands());
                if (j != null) {
                    getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("COMMAND_TIMES_EXECUTED"),
                            getChannelHandler(), sender, this, false,
                            new String[]{Integer.toString(j.getCH().getExecCounter()), "", j.toString()}, ""),
                            this.getChannelHandler().getChannel(), sender);
                }
                CommandHandler o = getChannelHandler().findCommandForString(data[1], getChannelHandler().getInternalCommands());
                if (o != null) {
                    getChannelHandler().sendMessage(
                            Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("COMMAND_TIMES_EXECUTED"),
                                    getChannelHandler(), sender, this, false,
                                    new String[]{Integer.toString(o.getExecCounter()),
                                            "", o.toString()}, ""),
                            this.getChannelHandler().getChannel(), sender);
                }
            } else if (data[0].equals("list")) {
                if (Memebot.useWeb) {
                    getChannelHandler().sendMessage(Memebot.formatText("COMMAND_LIST", getChannelHandler(),
                            sender, this, true, new String[]{getChannelHandler().getChannelPageBaseURL()}, ""),
                            this.getChannelHandler().getChannel(), sender);
                } else {
                    int index = 0;
                    String output = "";
                    try {
                        index = java.lang.Integer.parseInt(data[1]);
                    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                        e.printStackTrace();
                    }
                    for (int i = index * 10; i < getChannelHandler().getChannelCommands().size(); i++) {
                        if (i > index * 10 + 10) {
                            break;
                        }
                        output = output + " || " + getChannelHandler().getChannelCommands().get(i).getCommandName();
                    }
                    getChannelHandler().sendMessage("Commands: " + output, this.getChannelHandler().getChannel(), sender);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            getChannelHandler().sendMessage(this.getHelptext(), this.getChannelHandler().getChannel());
        } catch (NumberFormatException e) {
            getChannelHandler().sendMessage(Memebot.formatText("COMMAND_LIST_ERROR", getChannelHandler(),
                    sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel(), sender);
            e.printStackTrace();
        }
    }
}
