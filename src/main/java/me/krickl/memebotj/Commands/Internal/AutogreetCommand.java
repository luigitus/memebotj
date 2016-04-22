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
public class AutogreetCommand extends CommandHandler {

    public AutogreetCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setHelptext(Memebot.formatText("AUTOGREET_SYNTAX", getChannelHandler(), null, this, true, new String[]{}, ""));
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            String nameToModify = data[1].toLowerCase();
            String message = "";
            UserHandler user = null;
            if (getChannelHandler().getUserList().containsKey(nameToModify)) {
                user = getChannelHandler().getUserList().get(nameToModify);
            } else {
                user = new UserHandler(nameToModify, getChannelHandler().getChannel());
            }
            if (((data[0].equals("add") || data[0].equals("edit")) && checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute))) {
                String newValue = data[2];
                for (int x = 3; x < data.length; x++) {
                    newValue = newValue + " " + data[x];
                }
                if (!user.isNewUser()) {
                    user.setAutogreet(newValue);
                    user.writeDB();
                    message = Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("AUTOGREET_ADDED"), getChannelHandler(), sender, this, false, new String[]{}, "");
                } else {
                    message = Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("AUTOGREET_FAILED"), getChannelHandler(), sender, this, false, new String[]{}, "");
                }
            } else if (data[0].equals("remove") && checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {
                if (!user.isNewUser()) {
                    user.setAutogreet("");
                    user.writeDB();
                    message = Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("AUTOGREET_REMOVED"), getChannelHandler(), sender, this, false, new String[]{}, "");
                }
            } else if (data[0].equals("get")) {
                if (!user.isNewUser()) {
                    getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("AUTOGREET_GET"), getChannelHandler(), sender, this, false, new String[]{user.getUsername(), user.getAutogreet()}, getChannelHandler().getChannel()));
                }
            } else if (data[0].equals("toggle")) {
                sender.setEnableAutogreets(!sender.isEnableAutogreets());
                getChannelHandler().sendMessage(Memebot.formatText(getChannelHandler().getLocalisation().localisedStringFor("AUTOGREET_TOGGLE"), getChannelHandler(), sender, this, false, new String[]{sender.getUsername(), Boolean.toString(sender.isEnableAutogreets())}, getChannelHandler().getChannel()));
            }
            getChannelHandler().sendMessage(message, getChannelHandler().getChannel());
        } catch(ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
}
