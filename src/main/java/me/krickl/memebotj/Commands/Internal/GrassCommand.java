package me.krickl.memebotj.Commands.Internal;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.Commands.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;

import java.security.SecureRandom;

/**
 * The memes live on
 * This file is part of memebotj.
 * Created by unlink on 22/04/16.
 */
public class GrassCommand extends CommandHandler {
    public GrassCommand(ChannelHandler channelHandler, String commandName, String dbprefix) {
        super(channelHandler, commandName, dbprefix);
    }

    @Override
    public void initCommand() {

    }

    @Override
    public void overrideDB() {
        this.setCost(50);
        this.setUserCooldownLength(90);
        this.setEnabled(true);
    }

    @Override
    public void commandScript(UserHandler sender, String[] data) {
        SecureRandom random = new SecureRandom();

        int outcome = random.nextInt(100);
        if (outcome <= 10) {
            getChannelHandler().sendMessage(Memebot.formatText("GRASS_STICK_FOUND", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
            sender.getUserInventory().addItem("dekustick", 1);
        } else if(outcome <= 20) {
            getChannelHandler().sendMessage(Memebot.formatText("GRASS_BOMB_FOUND", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
            sender.getUserInventory().addItem("bomb", 1);
        } else if(outcome <= 30) {
            getChannelHandler().sendMessage(Memebot.formatText("GRASS_ARROW_FOUND", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
            sender.getUserInventory().addItem("arrow", 5);
        }  else if(outcome <= 40) {
            getChannelHandler().sendMessage(Memebot.formatText("GRASS_BOMBCHUS_FOUND", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
            sender.getUserInventory().addItem("bombchu", 1);
        } else if(outcome <= 50) {
            getChannelHandler().sendMessage(Memebot.formatText("GRASS_FAIRY_FOUND", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
            sender.getUserInventory().addItem("fairy", 1);
        } else {
            getChannelHandler().sendMessage(Memebot.formatText("GRASS_FOUND_NOTHING", getChannelHandler(), sender, this, true, new String[]{}, ""), this.getChannelHandler().getChannel());
        }
    }
}
