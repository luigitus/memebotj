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
public class WhoisCommand extends CommandHandler {
    public WhoisCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setHelptext(Memebot.formatText("WHOIS_SYNTAX", getChannelHandler(), null, this, true, new String[]{}, ""));

        this.setNeededCommandPower(CommandPower.modAbsolute);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            String user = data[0].toLowerCase();
            UserHandler uh = null;
            if (getChannelHandler().getUserList().containsKey(user)) {
                uh = getChannelHandler().getUserList().get(user);
            } else {
                uh = new UserHandler(user, this.getChannelHandler().getChannel());
            }

            if (uh.isNewUser()) {
                getChannelHandler().sendMessage(Memebot.formatText("WHOIS_NEW_USER", getChannelHandler(), sender, this, true, new String[]{sender.screenName()}, ""), getChannelHandler().getChannel());
            }

            boolean isCat = false;
            boolean swears = true;
            if (user.contains("cat") || user.contains("kitty")) {
                isCat = true;
            }
            if (user.contains("95shade")) {
                swears = false;
            }
            getChannelHandler().sendMessage(uh.getUsername() + " || Broadcaster: " + java.lang.Boolean.toString(uh.isUserBroadcaster()) +
                    " || Mod: " +
                    java.lang.Boolean.toString(uh.isModerator()) +
                    " || Command Power: " +
                    java.lang.Integer.toString(uh.getCommandPower()) +
                    " || Timeouts: " +
                    java.lang.Integer.toString(uh.getTimeouts()) +
                    " || Timeout Duration " + Integer.toString(uh.getLastTimeoutDuration()) +
                    " || Timeout Reason " + uh.getLastTimeoutReason() +
                    " || Is known user: " +
                    java.lang.Boolean.toString(!uh.isNewUser()) +
                    " || Date joined: " +
                    uh.getDateJoined() +
                    " || Screenname: " + uh.screenName() +
                    "|| Weird Boolean: " + "Rip Weird Boolean" +
                    " || Is user a cat: " +
                    java.lang.Boolean.toString(isCat)
                    + "|| Is user a bad girl/boy: " + Boolean.toString(swears)
                    + "|| Jackpot wins: " + Integer.toString(sender.getJackpotWins()), this.getChannelHandler().getChannel(), sender);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
}
