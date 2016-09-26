package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Database.MongoHandler;
import me.krickl.memebotj.Exceptions.DatabaseReadException;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.SpeedrunCom.Model.*;
import me.krickl.memebotj.SpeedrunCom.ISpeedRunCom;
import me.krickl.memebotj.SpeedrunCom.SpeedRunComAPI;
import me.krickl.memebotj.Twitch.Model.Channel;
import me.krickl.memebotj.Twitch.Model.Stream;
import me.krickl.memebotj.User.UserHandler;
import me.krickl.memebotj.Utility.CommandPower;
import retrofit2.Call;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * This file is part of memebotj.
 * Created by Luigitus on 01/05/16.
 */
public class WorldRecordCommand extends CommandHandler {
    private MongoHandler worldRecordOverrideHandler;

    public WorldRecordCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
        if (Memebot.useMongo) {
            worldRecordOverrideHandler = new MongoHandler(Memebot.db, "#worldrecords#");
            try {
                worldRecordOverrideHandler.readDatabase("wrs");
            } catch (DatabaseReadException e) {
                log.log(e.toString());
            }
        }
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        ChannelHandler ch = getChannelHandler();
        if (ch.getGame() != null) {
            try {
                if (data[0].equals("list")) {
                    ch.sendMessage(formatString(sender, true), getChannelHandler().getChannel(), sender, isWhisper());
                    return;
                } else if(data[0].equals("issue")) {
                    ch.sendMessage(Memebot.formatText("WR_ISSUE", getChannelHandler(), sender, this, true,
                            new String[]{}, ""), getChannelHandler().getChannel(), sender, isWhisper());
                    return;
                } else if(data[0].equals("add") && checkPermissions(sender, CommandPower.botModAbsolute,
                        CommandPower.botModAbsolute)) {
                    //TODO add World Records manually
                    return;
                } else if(data[0].equals("remove") && checkPermissions(sender, CommandPower.botModAbsolute,
                        CommandPower.botModAbsolute)) {
                    //TODO remove manually added World Records
                    return;
                } else if (data[0].equals("search") || data[0].equals("lookup") || data[0].equals("s")) {
                    if (data.length == 2) {
                        // Search for game abbreviation and list all World Records of it
                        Game lookupResult = lookupGame(data[1]);
                        if (lookupResult != null) {
                            ch.sendMessage(formatString(sender, lookupResult, null, true),
                                    getChannelHandler().getChannel(), sender, isWhisper());
                        } else {
                            ch.sendMessage(Memebot.formatText("WR_GAME_NOT_FOUND",
                                    getChannelHandler(), sender, this, true, new String[]{}, ""),
                                    getChannelHandler().getChannel(), sender, isWhisper());
                        }
                        return;
                    } else if (data.length >= 3) {
                        // Search for game abbreviation and return World Record
                        Game lookupResult = lookupGame(data[1]);
                        String searchKey = "";
                        for (int i = 2, dataLength = data.length; i < dataLength; i++) {
                            String tmp = data[i];
                            searchKey += tmp + " ";
                        }
                        searchKey.trim();
                        if (lookupResult != null) {
                            ch.sendMessage(formatString(sender, lookupResult, searchKey, false),
                                    getChannelHandler().getChannel(), sender, isWhisper());
                        }
                        return;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ignored) {}
            ch.sendMessage(formatString(sender, false), getChannelHandler().getChannel(), sender, isWhisper());
        } else {
            ch.sendMessage(Memebot.formatText("WR_NO_GAME_AVAILABLE", getChannelHandler(), sender,
                    this, true, new String[]{}, ""), getChannelHandler().getChannel(), sender, isWhisper());
        }
    }

    private String formatString(UserHandler sender, boolean list) {
        return formatString(sender, null, null, list);
    }

    private String formatString(UserHandler sender, Game game, String searchCategory, boolean list) {
        String searchKey;
        String gameID;
        ArrayList<Category> categories;
        if (game == null) {
            game = getChannelHandler().getGame();
            gameID = game.getId();
            categories = getChannelHandler().getGame().getCategories();
            searchKey = getChannelHandler().getStreamTitle();
        } else {
            gameID = game.getId();
            categories = game.getCategories();
            searchKey = searchCategory;
        }
        if (!list && searchKey != null) {
            for (Category category : categories) {
                if (searchKey.toLowerCase().contains(category.getName().toLowerCase())) {
                    String[] wr = getWR(gameID, category.getId());
                    return Memebot.formatText("WR_CATEGORY", getChannelHandler(), sender, this, true,
                            new String[]{game.getNames().getInternational(), category.getName(), wr[0], wr[1], wr[2]}, "");
                }
            }
        }
        return Memebot.formatText("WR_ALL_GAME", getChannelHandler(), sender, this, true,
                new String[]{game.getNames().getInternational(), getWRs(gameID, categories)}, "");
    }

    private String[] getWR(String gameID, String categoryID) {
        try {
            SpeedRunComAPI speedRunComAPI = (SpeedRunComAPI) Memebot.plugins.get("speedruncomapi");
            ISpeedRunCom service = speedRunComAPI.getService();
            Call<WRLookup> wr = service.getWorldRecord(gameID, categoryID, "players");
            RecordObject record = wr.execute().body().getData();
            if (record.getUsername().equalsIgnoreCase("trevperson") &&
                    getChannelHandler().getBroadcaster().equals("trevperson")) {
                // That Trev Person never has World Record, that Me guy steals it away from him every time.
                return new String[]{parseTime(record.getTime()), "Me",
                        record.getTwitchURL() != null ? "- " + record.getTwitchURL() : ""};
            }
            return new String[]{parseTime(record.getTime()), record.getUsername(),
                    record.getTwitchURL() != null ? "- " + record.getTwitchURL() : ""};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[]{"THERE'S NO TIME TO EXPLAIN", "[FOUND_NO_WR_TIME]", "4Head"};
    }

    private String getWRs(String gameID, ArrayList<Category> categories) {
        String output = "";
        try {
            SpeedRunComAPI speedRunComAPI = (SpeedRunComAPI) Memebot.plugins.get("speedruncomapi");
            ISpeedRunCom service = speedRunComAPI.getService();
            Call<WRSLookup> wrs = service.getWorldRecords(gameID, "players");
            ArrayList<RecordObject> records = wrs.execute().body().getData();
            for (RecordObject record : records) {
                if (record.getLevel() == null && !record.getRuns().isEmpty()) {
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

    private Game lookupGame(String game) {
        SpeedRunComAPI speedRunComAPI = (SpeedRunComAPI) Memebot.plugins.get("speedruncomapi");
        ISpeedRunCom service = speedRunComAPI.getService();
        Call<Games> gameLookup = service.lookupGame(null, game, "categories");
        try {
            ArrayList<Game> games = gameLookup.execute().body().getData();
            if (!games.isEmpty()) {
                return games.get(0);
            }
        } catch (IOException ignored) {}
        return null;
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
