package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.SpeedrunCom.Model.*;
import me.krickl.memebotj.SpeedrunCom.ISpeedRunCom;
import me.krickl.memebotj.SpeedrunCom.SpeedRunComAPI;
import me.krickl.memebotj.User.UserHandler;
import retrofit2.Call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class WorldRecordCommand extends CommandHandler {
    public WorldRecordCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        if (getChannelHandler().getGame() != null) {
            try {
                if (data[0].equals("list")) {
                    getChannelHandler().sendMessage(formatString(sender, true), getChannelHandler().getChannel(), sender, isWhisper());
                    return;
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {
            }
            //getChannelHandler().updateGame();
            getChannelHandler().sendMessage(formatString(sender, false), getChannelHandler().getChannel(), sender, isWhisper());
        } else {
            getChannelHandler().sendMessage(Memebot.formatText("WR_NO_GAME_AVAILABLE", getChannelHandler(), sender,
                    this, true, new String[]{}, ""), getChannelHandler().getChannel(), sender, isWhisper());
        }
    }

    private String formatString(UserHandler sender, boolean list) {
        String title = getChannelHandler().getStreamTitle();
        String gameID = getChannelHandler().getGame().getId();
        ArrayList<Category> categories = getChannelHandler().getGame().getCategories();
        if (!list) {
            for (Category category : categories) {
                if (title.toLowerCase().contains(category.getName().toLowerCase())) {
                    String[] wr = getWR(gameID, category.getId());
                    return Memebot.formatText("WR_CATEGORY", getChannelHandler(), sender, this, true,
                            new String[]{category.getName(), wr[0], wr[1], wr[2]}, "");
                }
            }
        }
        return Memebot.formatText("WR_ALL_GAME", getChannelHandler(), sender, this, true,
                new String[]{getWRs(gameID, categories)}, "");
    }

    private String[] getWR(String gameID, String categoryID) {
        try {
            SpeedRunComAPI speedRunComAPI = (SpeedRunComAPI) Memebot.plugins.get("speedruncomapi");
            ISpeedRunCom service = speedRunComAPI.getService();
            Call<WRLookup> wr = service.getWorldRecord(gameID, categoryID, "players");
            RecordObject record = wr.execute().body().getData();
            if (record.getUsername().equalsIgnoreCase("trevperson") && getChannelHandler().getBroadcaster().equals("trevperson")) {
                return new String[]{parseTime(record.getTime()), "Me",
                        record.getTwitchURL() != null ? "- " + record.getTwitchURL() : ""};
            }
            return new String[]{parseTime(record.getTime()), record.getUsername(),
                    record.getTwitchURL() != null ? "- " + record.getTwitchURL() : ""};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[]{"THERE'S NO TIME TO EXPLAIN", "[FOUND_NO_PB_TIME]", "4Head"};
    }

    private String getWRs(String gameID, ArrayList<Category> categories) {
        String output = "";
        try {
            SpeedRunComAPI speedRunComAPI = (SpeedRunComAPI) Memebot.plugins.get("speedruncomapi");
            ISpeedRunCom service = speedRunComAPI.getService();
            Call<WRSLookup> wrs = service.getWorldRecords(gameID, "players");
            ArrayList<RecordObject> records = wrs.execute().body().getData();
            for (RecordObject record : records) {
                if (record.getLevel() == null) {
                    Run run = record.getRuns().get(0).getRun();
                    for (Category category : categories) {
                        if (run.getCategory().equals(category.getId())) {
                            output += " " + category.getName();
                            break;
                        }
                    }
                    output += " " + parseTime(run.getTimes().getPrimaryT()) + " held by " + record.getUsername() + ",";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.substring(0, output.lastIndexOf(","));
    }

    private String parseTime(long time) {
        long millis = time * 1000;
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

}
