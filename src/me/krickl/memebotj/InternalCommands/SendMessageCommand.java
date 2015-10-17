package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;

public class SendMessageCommand extends CommandHandler {

	public SendMessageCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("botadmin");
		this.setNeededCommandPower(75);
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		String msg = "";
		for (String s : data) {
			msg = msg + " " + s;
		}

		for (ChannelHandler ch : Memebot.joinedChannels()) {
			ch.sendMessage(msg, ch.getChannel());
		}
	}

}
