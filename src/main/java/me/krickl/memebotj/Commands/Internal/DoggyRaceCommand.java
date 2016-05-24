package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;
import org.bson.Document;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This file is part of memebotj.
 * Created by unlink on 17/05/16.
 */
public class DoggyRaceCommand extends CommandHandler {
    private Document entracnes = new Document();

    public DoggyRaceCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void overrideDB() {
        this.setNeededCommandPower(CommandPower.viewerAbsolute);

        this.setHelptext(Memebot.formatText("DOGGY_SYNTAX", getChannelHandler(), null, this, true, new String[]{}, ""));
    }

    @Override
    public void setDB() {
        super.setDB();
        mongoHandler.updateDocument("entrances", entracnes);
    }

    @Override
    public void readDB() {
        if (!Memebot.useMongo) {
            return;
        }

        super.readDB();

        entracnes = (Document)mongoHandler.getObject("entrances", entracnes);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        if(data.length >= 1) {
            if(data[0].equals("enter") && data.length >= 3) {
                //enter doggy race
                if(!entracnes.containsKey(sender.getUsername())) {
                    String message = Memebot.formatText("DOGGY_ENTER", getChannelHandler(), null, this, true, new
                            String[]{data[1], data[2]}, "");

                    getChannelHandler().sendMessage(message, getChannelHandler().getChannel(), sender, isWhisper());

                    try {
                        entracnes.append(sender.getUsername(), new Document().append("bet", Integer.parseInt(data[1]))
                                .append("doggy", data[2]));
                    } catch(NumberFormatException e) {
                        message = Memebot.formatText("DOGGY_ENTER_NFE", getChannelHandler(), null, this, true, new
                                String[]{data[1], data[2]}, "");

                        getChannelHandler().sendMessage(message, getChannelHandler().getChannel(), sender, isWhisper());
                    }
                } else {
                    String message = Memebot.formatText("DOGGY_ENTER_ERROR", getChannelHandler(), null, this, true, new
                            String[]{data[1], data[2]}, "");

                    getChannelHandler().sendMessage(message, getChannelHandler().getChannel(), sender, isWhisper());
                }

            } else if(data[0].equals("status")) {
                String message = Memebot.formatText("DOGGY_STATUS", getChannelHandler(), null, this, true, new
                        String[]{}, "");

                getChannelHandler().sendMessage(message, getChannelHandler().getChannel(), sender, isWhisper());
            }
        } else {
            getChannelHandler().sendMessage(getHelptext(), getChannelHandler().getChannel(), sender, isWhisper());
        }

        //start doggy race at X entrances
        if(entracnes.keySet().size() >= 15) {
            SecureRandom random = new SecureRandom();

            ArrayList<String> entracnesKeys = new ArrayList();
            for(String keys : entracnes.keySet()) {
                entracnesKeys.add(keys);
            }

            // continue until only one person is left
            while(entracnesKeys.size() > 3) {
                // round 1
                for (int i = 0; i< entracnesKeys.size(); i++) {
                    if (!random.nextBoolean() && entracnesKeys.size() > 3) {
                        entracnesKeys.remove(i);
                    }
                }
            }

            String winners = "";

            for(int i = 2; i <= 0; i--) {
                Document winner = (Document) entracnes.get(entracnesKeys.get(i));
                String winnerName = entracnesKeys.get(i);

                winners = winnerName + " ";

                UserHandler winnerUH = null;
                if (getChannelHandler().getUserList().containsKey(winnerName)) {
                    winnerUH = getChannelHandler().getUserList().get(winnerName);

                } else {
                    winnerUH = new UserHandler(winnerName, getChannelHandler().getChannel());
                }

                winnerUH.setPoints(winnerUH.getPoints() + (double) winner.getOrDefault("bet", 0) * (i + 1));
            }


            String message = Memebot.formatText("DOGGY_START", getChannelHandler(), null, this, true, new
                    String[]{winners}, "");

            getChannelHandler().sendMessage(message, getChannelHandler().getChannel(), sender, isWhisper());
        }
    }
}
