package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.User.UserHandler;
import me.krickl.memebotj.Utility.BuildInfo;

/**
 * This file is part of memebotj.
 * Created by lukas on 8/1/2016.
 */
public class BypassCooldownCommand extends CommandHandler {
    public BypassCooldownCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setEnabled(true);
        this.setHelptext(Memebot.formatText("BP_SYNTAX", getChannelHandler(), null, this, true, new String[]{},
                ""));
        this.setUserCooldownLength(600);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        if(data.length < 1) {
            getChannelHandler().sendMessage(this.getHelptext(), getChannelHandler().getChannel(), sender, isWhisper());
            return;
        }

        if(checkCost(sender, sender.getPoints() * 0.01)) {
            if(!sender.getUserCommandCooldowns().containsKey(data[0])) {
                String ok = Memebot.formatText("BP_NOT", getChannelHandler(), null, this, true,
                        new String[]{}, "");
            } else {
                sender.setPoints(sender.getPoints() - sender.getPoints() * 0.01);
                String ok = Memebot.formatText("BP_OK", getChannelHandler(), null, this, true,
                        new String[]{String.format("%.2f", sender.getPoints() * 0.01)}, "");
                sender.getUserCommandCooldowns().remove(data[0]);
            }

        } else {
            String error = Memebot.formatText("BP_ERROR", getChannelHandler(), null, this, true,
                    new String[]{String.format("%.2f", sender.getPoints() * 0.01)}, "");
            getChannelHandler().sendMessage(error, getChannelHandler().getChannel(), sender, isWhisper());
        }
    }
}
