package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.User.UserHandler;

/**
 * Created by unlink on 8/16/2016.
 */
public class MorninCommand extends CommandHandler {
    public MorninCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setUserCooldownLength(10);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        String output = "";
        for(String s : data) {
            output = output + s + " ";
        }
        String formatted = Memebot.formatText("@{sender} " + output, getChannelHandler(), sender,
                this, false, new String[]{}, "");
        getChannelHandler().sendMessage(formatted, getChannelHandler().getChannel(),
                sender, isWhisper());
    }
}
