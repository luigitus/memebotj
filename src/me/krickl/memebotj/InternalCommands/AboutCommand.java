package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;

public class AboutCommand extends CommandHandler {

	public AboutCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		channelHandler.sendMessage("memebot-j version " + Memebot.version + " Developed by " + Memebot.dev,
				this.getChannelOrigin());
		channelHandler.sendMessage(
				"Licence: http://vps.krickl.me/license.html || Fork me RitzMitz : https://github.com/unlink2/memebotj",
				this.getChannelOrigin());
	}
}
