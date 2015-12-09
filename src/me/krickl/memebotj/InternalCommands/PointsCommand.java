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
		this.setSuccess(false);
		if (channelHandler.getUserList().containsKey(sender.getUsername().toLowerCase())) {
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
                            this.setSuccess(true);
                        } else if (data[0].equals("sub")) {
                            target.setPoints(target.getPoints() - number);
                            this.setSuccess(true);
                        } else if (data[0].equals("set")) {
                            target.setPoints(number);
                            this.setSuccess(true);
                        }

                        if(this.getSuccess()) {
                            channelHandler.sendMessage(
                                    String.format("%s your new total is: %.2f %s", target.getUsername(), target.getPoints(),
                                            channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE")),
                                    this.getChannelOrigin());
                        }
                    }

                    if (target != null && !this.getSuccess()) {
                        double number = Double.parseDouble(data[2]);
                        double tax = number / 100 * 10;
						if(data[0].equals("send")) {
                            if(this.checkCost(sender, number + tax, channelHandler)) {
                                sender.setPoints(sender.getPoints() - (number + tax));
                                target.setPoints(target.getPoints() + number);
                                channelHandler.sendMessage(String.format("%s: You sent %.2f %s to %s", target.getUsername(), number,
                                        channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE"), target.getUsername()), this.getChannelOrigin());
                            }
                        } else {
                            channelHandler.sendMessage(String.format("%s: Sorry you don't have %.2f %s", target.getUsername(), number + tax,
                                    channelHandler.getBuiltInStrings().get("CURRENCY_EMOTE")), this.getChannelOrigin());
                        }
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
			}

		}
	}
}
