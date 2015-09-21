package me.krickl.memebotj.InternalCommands;

import me.krickl.memebotj.ChannelHandler;
import me.krickl.memebotj.CommandHandler;
import me.krickl.memebotj.UserHandler;

public class PointsCommand extends CommandHandler {
	public PointsCommand(String channel, String command, String dbprefix) {
		super(channel, command, dbprefix);
		this.setHelptext("Shows points of user");
	}

	@Override
	protected void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		if (channelHandler.getUserList().containsKey(sender.getUsername())) {
			if(data.length < 1) {
				channelHandler.sendMessage(sender.getUsername() + ": "
						+ Double.toString(channelHandler.getUserList().get(sender.getUsername()).getPoints()), this.getChannelOrigin());
			} else {
				try {
					if (channelHandler.getUserList().containsKey(data[1]) && CommandHandler.checkPermission(sender.getUsername(), this.getNeededBotAdminCommandPower(), channelHandler.getUserList())) {
						UserHandler target = channelHandler.getUserList().get(data[1]);
						double number = Double.parseDouble(data[2]);
						if (data[0].equals("add")) {
							target.setPoints(target.getPoints() + number);
						} else if (data[0].equals("sub")) {
							target.setPoints(target.getPoints() - number);
						}
						
						channelHandler.sendMessage(String.format("%s your new total is: %f", target.getUsername(), target.getPoints()), this.getChannelOrigin());
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					
				}
			}
			
		}
	}
}
