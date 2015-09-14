package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class SuggestNameCommand extends CommandHandler {

	public SuggestNameCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setPointCost(100);
	}
	
	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			if (data[0].length() <= channelHandler.getMaxFileNameLen()) {
				channelHandler.getFileNameList().add(data[0] + "#" + sender.getUsername());
				if( !this.getCommand().equals("~name") ) {
					channelHandler.sendMessage(String.format("%s added name %s", sender.getUsername(), data[0]), this.getChannelOrigin());
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			
		}
	}

}
