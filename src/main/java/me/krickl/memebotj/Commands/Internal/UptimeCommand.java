package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;

/**
 * This file is part of memebotj.
 * Created by unlink on 03/05/16.
 */
public class UptimeCommand extends CommandHandler {
    public UptimeCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        // fuck it let's just use this
        getChannelHandler().sendMessage(Memebot.urlRequest("http://owyn.us/APIs/Uptime.php?Broadcaster="
                + getChannelHandler().getBroadcaster()), getChannelHandler().getChannel(),
        sender, isWhisper());
    }
}
