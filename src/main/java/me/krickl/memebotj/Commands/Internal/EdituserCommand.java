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
public class EdituserCommand extends CommandHandler {
    public EdituserCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setHelptext(Memebot.formatText("EDIT_USER_SYNTAX", getChannelHandler(), null, this, true, new String[]{}, ""));

        this.setNeededCommandPower(CommandPower.viewerAbsolute);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        UserHandler uh = null;

        try {
            if (getChannelHandler().getUserList().containsKey(data[1].toLowerCase())) {
                uh = getChannelHandler().getUserList().get(data[1].toLowerCase());
            } else {
                uh = new UserHandler(data[1].toLowerCase(), getChannelHandler().getChannel());
            }

            if (uh.isNewUser() && checkPermissions(sender, CommandPower.adminAbsolute, CommandPower.adminAbsolute)) {
                getChannelHandler().sendMessage(Memebot.formatText("EDIT_USER_NEVER_JOINED", getChannelHandler(), sender, this, true, new String[]{data[1]}, ""), this.getChannelHandler().getChannel());
                return;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        try {
            if (data[0].equals("power") && checkPermissions(sender, CommandPower.adminAbsolute, CommandPower.adminAbsolute)) {
                boolean success = false;
                int newCP = java.lang.Integer.parseInt(data[2]);

                if ((newCP + uh.getAutoCommandPower()) > sender.getCommandPower()) {
                    getChannelHandler().sendMessage(Memebot.formatText("EDIT_USER_FAILED_UP", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
                    return;
                }
                uh.setCustomCommandPower(java.lang.Integer.parseInt(data[2]));
                uh.setCommandPower(uh.getAutoCommandPower());
                uh.writeDB();
                success = true;

                if (success) {
                    getChannelHandler().sendMessage(Memebot.formatText("EDIT_USER_UP", getChannelHandler(), sender, this, true, new String[]{data[2]}, ""), this.getChannelHandler().getChannel());
                }

            } else if (data[0].equals("save") && checkPermissions(sender, CommandPower.adminAbsolute, CommandPower.adminAbsolute)) {
                sender.writeDB();
            } else if (data[0].equals("login")) {
                getChannelHandler().sendMessage(sender.getOauth(), this.getChannelHandler().getChannel(), sender, true);
            } else if (data[0].equals("restlogin")) {
                getChannelHandler().sendMessage(Memebot.formatText("EDIT_USER_RESETLOGIN", getChannelHandler(), sender, this, true, new String[]{data[2]}, ""), this.getChannelHandler().getChannel());

                sender.setOauth(sender.resetOAuth());
                sender.setAPIKey(sender.resetOAuth());
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
}
