package me.krickl.memebotj.InternalCommands;

import java.util.Random;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class FilenameCommand extends CommandHandler {

	public FilenameCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setHelptext("Syntax: !name <filename> (100 points/name) || !name get || !name current"); 
		if( !command.equals("~name") )
			this.setPointCost(0);
	}
	
	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			if (data[0].equals("get")) {
				if (CommandHandler.checkPermission(sender.getUsername(), this.getNeededBroadcasterCommandPower(), channelHandler.getUserList())) {
					Random rand = new Random();
					int index = rand.nextInt(channelHandler.getFileNameList().size() - 1);
					channelHandler.setCurrentFileName(channelHandler.getFileNameList().get(index));
					
					//TODO if name is too long reroll - this allows to have names of more than one length in the list
					//TODO try getting a good name a few times. if it fails return a name that is longer than max len
					
					channelHandler.getFileNameList().remove(index);
					channelHandler.sendMessage("Filename: " + channelHandler.getCurrentFileName().split("#")[0] + " suggested by " + channelHandler.getCurrentFileName().split("#")[1], this.getChannelOrigin());
					
					return;
				}
			}
			else if (data[0].equals("current")) {
				channelHandler.sendMessage("Filename: " + channelHandler.getCurrentFileName().split("#")[0] + " suggested by " + channelHandler.getCurrentFileName().split("#")[1], this.getChannelOrigin());
				return;
			}
			
			if (data[0].length() <= channelHandler.getMaxFileNameLen()) {
				if (sender.getPoints() < 100 && !CommandHandler.checkPermission(sender.getUsername(), this.getNeededBotAdminCommandPower(), channelHandler.getUserList())) {
					channelHandler.sendMessage(String.format("Sorry, you don't have %f points", 100) , this.getChannelOrigin());
				} else {
					channelHandler.getFileNameList().add(data[0] + "#" + sender.getUsername());
					if( !this.getCommand().equals("~name") ) {
						channelHandler.sendMessage(String.format("%s added name %s", sender.getUsername(), data[0]), this.getChannelOrigin());
					}
				}
			}
			
			channelHandler.writeDBChannelData();
		} catch (ArrayIndexOutOfBoundsException e) {
			
		} catch (java.lang.IllegalArgumentException e) {
			
		}
	}

}
