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
public class RaceCommand extends CommandHandler {
    public RaceCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setHelptext(Memebot.formatText("RACE_SYNTAX", getChannelHandler(), null, this, true, new String[]{}, ""));
        this.setEnabled(true);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        if (data.length >= 1 && checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {
            getChannelHandler().setCurrentRaceURL(getChannelHandler().getRaceBaseURL() + "/" + getChannelHandler().getBroadcaster());
            for (String i : data) {
                getChannelHandler().setCurrentRaceURL(getChannelHandler().getCurrentRaceURL() + "/" + i);
            }
            getChannelHandler().sendMessage(getChannelHandler().getCurrentRaceURL(), this.getChannelHandler().getChannel());
        } else {
            getChannelHandler().sendMessage(getChannelHandler().getCurrentRaceURL(), this.getChannelHandler().getChannel());
        }
    }
}
