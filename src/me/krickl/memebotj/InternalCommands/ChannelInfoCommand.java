package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class ChannelInfoCommand extends CommandHandler {
	public ChannelInfoCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("moderators");
		this.setNeededCommandPower(25);
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		channelHandler.sendMessage(String.format("Is live: %b || Points per update %f", channelHandler.isLive(), channelHandler.getPointsPerUpdate()), this.getChannelOrigin());
	}
}
