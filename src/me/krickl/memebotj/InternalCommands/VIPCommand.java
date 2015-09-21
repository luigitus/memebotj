package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class VIPCommand extends CommandHandler {
	public VIPCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("broadcaster");
		this.setNeededCommandPower(50);
		this.setHelptext("Syntax: !vip <name>");
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			if (channelHandler.getUserList().containsKey(data[0])) {
				if (channelHandler.getUserList().get(data[0]).isVIP()) {
					channelHandler.getUserList().get(data[0]).setVIP(false);
					channelHandler.sendMessage("Removed vip status", this.getChannelOrigin());
				} else {
					channelHandler.getUserList().get(data[0]).setVIP(true);
					channelHandler.sendMessage("Added vip status", this.getChannelOrigin());
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {

		}
	}
}
