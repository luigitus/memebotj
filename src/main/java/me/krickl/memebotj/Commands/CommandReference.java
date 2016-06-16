package me.krickl.memebotj.Commands;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.User.UserHandler;


// todo this class needs to be used instead of commandhandlers to have a refrence. it'll make sure to load/unload
// todo commandhandlers

/**
 * This file is part of memebotj.
 * Created by unlink on 17/05/16.
 */
public class CommandReference implements CommandInterface, Comparable<CommandReference> {

    protected String commandName = null;
    protected ChannelHandler channelHandler = null;
    protected String dbprefix = "";
    private CommandHandler commandHandler = null;

    public CommandReference(ChannelHandler channelHandler, String commandName, String dbprefix) {
        this.channelHandler = channelHandler;
        this.commandName = commandName;
        this.dbprefix = dbprefix;
        commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
    }

    public void readDB() {
        if (commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        commandHandler.readDB();
    }

    public void removeDB() {
        if (commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        commandHandler.removeDB();
    }

    public void writeDB() {
        if (commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        commandHandler.writeDB();
    }

    public void overrideDB() {
        if (commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        commandHandler.overrideDB();
    }

    public void initCommand() {
        if (commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        commandHandler.initCommand();
    }

    public boolean handleCooldown(UserHandler sender) {
        if (commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        return commandHandler.handleCooldown(sender);
    }

    public boolean startCooldown(UserHandler sender) {
        if (commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        return commandHandler.startCooldown(sender);
    }

    public boolean checkCost(UserHandler sender, double cost) {
        if (commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        return commandHandler.checkCost(sender, cost);
    }

    public void commandScript(UserHandler sender, String[] data) {
        if (commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        commandHandler.commandScript(sender, data);
    }

    public boolean executeCommand(UserHandler sender, String[] data) {
        if (commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        return commandHandler.executeCommand(sender, data);
    }

    public boolean editCommand(String modType, String newValue, UserHandler sender) {
        if (commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }
        boolean success = false;
        success = commandHandler.editCommand(modType, newValue, sender);

        commandName = commandHandler.getCommandName();

        return success;
    }

    public void update() {
        if (commandHandler != null) {
            commandHandler.update();
            if (commandHandler.canBeRemoved() && !commandHandler.getCommandType().equals("timer")) {
                commandHandler.writeDB();
                commandHandler = null;
            }
        }
    }

    public CommandHandler getCH() {
        if (commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }
        return commandHandler;
    }


    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public ChannelHandler getChannelHandler() {
        return channelHandler;
    }

    public void setChannelHandler(ChannelHandler channelHandler) {
        this.channelHandler = channelHandler;
    }

    public String getDbprefix() {
        return dbprefix;
    }

    public void setDbprefix(String dbprefix) {
        this.dbprefix = dbprefix;
    }

    public CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public void setCommandHandler(CommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public int compareTo(CommandReference another) {
        return commandName.compareTo(another.getCommandName());
    }
}
