package me.krickl.memebotj.Utility;

import me.krickl.memebotj.Channel.ChannelHandler;
import me.krickl.memebotj.User.UserHandler;

import java.security.SecureRandom;

/**
 * Created by unlink on 6/6/2016.
 */
public class ChatColours {
    // todo make custom channel settings possible
    private static final String[] chatColours = new String[]{"Blue", "BlueViolet", "CadetBlue", "Chocolate", "Coral",
            "DodgerBlue", "Firebrick", "GoldenRod", "Green", "HotPink", "OrangeRed", "Red", "SeaGreen", "SpringGreen",
            "YellowGreen"};

    public static void pickColour(ChannelHandler channelHandler, UserHandler sender) {
        SecureRandom ran = new SecureRandom();

        setColour(channelHandler, sender, chatColours[ran.nextInt(chatColours.length - 1)]);
    }

    public static void setColour(ChannelHandler channelHandler, UserHandler sender, String colour) {
        channelHandler.sendMessage("/color " + colour, channelHandler.getChannel(), sender, false, false, true);
    }
}
