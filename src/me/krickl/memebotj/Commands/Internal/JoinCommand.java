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
public class JoinCommand extends CommandHandler {
    public JoinCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setNeededCommandPower(10);

        this.setHelptext(Memebot.formatText("JOIN_SYNTAX", getChannelHandler(), null, this, true, new String[]{}, ""));
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            if (getChannelHandler().getChannel().equals(Memebot.mainChannel)) {
                for (ChannelHandler channel : Memebot.joinedChannels) {
                    if (channel.getChannel().equals("#" + sender.getUsername().toLowerCase())) {
                        getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("JOIN_FAIL"), getChannelHandler(), sender, this, false, new String[]{sender.getUsername()}, ""), getChannelHandler().getChannel());
                        return;
                    }
                }
                Memebot.joinChannel("#" + sender.getUsername());
                getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("JOIN"), getChannelHandler(), sender, this, false, new String[]{sender.getUsername()}, ""), getChannelHandler().getChannel());
            }
        } catch(ArrayIndexOutOfBoundsException e) {
        }
    }
}
