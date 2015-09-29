package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

//this command is used for debug purposes only and should never be used
public class DebugCommand extends CommandHandler {

	public DebugCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("botadmin");
		this.setNeededCommandPower(75);
		this.setExcludeFromCommandList(true);
	}
	
	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		channelHandler.sendMessage("This'll crash Kappa", this.getChannelOrigin());
	}

}
