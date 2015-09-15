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
		if (channelHandler.getUserList().containsKey(user)) {
			UserHandler uh = channelHandler.getUserList().get(user);
			channelHandler.sendMessage(
					uh.getUsername() + "|| Broadcaster: " + Boolean.toString(uh.isBroadcaster()) + "|| Mod: "
							+ Boolean.toString(uh.isMod()) + "|| VIP: " + Boolean.toString(uh.isVIP()),
					this.getChannelOrigin());
		}
	}

}
