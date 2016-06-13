package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Commands.CommandReference;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;

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
            CommandReference j = getChannelHandler().findCommandReferneceForString(data[0], getChannelHandler().getChannelCommands());
            if (j != null) {
                getChannelHandler().sendMessage(j.getCH().getHelptext(), getChannelHandler().getChannel(), sender, isWhisper());
                return;
            }
            for (CommandHandler ch : getChannelHandler().getInternalCommands()) {
                if (ch.getCommandName().equals(data[0])) {
                    if (ch.getHelptext().equals("null")) {
                        getChannelHandler().sendMessage(Memebot.formatText("HELP_NOT_FOUND", getChannelHandler(),
                                sender, this, true, new String[]{}, ""), getChannelHandler().getChannel(), sender, isWhisper());
                    } else {
                        getChannelHandler().sendMessage(ch.getHelptext(), getChannelHandler().getChannel(), sender, isWhisper());
                    }
                    return;
                }
            }
            getChannelHandler().sendMessage(Memebot.formatText("HELP_NOT_FOUND", getChannelHandler(), sender, this,
                    true, new String[]{}, ""), getChannelHandler().getChannel(), sender, isWhisper());
        } catch (ArrayIndexOutOfBoundsException e) {
            if (Memebot.useWeb) {
                getChannelHandler().sendMessage(Memebot.webBaseURL + "/help", getChannelHandler().getChannel(), sender, isWhisper());
            } else {
                getChannelHandler().sendMessage(Memebot.formatText("HELP_SYNTAX", getChannelHandler(), sender, this,
                        true, new String[]{"!help <command>"}, getChannelHandler().getChannel()),
                        getChannelHandler().getChannel(), sender, isWhisper());
            }
        }
    }
}
