package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.User.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;

/**
 * Created by unlink on 8/7/2016.
 */
public class UpdateChannelInformationCommand extends CommandHandler {
    public UpdateChannelInformationCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {

    }
}
