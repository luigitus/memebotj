package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;

import java.nio.channels.Channel;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class HelpCommand extends CommandHandler {
    public HelpCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setHelptext("Displays help for other commands");
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            int j = getChannelHandler().findCommand(data[0]);
            if (j != -1) {
                getChannelHandler().sendMessage(getChannelHandler().getChannelCommands().get(j).getHelptext(), getChannelHandler().getChannel());
                return;
            }
            for (CommandHandler ch : getChannelHandler().getInternalCommands()) {
                if(ch.getCommandName().equals(data[0])) {
                    if (ch.getHelptext().equals("null")) {
                        getChannelHandler().sendMessage(Memebot.formatText("HELP_NOT_FOUND", getChannelHandler(), sender, this, true, new String[]{}, ""), getChannelHandler().getChannel());
                    } else {
                        getChannelHandler().sendMessage(ch.getHelptext(), getChannelHandler().getChannel());
                    }
                    return;
                }
            }
            getChannelHandler().sendMessage(Memebot.formatText("HELP_NOT_FOUND", getChannelHandler(), sender, this, true, new String[]{}, ""), getChannelHandler().getChannel());
        } catch(ArrayIndexOutOfBoundsException e) {
            if(Memebot.useWeb) {
                getChannelHandler().sendMessage(Memebot.webBaseURL + "/help", getChannelHandler().getChannel());
            } else {
                getChannelHandler().sendMessage(Memebot.formatText("HELP_SYNTAX", getChannelHandler(), sender, this, true, new String[]{"!help <command>"}, getChannelHandler().getChannel()));
            }
        }
    }
}
