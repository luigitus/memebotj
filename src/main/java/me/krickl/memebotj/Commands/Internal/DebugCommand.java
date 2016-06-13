package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.UserHandler;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class DebugCommand extends CommandHandler {
    public DebugCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setHelptext("");
        this.setNeededCommandPower(75);
        this.setUserCooldownLength(0);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
    }
}
