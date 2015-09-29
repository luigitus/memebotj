package me.krickl.memebotj.InternalCommands;

import java.util.ArrayList;
import java.util.Random;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class DampeCommand extends CommandHandler {

	public DampeCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setHelptext("Let dampe hate you for only all of your points");
		this.setCmdtype("list");
		this.setListContent(new ArrayList<String>());
		this.getListContent().add("Dampé slowly walks towards a grave...");
		this.getListContent().add("Dampé stops in the middle of the graveyard, but after half an hour he decides to dig a hole...");
		this.getListContent().add("Dampé is busy staring at what appears to be nothing...");
		this.setPointCost(1);
	}
 
	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			if(data[0].equalsIgnoreCase(sender.getUsername())) {
				Random ran = new Random();
				int outcome = ran.nextInt(100);
				Thread.sleep(100);
				if(outcome <= 1) {
					channelHandler.sendMessage("Dampé found " + Double.toString(sender.getPoints() * 3) + " " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + "! You lucky bastard!", this.getChannelOrigin());
					sender.setPoints(sender.getPoints() * 3);
				} else if(outcome <= 10) {
					channelHandler.sendMessage("Dampé found " + Double.toString(sender.getPoints() * 2) + " " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + "! Pretty good!", this.getChannelOrigin());
					sender.setPoints(sender.getPoints() * 2);
				} else if(outcome <= 60) {
					channelHandler.sendMessage("Dampé is being a dick and returned half of your " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + "!", this.getChannelOrigin());
					sender.setPoints(sender.getPoints() / 2);
				}
				else {
					channelHandler.sendMessage("Dampé spent all of your " + channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE") + " on hookers, booze and crack!", this.getChannelOrigin());
					sender.setPoints(0);
				}
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			channelHandler.sendMessage("Dampé will take all of your " + channelHandler.getBuiltInStrings().get("CURRENCY_NAME")
					+ "! Are you sure you want to continue? Type !dampe " + sender.getUsername() + " to confirm.", this.getChannelOrigin());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
