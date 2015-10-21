package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class RaceCommand extends CommandHandler {

	public RaceCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("moderators");
		this.setHelptext("Syntax: !race <channel1> <channel2> <channel3> ... || !race");
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		if (data.length >= 1 && CommandHandler.checkPermission(sender.getUsername(), this.getNeededModCommandPower(),
				channelHandler.getUserList())) {
			channelHandler.setCurrentRaceURL(channelHandler.getRaceBaseURL() + "/" + channelHandler.getBroadcaster());
			for (int i = 0; i < data.length; i++) {
				channelHandler.setCurrentRaceURL(channelHandler.getCurrentRaceURL() + "/" + data[i]);
			}
			channelHandler.sendMessage(channelHandler.getCurrentRaceURL(), this.getChannelOrigin());
		} else {
			channelHandler.sendMessage(channelHandler.getCurrentRaceURL(), this.getChannelOrigin());
		}
	}
}
