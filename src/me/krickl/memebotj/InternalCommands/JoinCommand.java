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
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			try {
				ChannelHandler newCH = new ChannelHandler(data[0], new ConnectionHandler(Memebot.ircServer, Memebot.port, Memebot.botNick, Memebot.botPassword));
				newCH.strart();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			
		}
	}

}
