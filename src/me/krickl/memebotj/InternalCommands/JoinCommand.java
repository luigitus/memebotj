package me.krickl.memebotj.InternalCommands;

import java.io.IOException;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.ConnectionHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;

public class JoinCommand extends CommandHandler {

	public JoinCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("botadmin");
		this.setNeededCommandPower(75);
		this.setHelptext("Syntax: !mejoin <channel>");
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			Memebot.joinChannel("#" + data[0]);

			channelHandler.sendMessage("Joined channel " + data[0], this.getChannelOrigin());
		} catch (ArrayIndexOutOfBoundsException e) {

		}
	}

}
