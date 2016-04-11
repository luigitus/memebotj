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
public class QuitCommand extends CommandHandler {
    public QuitCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setNeededCommandPower(CommandPower.adminAbsolute);

        this.setUnformattedOutput(Memebot.formatText("QUIT", getChannelHandler(), null, this, true, new String[]{}, ""));

        this.setHelptext(Memebot.formatText("QUIT_SYNTAX", getChannelHandler(), null, this, true, new String[]{}, ""));
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        System.exit(0);
    }
}
