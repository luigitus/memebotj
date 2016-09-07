package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.User.UserHandler;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class HugCommand extends CommandHandler {
    public HugCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setFormatData(true);
        this.setHelptext(Memebot.formatText("HUG_SYNTAX", getChannelHandler(), null, this, true, new String[]{}, ""));
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            if (data[0].contains(Memebot.botNick)) {
                getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("HUG_BOT"),
                        getChannelHandler(), sender, this, false, new String[]{}, getChannelHandler().getChannel()),
                        getChannelHandler().getChannel(), sender, isWhisper());
            } else if(data[0].equalsIgnoreCase(sender.getUsername())) {
                String msg = Memebot.formatText("HUG_SELF", getChannelHandler(), sender, this, true,
                        new String[]{}, "");
                getChannelHandler().sendMessage(msg, getChannelHandler().getChannel(), sender, isWhisper());
            } else {
                getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("HUG_SOMEONE"),
                        getChannelHandler(), sender, this, false, new String[]{sender.screenName(), data[0]},
                        getChannelHandler().getChannel()), getChannelHandler().getChannel(), sender, isWhisper());
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("HUG_NOBODY"),
                    getChannelHandler(), sender, this, false, new String[]{sender.screenName()}, ""),
                    getChannelHandler().getChannel(), sender, isWhisper());
        }
    }
}
