package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.SpeedrunCom.Model.*;
import me.krickl.memebotj.SpeedrunCom.SpeedRunCom;
import me.krickl.memebotj.UserHandler;
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
        getChannelHandler().getSpeedRunComAPI().updateGame();
        getChannelHandler().sendMessage(formatString(sender), getChannelHandler().getChannel(), sender, isWhisper());
    }

    private String formatString(UserHandler sender) {
        String title = getChannelHandler().getStreamTitle();
        String gameID = getChannelHandler().getSpeedRunComAPI().getGame().getId();
        ArrayList<Category> categories = getChannelHandler().getSpeedRunComAPI().getGame().getCategories();
        for (Category category : categories) {
            if (title.toLowerCase().contains(category.getName().toLowerCase())) {
                String[] wr = getWR(gameID, category.getId());
                return Memebot.formatText("WR_CATEGORY", getChannelHandler(), sender, this, true,
                        new String[]{category.getName(), wr[0], wr[1]}, "");
            }
        }
        return Memebot.formatText("PB_NO_CATEGORY_SET", getChannelHandler(), sender, this, true,
                new String[]{}, "");
    }

    private String[] getWR(String gameID, String categoryID) {
        try {
            SpeedRunCom service = getChannelHandler().getSpeedRunComAPI().getService();
            Call<WRLookup> wrs = service.getWorldRecords(gameID);
            ArrayList<RecordObject> runs = wrs.execute().body().getData();
            for (RecordObject run1 : runs) {
                if (!run1.getRuns().isEmpty()) {
                    if (run1.getLevel() == null) {
                        Run tmpRun = run1.getRuns().get(0).getRun();
                        if (run1.getGame().equals(gameID)) {
                            if (run1.getCategory().equals(categoryID)) {
                                String userID = tmpRun.getPlayers().get(0).getId();
                                Call<UserLookup> user = service.getUser(userID);
                                String wrHolder = user.execute().body().getData().getName();
                                return new String[]{parseTime(tmpRun.getTimes().getPrimaryT()), wrHolder};
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[]{"THERE'S NO TIME TO EXPLAIN", "[FOUND_NO_PB_TIME]"};
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

}
