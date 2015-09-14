package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class HelpCommand extends CommandHandler {

	public HelpCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			int j = -1;
			if ((j = channelHandler.findCommand(data[0])) != -1) {
				channelHandler.sendMessage(channelHandler.getChannelCommands().get(j).getHelptext(),
						this.getChannelOrigin());
			} else {
				channelHandler.sendMessage(channelHandler.getBuiltInStrings().get("HELP_NOT_FOUND"),
						this.getChannelOrigin());
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			channelHandler.sendMessage(
					channelHandler.getBuiltInStrings().get("HELP_SYNTAX").replace("{param1}", "!help <command>"),
					this.getChannelOrigin());
		}
	}

}
