package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;
import org.json.simple.JSONObject;

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

            if(!user.equals("fniure")) {
                // todo make it render json of user object

                // get json
                JSONObject jsonObject = (JSONObject) uh.toJSONObject().get("data");

                String output = "";

                for(Object key : jsonObject.keySet()) {
                    output = output + key.toString() + ": " + jsonObject.get(key).toString() + " || ";
                }
                getChannelHandler().sendMessage(output, getChannelHandler().getChannel(), sender, isWhisper());

               /* getChannelHandler().sendMessage(uh.getUsername() + " || Broadcaster: " + java.lang.Boolean.toString(uh.isUserBroadcaster()) +
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
                        + "|| Jackpot wins: " + Integer.toString(sender.getJackpotWins()), this.getChannelHandler().getChannel(), sender); */
            } else {
                getChannelHandler().sendMessage("Who the **** is even fniure? Last time I checked ennopp had wr and wss the best.");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
}
