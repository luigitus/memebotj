package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class HugCommand extends CommandHandler {

	public HugCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			channelHandler.sendMessage(sender + " hugs " + data[0] + ". HOW CUTE!", this.getChannelOrigin());
		} catch (ArrayIndexOutOfBoundsException e) {
			channelHandler.sendMessage(sender + " hugs nobody. How pathetic!", this.getChannelOrigin());
		}
	}

}
