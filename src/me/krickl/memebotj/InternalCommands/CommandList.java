package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class CommandList extends CommandHandler {

	public CommandList(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setHelptext("Displays a command list");
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		channelHandler.sendMessage(
				"Commands: "
						+ channelHandler.getChannelPageBaseURL() + "/index.html || Use !help for further information || Documentation: https://github.com/unlink2/memebotj/blob/master/README.md",
				this.getChannelOrigin());
	}
}
