package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.User.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;

/**
 * This file is part of memebotj.
 * Created by unlink on 11/04/16.
 */
public class RaceCommand extends CommandHandler {
    public RaceCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setHelptext(Memebot.formatText("RACE_SYNTAX", getChannelHandler(), null, this, true, new String[]{}, ""));
        this.setEnabled(true);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {

        if (data.length >= 1 && checkPermissions(sender, CommandPower.modAbsolute, CommandPower.modAbsolute)) {
            if(data.length >= 2) {
                if(data[0].equals("countdown")) {
                    int time = 0;
                    try {
                        time = Integer.parseInt(data[1]);
                    } catch(NumberFormatException e) {

                    }
                    if(time > 15) {
                        time = 15;
                    }
                    for(int i = time + 1; i >= 0; i--) {
                        try {
                            if(i == 0) {
                                getChannelHandler().sendMessage("go!", this.getChannelHandler().getChannel(),
                                        sender, isWhisper());
                            } else {
                                getChannelHandler().sendMessage(String.format("%d", i), this.getChannelHandler().getChannel(),
                                        sender, isWhisper());
                            }
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    return;
                }
            }
            getChannelHandler().setCurrentRaceURL(getChannelHandler().getRaceBaseURL() + "/" + getChannelHandler().getBroadcaster());
            for (String i : data) {
                getChannelHandler().setCurrentRaceURL(getChannelHandler().getCurrentRaceURL() + "/" + i);
            }
            getChannelHandler().sendMessage(getChannelHandler().getCurrentRaceURL(), this.getChannelHandler().getChannel(),
                    sender, isWhisper());
        } else {
            getChannelHandler().sendMessage(getChannelHandler().getCurrentRaceURL(), this.getChannelHandler().getChannel(),
                    sender, isWhisper());
        }
    }
}
