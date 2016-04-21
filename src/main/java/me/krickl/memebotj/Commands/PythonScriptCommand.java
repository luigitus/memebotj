package me.krickl.memebotj.Commands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.UserHandler;

/**
 * This command will eventually be used for the python scripting interface (WIP)
 * This file is part of memebotj.
 * Created by unlink on 21/04/16.
 */
public class PythonScriptCommand extends CommandHandler {
    public PythonScriptCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {

    }
}
