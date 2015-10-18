package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class AliasCommand extends CommandHandler {
	public AliasCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("broadcaster");
		this.setNeededCommandPower(50);
		this.setHelptext("");
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			if(data[0].equals("add")) {
				channelHandler.getAliasList().add(data[1]);
				channelHandler.sendMessage("Added alias " + data[1], this.getChannelOrigin());
			}
		} catch(ArrayIndexOutOfBoundsException e) {

		}
	}
}
