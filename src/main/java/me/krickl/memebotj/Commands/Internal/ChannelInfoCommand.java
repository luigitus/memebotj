package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.User.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;
import org.json.simple.JSONObject;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class ChannelInfoCommand extends CommandHandler {
    public ChannelInfoCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setNeededCommandPower(CommandPower.adminAbsolute);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        try {
            if (data[0].equals("reload")) {
                Memebot.readConfig();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            JSONObject jsonObject = (JSONObject) getChannelHandler().toJSONObject().get("data");

            String output = "";

            for (Object key : jsonObject.keySet()) {
                output = output + key.toString() + ": " + jsonObject.get(key).toString() + " || ";
            }
            getChannelHandler().sendMessage(output, getChannelHandler().getChannel(), sender, isWhisper());

            /*getChannelHandler().sendMessage("Is live: " + getChannelHandler().isLive() +
                            " || Points per update: " + getChannelHandler().getPointsPerUpdate() +
                            " || Game: " + getChannelHandler().getCurrentGame()
                            + " || Private DB: "
                            + Boolean.toString(Memebot.channelsPrivate.contains(this.getChannelHandler().getChannel()))
                            + " || Current Viewer Count: " + Integer.toString(getChannelHandler().getViewerNumber()),
                    this.getChannelHandler().getChannel(), sender, isWhisper());*/
        }
    }
}
