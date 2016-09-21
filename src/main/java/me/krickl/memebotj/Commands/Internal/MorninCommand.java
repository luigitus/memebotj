package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.User.UserHandler;

import java.security.SecureRandom;

/**
 * Created by unlink on 8/16/2016.
 */
public class MorninCommand extends CommandHandler {
    public MorninCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setUserCooldownLength(10);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {

        boolean whenIs = false;
        if(data.length > 1) {
            if(data[0].equals("when") && data[1].equals("is")) {
                whenIs = true;
            }
        }

        if(!whenIs) {
            String output = "";
            for (String s : data) {
                output = output + s + " ";
            }
            String formatted = Memebot.formatText("@{sender} " + output, getChannelHandler(), sender,
                    this, false, new String[]{}, "");
            getChannelHandler().sendMessage(formatted, getChannelHandler().getChannel(),
                    sender, isWhisper());
        } else {
            String[] whenIsList = {"Never", "At 8:01 AM CEST", "Tomorrow"};

            SecureRandom ran = new SecureRandom();

            String formatted = Memebot.formatText("@{sender}: " + whenIsList[ran.nextInt(whenIsList.length)], getChannelHandler(), sender,
                    this, false, new String[]{}, "");
            getChannelHandler().sendMessage(formatted, getChannelHandler().getChannel(),
                    sender, isWhisper());
        }
    }
}
