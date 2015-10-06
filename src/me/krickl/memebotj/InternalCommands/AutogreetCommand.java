package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class AutogreetCommand extends CommandHandler {

	public AutogreetCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("moderators");
		this.setNeededCommandPower(25);
		this.setHelptext("Syntax: !autogreet <add/remove/get> <name>");
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			String nameToModify = data[1].toLowerCase();
			String message = "";

			UserHandler user = null;
			if(channelHandler.getUserList().containsKey(nameToModify)) {
				user = channelHandler.getUserList().get(nameToModify);
			} else {
				user = new UserHandler(nameToModify, this.getChannelOrigin());
			}
			
			if (data[0].equals("add")) {
				String newValue = data[2];
				for (int x = 3; x < data.length; x++) {
					newValue = newValue + " " + data[x];
				}
				
				if(!user.isNewUser()) {
					user.setAutogreet(newValue);
					user.writeDBUserData();
					message = "Autogreet added";
				} else {
					message = "Autogreet was not added because this user never entered this chatroom";
				}
			} else if (data[0].equals("remove")) {
				if (channelHandler.getAutogreetList().containsKey(nameToModify)) {
					channelHandler.getAutogreetList().remove(nameToModify);
				} else if(!user.isNewUser()) {
					user.setAutogreet("");
					user.writeDBUserData();
					message = "Autogreet removed";
				}
			} else if (data[0].equals("get")) {
				if (channelHandler.getAutogreetList().containsKey(nameToModify)) {
					channelHandler.sendMessage(channelHandler.getAutogreetList().get(nameToModify),
							this.getChannelOrigin());
				} else if(!user.isNewUser()) {
					channelHandler.sendMessage(user.getAutogreet(),
							this.getChannelOrigin());
				}
			}
			channelHandler.sendMessage(message, this.getChannelOrigin());
		} catch (ArrayIndexOutOfBoundsException e) {

		}
	}
}
