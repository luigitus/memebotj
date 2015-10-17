package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class APIInformationCommand extends CommandHandler {

	public APIInformationCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setNeededCommandPower(75);
	}
	
	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		//this command will publicly print information about the api right now! use with caution.
		
		channelHandler.sendMessage("Current api connection: " + channelHandler.getApiConnectionIP() + " || Private Key for channel: " + channelHandler.getPrivateKey() + " || Private key for sender: " + sender.getPrivateKey(), this.getChannelOrigin());
	}
	
}
