package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class HypeCommand extends CommandHandler {
	public HypeCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("botadmin");
		this.setNeededCommandPower(75);
		this.setHelptext("Hype");
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		for (int i = 0; i < 1; i++) {
			channelHandler.sendMessage("/me <3 <3 HYPE HYPE HYPE HYPE HYPE HYPE <3 <3", this.getChannelOrigin());
		}
	}
}
