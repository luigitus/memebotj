package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.Memebot;
import me.krickl.memebotj.UserHandler;

public class EditUserCommand extends CommandHandler {

	public EditUserCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setAccess("botadmin");
		this.setNeededCommandPower(75);
	}

	@Override
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		try {
			if(data[0].equals("power")) {
				boolean success = false;
				int newCP = Integer.parseInt(data[2]);
				for (String key : channelHandler.getUserList().keySet()) {
					UserHandler uh = channelHandler.getUserList().get(key);
					if (uh.getUsername().equals(data[1])) {
						if ((newCP + uh.getAutoCommandPower()) > sender.getCommandPower()) {
							channelHandler.sendMessage("You cannot set the command power of a user higher than yours",
									this.getChannelOrigin());
							return;
						}

						uh.setCustomCommandPower(Integer.parseInt(data[2]));
						uh.setCommandPower(uh.getAutoCommandPower());
						uh.writeDBUserData();
						success = true;
					}
				}

				if (!success) {
					UserHandler uh = new UserHandler(data[1], channelHandler.getChannel());
					if (!uh.isNewUser()) {
						if ((newCP + uh.getAutoCommandPower()) > sender.getCommandPower()) {
							channelHandler.sendMessage("You cannot set the command power of a user higher than yours",
									this.getChannelOrigin());
							return;
						}

						uh.setCustomCommandPower(Integer.parseInt(data[2]));
						uh.setCommandPower(uh.getAutoCommandPower());
						uh.writeDBUserData();
						success = true;
					}
				}

				if (success) {
					channelHandler.sendMessage("Changed user power to " + data[2], this.getChannelOrigin());
				} else {
					channelHandler.sendMessage("This user never joined this channel: " + data[1], this.getChannelOrigin());
				}
			} else if(data[0].equals("note")) {

			}
		} catch (ArrayIndexOutOfBoundsException e) {

		} catch (NumberFormatException e) {

		}
	}

}
