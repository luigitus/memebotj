package me.krickl.memebotj.Commands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.UserHandler;


// todo this class needs to be used instead of commandhandlers to have a refrence. it'll make sure to load/unload
// todo commandhandlers
/**
 * This file is part of memebotj.
 * Created by unlink on 17/05/16.
 */
public class CommandRefernce implements CommandInterface {

    protected String commandName = null;
    protected ChannelHandler channelHandler = null;
    protected String dbprefix = "";
    private CommandHandler commandHandler = null;

    public CommandRefernce(ChannelHandler channelHandler, String commandName, String dbprefix) {
        this.channelHandler = channelHandler;
        this.commandName = commandName;
        this.dbprefix = dbprefix;
        commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
    }

    public void readDB() {
        if(commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        commandHandler.readDB();
    }

    public void removeDB() {
        if(commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        commandHandler.removeDB();
    }

    public void writeDB() {
        if(commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        commandHandler.writeDB();
    }

    public void overrideDB() {
        if(commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        commandHandler.overrideDB();
    }

    public void initCommand() {
        if(commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        commandHandler.initCommand();
    }

    public boolean handleCooldown(UserHandler sender) {
        if(commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        return commandHandler.handleCooldown(sender);
    }

    public boolean startCooldown(UserHandler sender) {
        if(commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        return commandHandler.startCooldown(sender);
    }

    public boolean checkCost(UserHandler sender, double cost) {
        if(commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        return commandHandler.checkCost(sender, cost);
    }

    public void commandScript(UserHandler sender, String[] data) {
        if(commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        commandHandler.commandScript(sender, data);
    }

    public boolean executeCommand(UserHandler sender, String[] data) {
        if(commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        return commandHandler.executeCommand(sender, data);
    }

    public boolean editCommand(String modType, String newValue, UserHandler sender) {
        if(commandHandler == null) {
            commandHandler = new CommandHandler(channelHandler, commandName, dbprefix);
        }

        return commandHandler.editCommand(modType, newValue, sender);
    }

    public void update() {
        if(commandHandler != null) {
            if(commandHandler.canBeRemoved()) {
                commandHandler.writeDB();
                commandHandler = null;
            }
            commandHandler.update();
        }
    }
}
