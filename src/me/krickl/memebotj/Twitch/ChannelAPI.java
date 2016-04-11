package me.krickl.memebotj.Twitch;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Memebot;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * This file is part of memebotj.
 * Created by unlink on 09/04/16.
 */
public class ChannelAPI {
    ChannelHandler channelHandler = null;
    public ChannelAPI(ChannelHandler ch) {
        this.channelHandler = ch;
    }

    public void update() {
        //twitch update
        try {
            if (Memebot.isTwitchBot) {
                String data = Memebot.urlRequest("https://api.twitch.tv/kraken/streams/" + channelHandler.getChannel(), 5000, false);
                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject)parser.parse(data);
                Object isOnline = obj.get("stream");
                if (isOnline == null) {
                    channelHandler.setLive(false);
                } else {
                    if (channelHandler.isLive()) {
                    }
                    channelHandler.setLive(true);
                }
            } else {
                channelHandler.setLive(true);
            }
        } catch(ParseException e) {
            e.printStackTrace();
        }

        //get game
        try {
            if (Memebot.isTwitchBot) {
                String data = Memebot.urlRequest("https://api.twitch.tv/kraken/channels/" + channelHandler.getChannel(), 5000, false);

                JSONParser parser = new JSONParser();
                JSONObject obj = (JSONObject) parser.parse(data);
                channelHandler.setCurrentGame((String)obj.get("game"));
                if (channelHandler.getCurrentGame() == null) {
                    channelHandler.setCurrentGame("Not Playing");
                }
            } else {
                channelHandler.setCurrentGame("");
            }
        } catch(ParseException e)  {
            e.printStackTrace();
        }
    }
}
