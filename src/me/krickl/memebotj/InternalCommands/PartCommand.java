package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class PartCommand extends CommandHandler {

	public PartCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("botadmin");
		this.setNeededCommandPower(75);
		this.setHelptext("Syntax: !mepart");
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		channelHandler.partChannel(this.getChannelOrigin());
	}
}
