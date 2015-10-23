package me.krickl.memebotj.InternalCommands;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class DampeCommand extends CommandHandler {

	public DampeCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setHelptext("Let dampe hate you for only all of your points");
		this.setUserCooldownLen(200);
        this.setListContent(new ArrayList<String>());
        this.setPointCost(0);
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
        double wage = 100;
		try {
            wage = Double.parseDouble(data[0]);
            if (wage < 100) {
                wage = 100;
            }
		} catch(ArrayIndexOutOfBoundsException e) {

		} catch(NumberFormatException e) {

		}

        if(this.checkCost(sender, wage, channelHandler)) {
            channelHandler.sendMessage(String.format("Sorry you don't have %f %s.", wage, channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE")), this.getChannelOrigin());
            return;
        }

        if(wage > 600) {
            channelHandler.sendMessage("Sorry the wage can't be more than 600 " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE"), this.getChannelOrigin());
            return;
        }

        sender.setPoints(sender.getPoints() - wage);

		//happy now Luigitus?
		SecureRandom ran = new SecureRandom();
        int range = 1000;
		int outcome = ran.nextInt(range - (int)wage);
        if (outcome <= 3) {
            channelHandler.sendMessage(sender.getUsername() + ": Dampé found " + Double.toString(10000) + " " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + "! You lucky bastard!", this.getChannelOrigin());
			sender.setPoints(sender.getPoints() + 10000 + wage);
		}
		else if(outcome <= 15) {
			channelHandler.sendMessage(sender.getUsername() + ": Dampé found " + Double.toString(1000) + " " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + "! You lucky bastard!", this.getChannelOrigin());
			sender.setPoints(sender.getPoints() + 1000 + wage);
		} else if(outcome <= 100) {
			channelHandler.sendMessage(sender.getUsername() + ": Dampé found " + Double.toString(300) + " " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + "! Pretty good!", this.getChannelOrigin());
			sender.setPoints(sender.getPoints() + 300 + wage);
		} else if(outcome <= 600) {
			channelHandler.sendMessage(sender.getUsername() + ": Dampé is being a dick and returned " + Double.toString(wage/2) + " " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + "!", this.getChannelOrigin());
			sender.setPoints(sender.getPoints() + wage / 2);
		} else {
			channelHandler.sendMessage(sender.getUsername() + ": Dampé spent your " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + " on hookers, booze and crack!", this.getChannelOrigin());
		}
	}
}
