package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;

public class SaveCommand extends CommandHandler {

	public SaveCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		channelHandler.sendMessage("Saving...", this.getChannelOrigin());
		for (ChannelHandler ch : Memebot.joinedChannels) {
			ch.writeDBChannelData();
		}
	}

}
