package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class RestartThreadCommand extends CommandHandler {
    public RestartThreadCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setNeededCommandPower(CommandPower.adminAbsolute);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("RESTART"), getChannelHandler(), sender, this, false, new String[]{}, ""), this.getChannelHandler().getChannel());
        Memebot.saveAll();
        getChannelHandler().setJoined(false);
    }
}
