package me.krickl.memebotj.InternalCommands;

import java.util.Random;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

@Deprecated
public class PickNameCommand extends CommandHandler {
	public PickNameCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setHelptext("This command is deprecated");
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			if (CommandHandler.checkPermission(sender.getUsername(), "broadcaster", channelHandler.getUserList())) {
				Random rand = new Random();
				
				int index = rand.nextInt(channelHandler.getFileNameList().size() - 1);
				channelHandler.setCurrentFileName(channelHandler.getFileNameList().get(index));
				channelHandler.getFileNameList().remove(index);
			}
			
			channelHandler.sendMessage("Filename: " + channelHandler.getCurrentFileName().split("#")[0] + " suggested by " + channelHandler.getCurrentFileName().split("#")[1], this.getChannelOrigin());
		} catch (java.lang.IllegalArgumentException e) {
			
		} catch (java.lang.ArrayIndexOutOfBoundsException e) {
			
		}
	}
}
