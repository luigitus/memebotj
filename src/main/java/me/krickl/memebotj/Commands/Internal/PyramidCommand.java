package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.User.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class PyramidCommand extends CommandHandler {
    public PyramidCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setNeededCommandPower(CommandPower.broadcasterAbsolute);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            String message = data[0];
            int size = java.lang.Integer.parseInt(data[1]);
            for (int i = 0; i < size; i++) {
                getChannelHandler().sendMessage(message, this.getChannelHandler().getChannel(), sender, isWhisper());
                message = message + " " + data[0];
                //Thread.sleep(1000)
            }
            int i = size;
            while (i >= 0) {
                getChannelHandler().sendMessage(message, this.getChannelHandler().getChannel(), sender, isWhisper());
                message = message.substring(0, message.length() - data[0].length() - 1);
                //Thread.sleep(1000)
                i -= 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
