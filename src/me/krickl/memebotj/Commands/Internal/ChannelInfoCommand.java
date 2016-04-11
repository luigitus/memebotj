package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;

import java.io.IOException;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class ChannelInfoCommand extends CommandHandler {
    public ChannelInfoCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setNeededCommandPower(CommandPower.adminAbsolute);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            if (data[0].equals("reload")) {
                Memebot.readConfig();
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            getChannelHandler().sendMessage("Is live: " + getChannelHandler().isLive() + " || Points per update: " + getChannelHandler().getPointsPerUpdate() + " || Game: " + getChannelHandler().getCurrentGame(), this.getChannelHandler().getChannel());
        }
    }
}
