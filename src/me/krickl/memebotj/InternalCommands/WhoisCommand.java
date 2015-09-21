package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class WhoisCommand extends CommandHandler {

	public WhoisCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setHelptext("Syntax: !whois <name>");
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		String user = data[0];
		UserHandler uh = null;
		if (channelHandler.getUserList().containsKey(user)) {
			uh = channelHandler.getUserList().get(user);
		} else {
			uh = new UserHandler(user, this.getChannelOrigin());
		}
		channelHandler.sendMessage(
				uh.getUsername() + " || Broadcaster: " + Boolean.toString(uh.isBroadcaster()) + " || Mod: "
						+ Boolean.toString(uh.isMod()) + " || Command Power: " + Integer.toString(uh.getCommandPower()) + " || Timeouts: " + Integer.toString(uh.getTimeouts()),
				this.getChannelOrigin());
	}

}
