package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class ModeratorsCommand extends CommandHandler {

	public ModeratorsCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setHelptext("Displays a list of moderators");
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		String outStr = "";
		for (String key : channelHandler.getUserList().keySet()) {
			if (channelHandler.getUserList().get(key).isMod())
				outStr = outStr + ", " + channelHandler.getUserList().get(key).getUsername();
		}

		channelHandler.sendMessage(outStr, this.getChannelOrigin());
	}

}
