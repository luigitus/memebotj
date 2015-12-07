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
	public void commandScript(UserHandler sender, ChannelHandler channelHandler, String[] data) {
		if (channelHandler.getUserList().containsKey(sender.getUsername())) {
			if (data.length < 1) {
				channelHandler.sendMessage(String.format("%s: %.2f %s", sender.getUsername(), channelHandler.getUserList().get(sender.getUsername()).getPoints(), channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE")), this.getChannelOrigin());
			} else {
				try {
					UserHandler target = null;
					if (channelHandler.getUserList().containsKey(data[1].toLowerCase())) {
						target = channelHandler.getUserList().get(data[1].toLowerCase());
					} else {
						target = new UserHandler(data[1], this.getChannelOrigin());
						if (target.isNewUser()) {
							target = null;
						}
					}

					if (target != null && CommandHandler.checkPermission(
							sender.getUsername(), this.getNeededBotAdminCommandPower(), channelHandler.getUserList())) {
                        double number = Double.parseDouble(data[2]);
                        if (data[0].equals("add")) {
                            target.setPoints(target.getPoints() + number);
                        } else if (data[0].equals("sub")) {
                            target.setPoints(target.getPoints() - number);
                        } else if (data[0].equals("reset")) {
                            target.setPoints(0.0);
                        }

                        channelHandler.sendMessage(
                                String.format("%s your new total is: %.2f %s", target.getUsername(), target.getPoints(),
                                        channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE")),
                                this.getChannelOrigin());
                    }
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
