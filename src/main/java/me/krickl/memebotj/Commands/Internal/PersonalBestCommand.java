package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.SpeedrunCom.SpeedRunCom;
import me.krickl.memebotj.SpeedrunCom.Model.*;
import me.krickl.memebotj.UserHandler;
import retrofit2.Call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class PersonalBestCommand extends CommandHandler {
    public PersonalBestCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        getChannelHandler().getSpeedRunComAPI().updateGame();
        getChannelHandler().sendMessage(formatString(sender), getChannelHandler().getChannel(), sender, isWhisper());
    }

    private String formatString(UserHandler sender) {
        String title = getChannelHandler().getStreamTitle();
        String gameID = getChannelHandler().getSpeedRunComAPI().getGame().getId();
        String userID = getChannelHandler().getSpeedRunComAPI().getUser().getId();
        ArrayList<Category> categories = getChannelHandler().getSpeedRunComAPI().getGame().getCategories();
        for (Category category : categories) {
            if (title.toLowerCase().contains(category.getName().toLowerCase())) {
                String time = getPB(userID, gameID, category.getId());
                return Memebot.formatText("PB_CATEGORY", getChannelHandler(), sender, this, true,
                        new String[]{category.getName(), time}, "");
            }
        }
        return Memebot.formatText("PB_NO_CATEGORY_SET", getChannelHandler(), sender, this, true,
                new String[]{}, "");
    }

    private String getPB(String userID, String gameID, String categoryID) {
        try {
            SpeedRunCom service = getChannelHandler().getSpeedRunComAPI().getService();
            Call<PBLookup> pbs = service.getPersonalBests(userID, gameID);
            ArrayList<RunObject> runs = pbs.execute().body().getData();
            for (RunObject run1 : runs) {
                Run run = run1.getRun();
                if (run.getCategory().equals(categoryID)) {
                    return parseTime(run.getTimes().getPrimaryT());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "THERE'S NO TIME TO EXPLAIN [FOUND_NO_PB_TIME]";
    }

    private String parseTime(long time) {
        long millis = time * 1000;
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    private String formatStringDebug(Game game) {
        String gameInt = game.getNames().getInternational();
        String gameJap = game.getNames().getJapanese();
        String gameID = game.getId();
        String abbreviation = game.getAbbreviation();
        ArrayList<Category> categories = game.getCategories();

        String output = "[DEBUG] ";
        output += "Game (International): " + gameInt;
        if (gameJap != null) {
            output += " Game (Japanese): " + gameJap;
        }
        output += " Game ID: " + gameID + " Abbreviation: " + abbreviation + " Categories:";

        for (Category category : categories) {
            output += " " + category.getName() + ",";
        }

        return output;
    }

    private String formatStringDebug(UserObject user, Game game) {
        String userName = user.getNames().getInternational();
        String userID = user.getId();
        String weblink = user.getWeblink();

        String gameInt = game.getNames().getInternational();
        String gameJap = game.getNames().getJapanese();
        String gameID = game.getId();
        String abbreviation = game.getAbbreviation();

        String output = "[DEBUG] ";
        output += "User: " + userName + " User ID: " + userID + " Weblink: " + weblink + " Game (International): " + gameInt;
        if (gameJap != null) {
            output += " Game (Japanese): " + gameJap;
        }
        output += " Game ID: " + gameID + " Abbreviation: " + abbreviation;
        return output;
    }
}
