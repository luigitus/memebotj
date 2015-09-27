package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class MujuruGame extends CommandHandler {

	public MujuruGame(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setHelptext("");
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		channelHandler.sendMessage("Comming soon enough Kappa", this.getChannelOrigin());
	}

}
