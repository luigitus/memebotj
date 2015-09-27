package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;

public class UserPowerCommand extends CommandHandler {

	public UserPowerCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("botadmin");
		this.setNeededCommandPower(75);
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			boolean success = false;
			int newCP = Integer.parseInt(data[1]);
			for (String key : channelHandler.getUserList().keySet()) {

				UserHandler uh = channelHandler.getUserList().get(key);
				if (uh.getUsername().equals(data[0])) {
					if ((newCP + uh.getAutoCommandPower()) > sender.getCommandPower()) {
						channelHandler.sendMessage("You cannot set the command power of a user higher than yours",
								this.getChannelOrigin());
						return;
					}

					uh.setCustomCommandPower(Integer.parseInt(data[1]));
					uh.setCommandPower(uh.getAutoCommandPower());
					uh.writeDBUserData();
					success = true;
				}
			}

			if (!success) {
				UserHandler uh = new UserHandler(data[0], channelHandler.getChannel());
				if (!uh.isNewUser()) {
					if ((newCP + uh.getAutoCommandPower()) > sender.getCommandPower()) {
						channelHandler.sendMessage("You cannot set the command power of a user higher than yours",
								this.getChannelOrigin());
						return;
					}

					uh.setCustomCommandPower(Integer.parseInt(data[1]));
					uh.setCommandPower(uh.getAutoCommandPower());
					uh.writeDBUserData();
					success = true;
				}
			}

			if (success) {
				channelHandler.sendMessage("Changed user power to " + data[1], this.getChannelOrigin());
			} else {
				channelHandler.sendMessage("This user never joined this channel: " + data[0], this.getChannelOrigin());
			}
		} catch (ArrayIndexOutOfBoundsException e) {

		} catch (NumberFormatException e) {

		}
	}

}
