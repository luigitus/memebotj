package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class UptimeCommand extends CommandHandler {
	
	public UptimeCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setNeededCommandPower(75);
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		int currentUpTimeSeconds = channelHandler.getStreamStartTime() - (int) (System.currentTimeMillis() / 1000L);
		channelHandler.sendMessage(String.format("Uptime: %d", currentUpTimeSeconds), this.getChannelOrigin());
	}
}
