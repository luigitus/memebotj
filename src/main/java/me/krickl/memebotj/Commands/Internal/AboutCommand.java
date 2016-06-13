package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.BuildInfo;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class AboutCommand extends CommandHandler {

    public AboutCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setHelptext("");
        this.setUserCooldownLength(600);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        getChannelHandler().sendMessage(BuildInfo.appName + " version " + BuildInfo.version + ". Developed by " +
                BuildInfo.dev, this.getChannelHandler().getChannel(), sender, isWhisper());
        getChannelHandler().sendMessage("Fork me RitzMitz : https://github.com/unlink2/memebotj", this.getChannelHandler().getChannel(),
                sender, isWhisper());
        getChannelHandler().sendMessage("Just type !mejoin in chat to get me! :)", this.getChannelHandler().getChannel(),
                sender, isWhisper());
    }
}
