package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;

/**
 * This file is part of memebotj.
 * Created by unlink on 19/04/16.
 */
public class SendMessageCommand extends CommandHandler {
    public SendMessageCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setNeededCommandPower(CommandPower.botModAbsolute);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            String sendToChannel = data[0];
            String message = "";
            boolean includeName = false;

            for (int i = 1; i < data.length; i++) {
                message = message + data[i] + " ";
            }

            if (!sendToChannel.contains("#")) {
                sendToChannel = "#" + sendToChannel;
            }

            if (sendToChannel.equals("#all#") || sendToChannel.equals("#live#")) {
                includeName = true;
            }

            if (includeName) {
                message = "<" + sender.getUsername() + "> " + message;
            }

            if (sendToChannel.equals("#all#")) {
                for (ChannelHandler channelHandler : Memebot.joinedChannels) {
                    channelHandler.sendMessage(message, channelHandler.getChannel(), sender, false, true);
                }
            } else if (sendToChannel.equals("#live#")) {
                for (ChannelHandler channelHandler : Memebot.joinedChannels) {
                    if (channelHandler.isLive()) {
                        channelHandler.sendMessage(message, channelHandler.getChannel(), sender, false, true);
                    }
                }
            } else {
                getChannelHandler().sendMessage(message, sendToChannel, sender, false, true);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            log.warning(e.toString());
        }
    }
}
